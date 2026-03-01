package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.*;
import com.jiawa.train.business.VO.ConfirmOrderQueryVO;
import com.jiawa.train.business.domain.*;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.enums.RedisKeyPreEnum;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ConfirmOrderService {

    private static final Logger LOG= LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;


    //redission的分布式锁
    @Resource
    private RedissonClient redissonClient;

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
    @Resource
    private SkTokenService skTokenService;

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
    //引入sentinel进行限流
//    @SentinelResource(value = "doConfirm",blockHandler = "doConfirmBlock")
    public  void doConfirm(ConfirmOrderMQDTO confirmOrderMQDTO){



//        //使用redis的分布式锁setnx
//        Boolean lock=stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1",10, TimeUnit.MILLISECONDS);
//        // 获取线程名称 (压测时最建议看这个，对应 Tomcat 的线程池)
//        String threadName = Thread.currentThread().getName();
//        if(lock){
//            LOG.info("恭喜，已经获取锁,线程号:{}", threadName);
//        }else{
//            LOG.info("锁被占用，请稍后再试,线程号:{}", threadName);
//            throw new BusinessException(BusinessExceptionEnum.BUSINESS_LOCK_IS_BUSY);
//        }
        String trainCode=confirmOrderMQDTO.getTrainCode();
        Date date=confirmOrderMQDTO.getDate();
        String lockKey= RedisKeyPreEnum.CONFIRM_ORDER_LOCK.getCode()+date+"-"+trainCode;
        RLock lock = null;
        try{
            lock = redissonClient.getLock(lockKey);
            //使用redission的分布式锁，非看门狗模式
            //三个参数，第一个参数是获取锁的等待时间，第二个参数是锁的过期时间，第三个参数是时间单位
//            boolean tryLock=lock.tryLock(0, 5, TimeUnit.SECONDS);
            //两个参数，第一个参数是获取锁的等待时间，第二个是时间单位-----看门狗模式，守护线程，主线程结束才会结束，否则一直重置锁的时间
            boolean tryLock=lock.tryLock(0, TimeUnit.SECONDS);
            if(tryLock){
                LOG.info("恭喜，已经获取锁,线程号:{}", Thread.currentThread().getName());
            }else {
                LOG.info("锁被占用，请稍后再试,线程号:{}", Thread.currentThread().getName());
//                throw new BusinessException(BusinessExceptionEnum.BUSINESS_LOCK_IS_BUSY);
                return;
            }

            //测试是否是看们狗模式
//            for (int i = 0; i < 30; i++){
//                LOG.info("循环中，线程号:{}", Thread.currentThread().getName());
//                Thread.sleep(1000);
//                LOG.info("当前锁的剩余过期时间:{}", lock.remainTimeToLive());
//            }

            //批量处理获取锁的车次订单
            while (true){
                //首先查询处于初始化状态的订单
                ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
                //保证了按顺序出票
                confirmOrderExample.setOrderByClause("id asc");
                ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();
                criteria.andDateEqualTo(date).andTrainCodeEqualTo(trainCode)
                        .andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode());
                PageHelper.startPage(1,10);
                List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExampleWithBLOBs(confirmOrderExample);
                if(CollUtil.isEmpty(confirmOrderList)){
                    LOG.info("没有找到待处理订单");
                    break;
                }else{
                    LOG.info("本次处理{}条订单", confirmOrderList.size());
                }
                //逐个订单进行选座购票,如果某个订单购票失败，则跳过该订单，继续处理下一个订单
                for (ConfirmOrder confirmOrder : confirmOrderList){
                    try {
                        orderSell(confirmOrder);
                    }catch (BusinessException e){
                        //针对没库存抛出的异常进行处理
                        if(e.getBusinessExceptionEnum()==BusinessExceptionEnum.BUSINESS_TICKET_NOT_ENOUGH){
                            updateOrderStatus(confirmOrder,ConfirmOrderStatusEnum.EMPTY);
                        }else{
                            throw e;
                        }
                    }
                }
            }

        }catch (Exception e){
            LOG.error("订单确认异常逻辑异常", e);
        }finally {
            //删除锁，而且在finally中执行，防止锁没有被删除，对于上边逻辑代码中如果出错的时候，锁没有被删除的情况，
            // 但是依旧存在上边逻辑如果卡住，锁超时，导致其他线程获取锁从而出现得到超卖现象
            if(lock!=null&&lock.isHeldByCurrentThread()){
                LOG.info("释放锁，线程号:{}", Thread.currentThread().getName());
                lock.unlock();
            }
        }
    }

    //新增一个orderSell函数
    public void orderSell(ConfirmOrder confirmOrder) {

        //为了测试排队功能，休眠1秒
//        try {
//            Thread.sleep(10000);
//        }catch (InterruptedException e){
//            LOG.error("睡眠异常！", e);
//        }

        //构造订单参数
        ConfirmOrderDoDTO confirmOrderDoDTO = new ConfirmOrderDoDTO();
        confirmOrderDoDTO.setMemberId(confirmOrder.getMemberId());
        confirmOrderDoDTO.setDate(confirmOrder.getDate());
        confirmOrderDoDTO.setTrainCode(confirmOrder.getTrainCode());
        confirmOrderDoDTO.setStart(confirmOrder.getStart());
        confirmOrderDoDTO.setEnd(confirmOrder.getEnd());
        confirmOrderDoDTO.setDailyTrainTicketId(confirmOrder.getDailyTrainTicketId());
        String ticketsJson = confirmOrder.getTickets();
        List<ConfirmOrderTicketDTO> confirmOrderTicketList = JSON.parseArray(ticketsJson, ConfirmOrderTicketDTO.class);
        confirmOrderDoDTO.setTickets(confirmOrderTicketList);

        //将当前订单的状态修改为处理中
        updateOrderStatus(confirmOrder,ConfirmOrderStatusEnum.PENDING);

        Date date=confirmOrder.getDate();
        String trainCode=confirmOrder.getTrainCode();
        String start=confirmOrder.getStart();
        String end=confirmOrder.getEnd();

        //省略业务数据校验、如车次是否存在、余票是否存在、车次是否在有效期内，以及是否同车次重复购买等

        //最终选座结果
        List<DailyTrainSeat> lastSelectSeatList = new ArrayList<>();

        //查询余票库存
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.queryByUnique(date, trainCode, start, end);
        LOG.info("查询到的余票信息：{}", dailyTrainTicket);
        //减库存，预减，检验合法性
        //遍历本次请求的全部车票-------这里如果库存不足会抛出异常
        ticketEnoughCheck(confirmOrderDoDTO, dailyTrainTicket);

        //计算本次所有车票对与第一张车票的相对偏移值
        List<Integer> absoluteToFirstTicket= new ArrayList<>();
        List<Integer> offsetsToFirstTicket= new ArrayList<>();
        List<ConfirmOrderTicketDTO> tickets = confirmOrderDoDTO.getTickets();
        if(ObjectUtil.isNull(tickets.get(0).getSeat())){
            LOG.info("本次请求属于无法选座类型，无需计算偏移值");
            for (ConfirmOrderTicketDTO ticket : tickets){
                getSeat(lastSelectSeatList,date, trainCode, ticket.getSeatTypeCode(),null,null,dailyTrainTicket);
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
                offsetsToFirstTicket.add(absoluteToFirstTicket.get(i) - absoluteToFirstTicket.get(0));
            }
            LOG.info("本次请求的相对位序：{}", offsetsToFirstTicket);
            getSeat(lastSelectSeatList,date, trainCode, tickets.get(0).getSeatTypeCode(),tickets.get(0).getSeat().split("")[0],offsetsToFirstTicket,dailyTrainTicket);
        }

        LOG.info("本次请求的选座结果：{}", lastSelectSeatList);


        //事务处理
        //修改每日座位德 sell字段
        //修改每日车票的余票信息
        //更新订单状态信息为成功
        //为会员增加购票记录
        try {
            afterConfirmOrderService.batchOrderTicketsUpdate(dailyTrainTicket,lastSelectSeatList,confirmOrder);
        } catch (Exception e) {
            LOG.error("分布式事务订票业务写库异常");
            throw new BusinessException(BusinessExceptionEnum.BUSINESS_ORDER_EXCEPTION);
        }
    }




    //一个车厢一个车厢查找合适座位
    public void getSeat(List<DailyTrainSeat> lastSelectSeatList,Date date, String trainCode, String seatType,String column,List<Integer> offsetsToFirstTicket,DailyTrainTicket dailyTrainTicket){
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageService.selectBySeatType(date,trainCode,seatType);
        LOG.info("查找到的符合车厢数：{}", dailyTrainCarriageList.size());
        for (DailyTrainCarriage dailyTrainCarriage : dailyTrainCarriageList){
            LOG.info("开始处理车厢：{}", dailyTrainCarriage.getIndex());
            //本车厢是否可能找到全部的座位
            Boolean isFindAllCarriage = true;
            List<DailyTrainSeat> dailyTrainSeatList = dailyTrainSeatService.selectByDateTrainCodeCarriage(date,trainCode,dailyTrainCarriage.getIndex());
            LOG.info("{}车厢查找到的符合座位数：{}",dailyTrainCarriage.getIndex() , dailyTrainSeatList.size());
            for (DailyTrainSeat dailyTrainSeat : dailyTrainSeatList){
                //以本座位为首的选座是否成功
                Boolean isFindAllSeat=true;
                //本座位是否已经被选择
                Boolean isSeatUsed = false;
                LOG.info("开始处理{}车厢的座位：{}", dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex());
                //首先看下是否这个座位已经选择过
                for(DailyTrainSeat lastSelectSeat : lastSelectSeatList){
                    if(Objects.equals(lastSelectSeat.getId(), dailyTrainSeat.getId())){
                        LOG.info("{}车厢的{}号座位已被选择，无法重复选择", dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex());
                        isSeatUsed = true;
                        break;
                    }
                }
                if(isSeatUsed){
                    continue;
                }
                //根据column区分选座与不选座两种情况
                //选座情况
                if(StrUtil.isNotBlank(column)){
                    LOG.info("开始处理选座情况,当前座位的列为：{}，目标座位的列为：{}", dailyTrainSeat.getCol(),column);
                    //选座时首先判断列数是否符合
                    //符合情况
                    if(dailyTrainSeat.getCol().equals(column)){
                        //第一位满足了列还要满足sell
                        Boolean isSell =calSell(dailyTrainTicket,dailyTrainSeat);
                        //如果可售
                        if(isSell){
                            //对比后边的偏移座位，有一个不符合的就首页后移重新来过
                            for (int i=1;i<offsetsToFirstTicket.size();i++){
                                Integer aimIndex=offsetsToFirstTicket.get(i)+dailyTrainSeat.getCarriageSeatIndex()-1;
                                LOG.info("{}车厢{}号座作为首个座位达标！当前要探索的座位号为：{}", dailyTrainSeat.getCarriageIndex(), dailyTrainSeat.getCarriageSeatIndex(),aimIndex+1);
                                //如果当前偏移座位已经超出车厢座位数，则应该终止，找下一个车厢，这样做会让选座保持在统一车厢
                                if(aimIndex>=dailyTrainSeatList.size()){
                                    LOG.info("{}车厢{}号座作为首个座位已经让该车厢不可能探索成功！", dailyTrainSeat.getCarriageIndex(), dailyTrainSeat.getCarriageSeatIndex());
                                    isFindAllCarriage = false;
                                    break;
                                }
                                DailyTrainSeat aimDailyTrainSeat = dailyTrainSeatList.get(aimIndex);
                                Boolean aimIsSell =calSell(dailyTrainTicket,aimDailyTrainSeat);
                                //如果不可售，则首个座位后移重新探索
                                if(!aimIsSell){
                                    isFindAllSeat = false;
                                    break;
                                }
                            }
                            //查看标志位，决定下一步结果
                            //如果isFindAllCarriage为false说明，本车厢不可能找到满足条件的座位了，应跳过车厢
                            if(!isFindAllCarriage){
                                break;
                            }
                            //本车厢有希望
                            else{
                                //isFindAllSeat为true代表上边的偏移循环安然退出，座位全部满足
                                if(isFindAllSeat){
                                    LOG.info("座位全部找到！！当前首个座位是{}车厢{}号座位，后续偏移是{}", dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex(),offsetsToFirstTicket);
                                    //清空原本的结果记录并赋值
//                                    lastSelectSeatList = new ArrayList<>();
                                    for (int i=0;i<offsetsToFirstTicket.size();i++)
                                        lastSelectSeatList.add(dailyTrainSeatList.get(offsetsToFirstTicket.get(i)+dailyTrainSeat.getCarriageSeatIndex()-1));
                                    //TODO 修改数据库
                                    return;
                                }else{
                                    LOG.info("以{}车厢{}号座为首座的探索失败，首座后移",dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex());
                                    continue;
                                }

                            }
                        }
                        //不可售
                        else{
                            continue;
                        }
                    }
                    //列不符合情况
                    else{
                        continue;
                    }
                }
                else {
                    LOG.info("开始处理不选座情况");
                    //首先判断是否sell可售
                    Boolean isSell =calSell(dailyTrainTicket,dailyTrainSeat);
                    if(isSell){
                        LOG.info("不选座--座位可售！！当前座位是{}车厢{}号座位", dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex());
                        lastSelectSeatList.add(dailyTrainSeat);
                        //todo 修改数据库
                        return;
                    }else{
                        LOG.info("不选座--座位不可售！！当前座位是{}车厢{}号座位", dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex());
                        continue;
                    }
                }
            }
        }
    }

    /**
     * 计算某座位在区间内是否可卖,由getSeat函数调用
     * 例：sell=10001，本次购买区间站1~4，则区间已售000
     * 全部是0，表示这个区间可买；只要有1，就表示区间内已售过票
     *
     * 选中后，要计算购票后的sell，比如原来是10001，本次购买区间站1~4
     * 方案：构造本次购票造成的售卖信息01110，和原sell 10001按位与，最终得到11111
     */
    public boolean calSell(DailyTrainTicket dailyTrainTicket,DailyTrainSeat dailyTrainSeat){
        //例如：00001
        String sell=dailyTrainSeat.getSell();
        String startStation=dailyTrainTicket.getStart();
        Integer startIndex=dailyTrainTicket.getStartIndex();
        String endStation=dailyTrainTicket.getEnd();
        Integer endIndex=dailyTrainTicket.getEndIndex();
        LOG.info("startStation:{},startIndex:{},endStation:{},endIndex:{}",startStation,startIndex,endStation,endIndex);
        //截取原先的sell,假设startIndex~endIndex是1~3，则000
        String subSell=sell.substring(startIndex,endIndex);
        if(Integer.parseInt(subSell)>0){
            LOG.info("{}车厢的座位{}在，{}站~{}站已售，不可购买", dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex(),startStation,endStation);
            return false;
        }else{
            LOG.info("{}车厢的座位{}在，{}站~{}站可售，可购买", dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex(),startStation,endStation);
            //000->111
            String curSell=subSell.replace("0", "1");
            //111->01110
            curSell= StrUtil.fillBefore(curSell, '0', endIndex);
            curSell= StrUtil.fillAfter(curSell, '0', sell.length());
            //与原sell按位或 00001|01110->01111,但是newSell只有11111，所以要补全
            int newSellInt= NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
            String newSell= NumberUtil.getBinaryStr(newSellInt);
            newSell= StrUtil.fillBefore(newSell, '0', sell.length());
            dailyTrainSeat.setSell(newSell);
            LOG.info("{}车厢的座位{}的sell变化过程为:{}->{}->{}",dailyTrainSeat.getCarriageIndex(),dailyTrainSeat.getCarriageSeatIndex(),sell,curSell,newSell);
            return true;
        }


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

    //修改订单状态
    public void updateOrderStatus(ConfirmOrder confirmOrder,ConfirmOrderStatusEnum confirmOrderStatusEnum) {
        ConfirmOrderExample confirmOrderExample=new ConfirmOrderExample();
        confirmOrderExample.createCriteria().andIdEqualTo(confirmOrder.getId());
        confirmOrder.setStatus(confirmOrderStatusEnum.getCode());
        confirmOrderMapper.updateByExampleSelective(confirmOrder,confirmOrderExample);
    }


    //取消排队并设置状态
    public int cancelOrderLine(Long confirmOrderId){
        ConfirmOrderExample confirmOrderExample=new ConfirmOrderExample();
        ConfirmOrderExample.Criteria criteria=confirmOrderExample.createCriteria();
        criteria.andIdEqualTo(confirmOrderId).andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode());
        ConfirmOrder confirmOrder=new ConfirmOrder();
        confirmOrder.setStatus(ConfirmOrderStatusEnum.CANCEL.getCode());
        return confirmOrderMapper.updateByExampleSelective(confirmOrder,confirmOrderExample);
    }



}
