package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.ConfirmOrderDoDTO;
import com.jiawa.train.business.DTO.ConfirmOrderQueryDTO;
import com.jiawa.train.business.DTO.ConfirmOrderSaveDTO;
import com.jiawa.train.business.VO.ConfirmOrderQueryVO;
import com.jiawa.train.business.domain.ConfirmOrder;
import com.jiawa.train.business.domain.ConfirmOrderExample;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ConfirmOrderService {

    private static final Logger LOG= LoggerFactory.getLogger(ConfirmOrderService.class);

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

        //选座

            //一个车厢一个车厢查找合适座位
        //事务处理
            //修改每日座位德 sell字段
            //修改每日车票的余票信息
            //更新订单状态信息为成功
            //为会员增加购票记录

    }

}
