package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
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
import com.jiawa.train.business.mapper.SkTokenMapper;
import com.jiawa.train.business.mapper.custom.SkTokenCustomMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
        Integer countToken = (int) (countSeat * countStation * 0.75);
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

    //获取令牌
    public Boolean getToken(Date date, String trainCode){
        Integer isVail=skTokenCustomMapper.decrease(date, trainCode);
        if(isVail>0){
            return true;
        }else{
            return false;
        }
    }

}
