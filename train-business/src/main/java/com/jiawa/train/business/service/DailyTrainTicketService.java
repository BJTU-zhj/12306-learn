package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainTicketQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainTicketSaveDTO;
import com.jiawa.train.business.VO.DailyTrainTicketQueryVO;
import com.jiawa.train.business.domain.DailyTrain;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.domain.DailyTrainTicketExample;
import com.jiawa.train.business.domain.TrainStation;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.enums.TrainTypeEnum;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

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
    //使用springboot的cache+redis
    @Cacheable(value = "DailyTrainTicketService.queryList")
    public PageVO<DailyTrainTicketQueryVO> queryList(DailyTrainTicketQueryDTO dailyTrainTicketQueryDTO){
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.setOrderByClause("id desc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();
        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainTicketQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(dailyTrainTicketQueryDTO.getTrainCode());
        };
        if(ObjectUtil.isNotNull(dailyTrainTicketQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainTicketQueryDTO.getDate());
        };
        if(ObjectUtil.isNotEmpty(dailyTrainTicketQueryDTO.getStart())){
            criteria.andStartEqualTo(dailyTrainTicketQueryDTO.getStart());
        };
        if(ObjectUtil.isNotEmpty(dailyTrainTicketQueryDTO.getEnd())){
            criteria.andEndEqualTo(dailyTrainTicketQueryDTO.getEnd());
        }
        PageHelper.startPage(dailyTrainTicketQueryDTO.getPage(),dailyTrainTicketQueryDTO.getSize());
        List<DailyTrainTicket> dailyTrainTicketList =dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
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
    public void genDaily(Date date, String trainCode, DailyTrain dailyTrain){
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
            //记录里程数
            BigDecimal km = BigDecimal.ZERO;
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
                //票价=里程数*座位类型单价*车次类型系数
                //里程数
                km=km.add(trainStationList.get(endIndex).getKm());
                //获取车次类型系数
                String trainType=dailyTrain.getType();
                BigDecimal trainTypePrice= EnumUtil.getFieldBy(TrainTypeEnum::getPriceRate,TrainTypeEnum::getCode,trainType);
                //计算车票
                BigDecimal ydzPrice=km.multiply(SeatTypeEnum.YDZ.getPrice()).multiply(trainTypePrice).setScale(2, RoundingMode.HALF_UP);
                BigDecimal edzPrice=km.multiply(SeatTypeEnum.EDZ.getPrice()).multiply(trainTypePrice).setScale(2, RoundingMode.HALF_UP);
                BigDecimal rwPrice=km.multiply(SeatTypeEnum.RW.getPrice()).multiply(trainTypePrice).setScale(2, RoundingMode.HALF_UP);
                BigDecimal ywPrice=km.multiply(SeatTypeEnum.YW.getPrice()).multiply(trainTypePrice).setScale(2, RoundingMode.HALF_UP);
                dailyTrainTicket.setYdz(dailyTrainSeatService.countBySeatType(date, trainCode, SeatTypeEnum.YDZ.getCode()));
                dailyTrainTicket.setYdzPrice(ydzPrice);
                dailyTrainTicket.setEdz(dailyTrainSeatService.countBySeatType(date, trainCode, SeatTypeEnum.EDZ.getCode()));
                dailyTrainTicket.setEdzPrice(edzPrice);
                dailyTrainTicket.setRw(dailyTrainSeatService.countBySeatType(date, trainCode, SeatTypeEnum.RW.getCode()));
                dailyTrainTicket.setRwPrice(rwPrice);
                dailyTrainTicket.setYw(dailyTrainSeatService.countBySeatType(date, trainCode, SeatTypeEnum.YW.getCode()));
                dailyTrainTicket.setYwPrice(ywPrice);
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                dailyTrainTicketMapper.insert(dailyTrainTicket);
            }
        }
    }

    //根据唯一键查询记录
    public DailyTrainTicket queryByUnique(Date date,String trainCode,String start,String end){
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andStartEqualTo(start)
                .andEndEqualTo(end);
        List<DailyTrainTicket> dailyTrainTicketList = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
        if(CollUtil.isEmpty(dailyTrainTicketList)){
            return null;
        }
        return dailyTrainTicketList.get(0);
    }

}
