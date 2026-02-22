package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.ConfirmOrderDoDTO;
import com.jiawa.train.business.DTO.ConfirmOrderQueryDTO;
import com.jiawa.train.business.DTO.ConfirmOrderSaveDTO;
import com.jiawa.train.business.DTO.ConfirmOrderTicketDTO;
import com.jiawa.train.business.VO.ConfirmOrderQueryVO;
import com.jiawa.train.business.domain.*;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConfirmOrderService {

    private static final Logger LOG= LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    //两个常量，对应一等座和二等座计算偏移值时的map
    private static final Map<String,Integer> YDZ_OFFSET_MAP = new HashMap<>(
            Map.ofEntries(
                    Map.entry("A1",0),
                    Map.entry("C1",1),
                    Map.entry("D1",2),
                    Map.entry("F1",3),
                    Map.entry("A2",4),
                    Map.entry("C2",5),
                    Map.entry("D2",6),
                    Map.entry("F2",7)
            )
    );

    private static final Map<String,Integer> EDZ_OFFSET_MAP = new HashMap<>(
            Map.ofEntries(
                    Map.entry("A1",0),
                    Map.entry("B1",1),
                    Map.entry("C1",2),
                    Map.entry("D1",3),
                    Map.entry("F1",4),
                    Map.entry("A2",5),
                    Map.entry("B2",6),
                    Map.entry("C2",7),
                    Map.entry("D2",8),
                    Map.entry("F2",9)
            )
    );

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService ;

    //保存
    public void save(ConfirmOrderSaveDTO confirmOrderSaveDTO){
        DateTime now=DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(confirmOrderSaveDTO, ConfirmOrder.class);
        if(ObjectUtil.isEmpty(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        }else{
            confirmOrder.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", confirmOrder.getId());
            confirmOrderMapper.updateByPrimaryKey( confirmOrder);
        }
    }

    //查询列表
    public PageVO<ConfirmOrderQueryVO> queryList(ConfirmOrderQueryDTO confirmOrderQueryDTO){
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        PageHelper.startPage(confirmOrderQueryDTO.getPage(),confirmOrderQueryDTO.getSize());
        List<ConfirmOrder> confirmOrderList =confirmOrderMapper.selectByExample(confirmOrderExample);
        ;
        //固定用插件获取查询总数
        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);

        PageVO<ConfirmOrderQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    //保存确认订单信息
    public void doConfirm(ConfirmOrderDoDTO confirmOrderDoDTO){
        //省略业务数据校验、如车次是否存在、余票是否存在、车次是否在有效期内，以及是否同车次重复购买等
        DateTime now=DateTime.now();

        Date date=confirmOrderDoDTO.getDate();
        String trainCode=confirmOrderDoDTO.getTrainCode();
        String start=confirmOrderDoDTO.getStart();
        String end=confirmOrderDoDTO.getEnd();


        //保存订单信息，设置状态为初始
        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setId(SnowUtil.getSnowflakeId());
        confirmOrder.setMemberId(LoginMemberContext.getId());
        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);
        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        //这个是每日余票中的主键
        confirmOrder.setDailyTrainTicketId(confirmOrderDoDTO.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        confirmOrder.setCreateTime(now);
        confirmOrder.setUpdateTime(now);
        confirmOrder.setTickets(JSON.toJSONString(confirmOrderDoDTO.getTickets()));


        //查询余票库存
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.queryByUnique(date, trainCode, start, end);
        LOG.info("查询到的余票信息：{}", dailyTrainTicket);
        //减库存，预减，检验合法性
        //遍历本次请求的全部车票
        ticketEnoughCheck(confirmOrderDoDTO, dailyTrainTicket);

        //计算本次所有车票对与第一张车票的相对偏移值
        List<Integer> absoluteToFirstTicket= new ArrayList<>();
        List<Integer> offsetsToFirstTicket= new ArrayList<>();
        List<ConfirmOrderTicketDTO> tickets = confirmOrderDoDTO.getTickets();
        if(ObjectUtil.isNull(tickets.get(0).getSeat())){
            LOG.info("本次请求属于无法选座类型，无需计算偏移值");
            for (ConfirmOrderTicketDTO ticket : tickets){
                getSeat(date, trainCode, ticket.getSeatTypeCode(),null,null);
            }
        }else {
            LOG.info("本次请求属于可以选座类型，开始计算偏移值");
            //判断是一等座还是二等座的情景
            List<SeatColEnum> cols = SeatColEnum.getColsByType(tickets.get(0).getSeatTypeCode());
            Map<String,Integer> offsetMap = cols.size() == 4 ? YDZ_OFFSET_MAP : EDZ_OFFSET_MAP;
            //计算绝对位序
            for (ConfirmOrderTicketDTO ticket : tickets){
                String seat=ticket.getSeat();
                absoluteToFirstTicket.add(offsetMap.get(seat));
            }
            LOG.info("本次请求的绝对位序：{}", absoluteToFirstTicket);
            //计算相对位序
            offsetsToFirstTicket.add(0);
            for (int i = 1; i < absoluteToFirstTicket.size(); i++){
                offsetsToFirstTicket.add(absoluteToFirstTicket.get(i) - absoluteToFirstTicket.get(i-1));
            }
            getSeat(date, trainCode, tickets.get(0).getSeatTypeCode(),tickets.get(0).getSeat().split("")[0],offsetsToFirstTicket);
        }
        LOG.info("本次请求的相对位序：{}", offsetsToFirstTicket);
        //选座

            //一个车厢一个车厢查找合适座位
        //事务处理
            //修改每日座位德 sell字段
            //修改每日车票的余票信息
            //更新订单状态信息为成功
            //为会员增加购票记录

    }

    //一个车厢一个车厢查找合适座位
    public void getSeat(Date date, String trainCode, String seatType,String column,List<Integer> offsetsToFirstTicket){
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageService.selectBySeatType(date,trainCode,seatType);
        LOG.info("查找到的符合车厢数：{}", dailyTrainCarriageList.size());
        for (DailyTrainCarriage dailyTrainCarriage : dailyTrainCarriageList){
            LOG.info("开始处理车厢：{}", dailyTrainCarriage.getIndex());
            List<DailyTrainSeat> dailyTrainSeatList = dailyTrainSeatService.selectByDateTrainCodeCarriage(date,trainCode,dailyTrainCarriage.getIndex());
            LOG.info("{}车厢查找到的符合座位数：{}",dailyTrainCarriage.getIndex(), dailyTrainSeatList.size());
        }
        LOG.info("column:{}", column);
        LOG.info("offsetsToFirstTicket:{}", offsetsToFirstTicket);

    }

    //减库存，预减，检验合法性
    //遍历本次请求的全部车票
    private  void ticketEnoughCheck(ConfirmOrderDoDTO confirmOrderDoDTO, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketDTO ticket : confirmOrderDoDTO.getTickets()){
            //首先获取本车票选座的座位类型
            String seatTypeCode = ticket.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum= EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            //根据座位类型进行操作
            switch (seatTypeEnum){
                case YDZ -> {
                    if(dailyTrainTicket.getYdz()<=0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_TICKET_NOT_ENOUGH);
                    }
                    int ticketCount= dailyTrainTicket.getYdz()-1;
                    dailyTrainTicket.setYdz(ticketCount);
                }
                case EDZ -> {
                    if(dailyTrainTicket.getEdz()<=0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_TICKET_NOT_ENOUGH);
                    }
                    int ticketCount= dailyTrainTicket.getEdz()-1;
                    dailyTrainTicket.setEdz(ticketCount);
                }
                case RW -> {
                    if(dailyTrainTicket.getRw()<=0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_TICKET_NOT_ENOUGH);
                    }
                    int ticketCount= dailyTrainTicket.getRw()-1;
                    dailyTrainTicket.setRw(ticketCount);
                }
                case YW -> {
                    if(dailyTrainTicket.getYw()<=0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_TICKET_NOT_ENOUGH);
                    }
                    int ticketCount= dailyTrainTicket.getYw()-1;
                    dailyTrainTicket.setYw(ticketCount);
                }
            }
        }
    }

}
