package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainTicketQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainTicketSaveDTO;
import com.jiawa.train.business.VO.DailyTrainTicketQueryVO;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.domain.DailyTrainTicketExample;
import com.jiawa.train.business.domain.TrainStation;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private TrainStationService trainStationService;

    //保存
    public void save(DailyTrainTicketSaveDTO dailyTrainTicketSaveDTO){
        DateTime now=DateTime.now();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(dailyTrainTicketSaveDTO, DailyTrainTicket.class);
        if(ObjectUtil.isEmpty(dailyTrainTicket.getId())) {
            dailyTrainTicket.setId(SnowUtil.getSnowflakeId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        }else{
            dailyTrainTicket.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrainTicket.getId());
            dailyTrainTicketMapper.updateByPrimaryKey( dailyTrainTicket);
        }
    }

    //查询列表
    public PageVO<DailyTrainTicketQueryVO> queryList(DailyTrainTicketQueryDTO dailyTrainTicketQueryDTO){
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.setOrderByClause("id desc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();

        PageHelper.startPage(dailyTrainTicketQueryDTO.getPage(),dailyTrainTicketQueryDTO.getSize());
        List<DailyTrainTicket> dailyTrainTicketList =dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTicketList);

        PageVO<DailyTrainTicketQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }

    //自动生成每日余票信息
    public void genDaily(Date date, String trainCode){
        LOG.info("开始生成日期【{}】车次【{}】的余票信息", DateUtil.format(date, "yyyy-MM-dd"), trainCode);
        //删除某日某车次的数据
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);
        //获取该车次的所有的车站
        List<TrainStation> trainStationList = trainStationService.selectByTrainCode(trainCode);
        if(CollUtil.isEmpty(trainStationList)){
            LOG.info("该车次没有车站基础数据，生成该车次的余票信息结束");
            return;
        }
        //初始化余票信息
        for (int startIndex = 0; startIndex < trainStationList.size(); startIndex++){
            for (int endIndex = startIndex+1; endIndex < trainStationList.size(); endIndex++){
                DateTime now=DateTime.now();
                DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                dailyTrainTicket.setId(SnowUtil.getSnowflakeId());
                dailyTrainTicket.setDate(date);
                dailyTrainTicket.setTrainCode(trainCode);
                dailyTrainTicket.setStart(trainStationList.get(startIndex).getName());
                dailyTrainTicket.setStartPinyin(trainStationList.get(startIndex).getNamePinyin());
                dailyTrainTicket.setStartTime(trainStationList.get(startIndex).getInTime());
                dailyTrainTicket.setStartIndex(trainStationList.get(startIndex).getIndex());
                dailyTrainTicket.setEnd(trainStationList.get(endIndex).getName());
                dailyTrainTicket.setEndPinyin(trainStationList.get(endIndex).getNamePinyin());
                dailyTrainTicket.setEndTime(trainStationList.get(endIndex).getInTime());
                dailyTrainTicket.setEndIndex(trainStationList.get(endIndex).getIndex());
                dailyTrainTicket.setYdz(0);
                dailyTrainTicket.setYdzPrice(BigDecimal.ZERO);
                dailyTrainTicket.setEdz(0);
                dailyTrainTicket.setEdzPrice(BigDecimal.ZERO);
                dailyTrainTicket.setRw(0);
                dailyTrainTicket.setRwPrice(BigDecimal.ZERO);
                dailyTrainTicket.setYw(0);
                dailyTrainTicket.setYwPrice(BigDecimal.ZERO);
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                dailyTrainTicketMapper.insert(dailyTrainTicket);
            }
        }
    }

}
