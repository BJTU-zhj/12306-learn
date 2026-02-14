package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainSaveDTO;
import com.jiawa.train.business.VO.DailyTrainQueryVO;
import com.jiawa.train.business.domain.DailyTrain;
import com.jiawa.train.business.domain.DailyTrainExample;
import com.jiawa.train.business.domain.Train;
import com.jiawa.train.business.mapper.DailyTrainMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainService.class);

    @Resource
    private DailyTrainMapper dailyTrainMapper;

    @Resource
    private TrainService trainService;

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    //保存
    public void save(DailyTrainSaveDTO dailyTrainSaveDTO){
        DateTime now=DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(dailyTrainSaveDTO, DailyTrain.class);
        if(ObjectUtil.isEmpty(dailyTrain.getId())) {
            dailyTrain.setId(SnowUtil.getSnowflakeId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        }else{
            dailyTrain.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrain.getId());
            dailyTrainMapper.updateByPrimaryKey( dailyTrain);
        }
    }

    //查询列表
    public PageVO<DailyTrainQueryVO> queryList(DailyTrainQueryDTO dailyTrainQueryDTO){
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.setOrderByClause("date desc,code asc");
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();

        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainQueryDTO.getCode())){
            criteria.andCodeEqualTo(dailyTrainQueryDTO.getCode());
        }
        if(ObjectUtil.isNotNull(dailyTrainQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainQueryDTO.getDate());
        }
        PageHelper.startPage(dailyTrainQueryDTO.getPage(),dailyTrainQueryDTO.getSize());
        List<DailyTrain> dailyTrainList =dailyTrainMapper.selectByExample(dailyTrainExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrainList);

        PageVO<DailyTrainQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainList, DailyTrainQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainMapper.deleteByPrimaryKey(id);
    }

    //删除date天的每日车次数据
    public void deleteDailyTrain(Date date,String code){
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.createCriteria()
                .andDateEqualTo(date)
                .andCodeEqualTo(code);
        dailyTrainMapper.deleteByExample(dailyTrainExample);
    }

    //生成date天后的每日车次、车次车站数据
    @Transactional
    public void genDaily(Date date){
        //获取所有车次
        List<Train> trainList=trainService.selectAll();
        if(CollUtil.isEmpty(trainList)){
            LOG.info("{},无每日车次数据", DateUtil.format(date, "yyyy-MM-dd"));
            return;
        }
        for (Train train : trainList){
            //先删除当前车次当前date的每日数据
            deleteDailyTrain(date,train.getCode());
            //删除后添加数据
            DateTime now=DateTime.now();
            DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
            dailyTrain.setId(SnowUtil.getSnowflakeId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrain.setDate(date);
            dailyTrainMapper.insert(dailyTrain);

            //生成当前车次的每日车次车站数据
            dailyTrainStationService.genDaily(date,train.getCode());

            //生成当前车次的每日车厢数据
            dailyTrainCarriageService.genDaily(date,train.getCode());
        }
    }

}
