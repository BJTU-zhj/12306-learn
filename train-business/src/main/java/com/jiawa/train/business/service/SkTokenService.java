package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.SkTokenQueryDTO;
import com.jiawa.train.business.DTO.SkTokenSaveDTO;
import com.jiawa.train.business.VO.SkTokenQueryVO;
import com.jiawa.train.business.domain.DailyTrainStation;
import com.jiawa.train.business.domain.SkToken;
import com.jiawa.train.business.domain.SkTokenExample;
import com.jiawa.train.business.enums.RedisKeyPreEnum;
import com.jiawa.train.business.mapper.SkTokenMapper;
import com.jiawa.train.business.mapper.custom.SkTokenCustomMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SkTokenService {

    private static final Logger LOG= LoggerFactory.getLogger(SkTokenService.class);

    @Resource
    private SkTokenMapper skTokenMapper;

    @Resource
    private SkTokenCustomMapper skTokenCustomMapper;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @Resource
    private RedissonClient redissonClient;

    //保存
    public void save(SkTokenSaveDTO skTokenSaveDTO){
        DateTime now=DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(skTokenSaveDTO, SkToken.class);
        if(ObjectUtil.isEmpty(skToken.getId())) {
            skToken.setId(SnowUtil.getSnowflakeId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        }else{
            skToken.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", skToken.getId());
            skTokenMapper.updateByPrimaryKey( skToken);
        }
    }

    //查询列表
    public PageVO<SkTokenQueryVO> queryList(SkTokenQueryDTO skTokenQueryDTO){
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id desc");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        PageHelper.startPage(skTokenQueryDTO.getPage(),skTokenQueryDTO.getSize());
        List<SkToken> skTokenList =skTokenMapper.selectByExample(skTokenExample);
        ;
        //固定用插件获取查询总数
        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokenList);

        PageVO<SkTokenQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(skTokenList, SkTokenQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        skTokenMapper.deleteByPrimaryKey(id);
    }

    //生成每日车次的令牌
    public void genDaily(Date date,String trainCode){
        //首先删除原记录
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        skTokenMapper.deleteByExample(skTokenExample);
        //查询计算出新的令牌数
        //首先计算出这天这个车次的总座位数
        Integer countSeat= dailyTrainSeatService.countBySeatType(date, trainCode,null);
        //然后获取该车所途径的所有车站
        List<DailyTrainStation> dailyTrainStationList = dailyTrainStationService.selectByDateTrain(date, trainCode);
        Integer countStation = dailyTrainStationList.size()-1;
        Integer countToken =  countSeat * countStation;
        //构造对象保存
        DateTime now = DateTime.now();
        SkToken skToken = new SkToken();
        skToken.setId(SnowUtil.getSnowflakeId());
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setCount(countToken);
        skToken.setCreateTime(now);
        skToken.setUpdateTime(now);
        skTokenMapper.insert(skToken);
    }

    //获取令牌，加上分布式锁并且不删除锁可以防止同一个用户机器人刷票，此处不应该使用看门狗方式
    public Boolean getToken(Date date, String trainCode, Long memberId){

        //先抢令牌的分布式锁
        String lockKey= RedisKeyPreEnum.TICKET_TOKEN_LOCK.getCode()+date+"-"+trainCode+memberId.toString();
        RLock rLock=redissonClient.getLock(lockKey);
        try {
            boolean tryLock=rLock.tryLock(0,5, TimeUnit.SECONDS);
            if(tryLock){
                LOG.info("获取令牌锁成功，接下来开始尝试获取令牌！");
            }else{
                LOG.info("获取令牌锁失败，请稍后再试！");
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TICKET_TOKEN_LOCK_IS_BUSY);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //查询令牌库存-为避免数据库压力过大，使用缓存
        //首先尝试查缓存
        String countKey=RedisKeyPreEnum.TICKET_TOKEN_CACHE.getCode()+date+"-"+trainCode;
        RAtomicLong rAtomicLong=redissonClient.getAtomicLong(countKey);
        //如果缓存有数据，则直接返回
        if(rAtomicLong.isExists()){
            LOG.info("缓存有令牌库存，{}",countKey);
            //减一后并获取值
            Long count=rAtomicLong.decrementAndGet();
            //重置缓存时间
            rAtomicLong.expire(5,TimeUnit.MINUTES);
            if(count<0L){
                LOG.info("令牌已售空！缓存值为{}", count);
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TICKET_TOKEN_CACHE_ZERO);
            }
            //缓存还够
            else{
                //每五次同步一下数据库
                if(rAtomicLong.get()%5==0){
                    skTokenCustomMapper.decrease(date, trainCode,5);
                }
                return true;
            }
        }
        //缓存没有数据，则从数据库中获取
        else{
            SkTokenExample skTokenExample = new SkTokenExample();
            skTokenExample.createCriteria().andDateEqualTo( date).andTrainCodeEqualTo( trainCode);
            List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);
            if(CollUtil.isEmpty(skTokenList)){
                LOG.info("找不到{}日期的{}车次的令牌库存信息",date,trainCode);
                return false;
            }
            rAtomicLong.set(skTokenList.get(0).getCount()-1);
            rAtomicLong.expire(60,TimeUnit.MINUTES);

            if(skTokenList.get(0).getCount()<0){
                LOG.info("令牌已售空！数据库值为0");
                return false;
            }
            //同步数据库
            skTokenCustomMapper.decrease(date, trainCode,1);
            return true;

        }


//        Integer isVail=skTokenCustomMapper.decrease(date, trainCode);
//        if(isVail>0){
//            return true;
//        }else{
//            return false;
//        }
    }

}
