package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainStationQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainStationSaveDTO;
import com.jiawa.train.business.VO.DailyTrainStationQueryVO;
import com.jiawa.train.business.domain.DailyTrainStation;
import com.jiawa.train.business.domain.DailyTrainStationExample;
import com.jiawa.train.business.domain.TrainStation;
import com.jiawa.train.business.mapper.DailyTrainStationMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainStationService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainStationService.class);

    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;


    @Resource
    private TrainStationService trainStationService;

    //保存
    public void save(DailyTrainStationSaveDTO dailyTrainStationSaveDTO){
        DateTime now=DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(dailyTrainStationSaveDTO, DailyTrainStation.class);
        if(ObjectUtil.isEmpty(dailyTrainStation.getId())) {
            dailyTrainStation.setId(SnowUtil.getSnowflakeId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }else{
            dailyTrainStation.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrainStation.getId());
            dailyTrainStationMapper.updateByPrimaryKey( dailyTrainStation);
        }
    }

    //查询列表
    public PageVO<DailyTrainStationQueryVO> queryList(DailyTrainStationQueryDTO dailyTrainStationQueryDTO){
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.setOrderByClause("date desc,train_code asc,`index` asc");
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();

        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainStationQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(dailyTrainStationQueryDTO.getTrainCode());
        }
        if(ObjectUtil.isNotNull(dailyTrainStationQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainStationQueryDTO.getDate());
        }

        PageHelper.startPage(dailyTrainStationQueryDTO.getPage(),dailyTrainStationQueryDTO.getSize());
        List<DailyTrainStation> dailyTrainStationList =dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(dailyTrainStationList);

        PageVO<DailyTrainStationQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainStationMapper.deleteByPrimaryKey(id);
    }

    //生成date天后trainCode每日车站信息
    public void genDaily(Date date,String trainCode){
        LOG.info("开始生成{}的{}车次车站每日数据",DateUtil.format(date, "yyyy-MM-dd"),trainCode);
        //删除所有的每日车站，注意是某日的
        DailyTrainStationExample dailyTrainStationExample=new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria()
                .andTrainCodeEqualTo(trainCode)
                .andDateEqualTo(date);
        dailyTrainStationMapper.deleteByExample(dailyTrainStationExample);
        //获取所有车站
        List<TrainStation> trainStationList=trainStationService.selectByTrainCode(trainCode);
        if(CollUtil.isEmpty(trainStationList)){
            LOG.info("{}，该车次没有基础车站数据",trainCode);
            return;
        }
        //插入该车次所有的车站
        for (TrainStation trainStation : trainStationList){
            DateTime now = DateTime.now();
            DailyTrainStation dailyTrainStation=BeanUtil.copyProperties(trainStation,DailyTrainStation.class);
            dailyTrainStation.setId(SnowUtil.getSnowflakeId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setDate(date);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }
        LOG.info("结束生成{}的{}车次车站每日数据", DateUtil.format(date, "yyyy-MM-dd"),trainCode);
    }

    //查询某日某车次所经过的所有车站
    public List<DailyTrainStation> selectByDateTrain(Date date, String trainCode){
        DailyTrainStationExample dailyTrainStationExample=new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        return dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
    }
}
