package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainSeatQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainSeatSaveDTO;
import com.jiawa.train.business.VO.DailyTrainSeatQueryVO;
import com.jiawa.train.business.domain.*;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainSeatService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainSeatService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private TrainSeatService trainSeatService;

    @Resource
    private TrainStationService trainStationService;

    //保存
    public void save(DailyTrainSeatSaveDTO dailyTrainSeatSaveDTO){
        DateTime now=DateTime.now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(dailyTrainSeatSaveDTO, DailyTrainSeat.class);
        if(ObjectUtil.isEmpty(dailyTrainSeat.getId())) {
            dailyTrainSeat.setId(SnowUtil.getSnowflakeId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }else{
            dailyTrainSeat.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrainSeat.getId());
            dailyTrainSeatMapper.updateByPrimaryKey( dailyTrainSeat);
        }
    }

    //查询列表
    public PageVO<DailyTrainSeatQueryVO> queryList(DailyTrainSeatQueryDTO dailyTrainSeatQueryDTO){
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.setOrderByClause("date desc,train_code asc,carriage_index  asc");
        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();
        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainSeatQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(dailyTrainSeatQueryDTO.getTrainCode());
        }
        if(ObjectUtil.isNotNull(dailyTrainSeatQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainSeatQueryDTO.getDate());
        }

        PageHelper.startPage(dailyTrainSeatQueryDTO.getPage(),dailyTrainSeatQueryDTO.getSize());
        List<DailyTrainSeat> dailyTrainSeatList =dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeatList);

        PageVO<DailyTrainSeatQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainSeatList, DailyTrainSeatQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }

    //生成date天后trainCode每日座位信息
    public void genDaily(Date date, String trainCode){
        LOG.info("开始生成{}的{}车次座位每日数据", DateUtil.format(date, "yyyy-MM-dd"), trainCode);
        //删除所有的每日座位，注意是某日的
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        dailyTrainSeatMapper.deleteByExample(dailyTrainSeatExample);
        //获取该车次所有的座位
        List<TrainSeat> trainSeatList = trainSeatService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(trainSeatList)){
            LOG.info("{},该车次无基础座位信息，结束", trainCode);
            return;
        }
        List<TrainStation> trainStationList =trainStationService.selectByTrainCode(trainCode);
        String sell= StrUtil.fillBefore("",'0',trainStationList.size()-1);
        for (TrainSeat trainSeat : trainSeatList) {
            DateTime now = DateTime.now();
            DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(trainSeat, DailyTrainSeat.class);
            dailyTrainSeat.setId(SnowUtil.getSnowflakeId());
            dailyTrainSeat.setDate(date);
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeat.setSell(sell);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }
    }

}
