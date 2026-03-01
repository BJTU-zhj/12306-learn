package com.jiawa.train.business.service;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.jiawa.train.business.DTO.ConfirmOrderDoDTO;
import com.jiawa.train.business.DTO.ConfirmOrderMQDTO;
import com.jiawa.train.business.domain.ConfirmOrder;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.enums.RocketMQTopicEnum;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.mapper.custom.ConfirmOrderCustomMapper;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BeforeConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);


    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private SkTokenService skTokenService;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private ConfirmOrderCustomMapper confirmOrderCustomMapper;

    //引入sentinel进行限流
    @SentinelResource(value = "beforeDoConfirm",blockHandler = "doConfirmBlock")
    public Long beforeConfirmOrder(ConfirmOrderDoDTO confirmOrderDoDTO) {
        //验证码校验
//        String imageCodeToken=confirmOrderDoDTO.getImageCodeToken();
//        RBucket<String> bucket = redissonClient.getBucket(imageCodeToken);
//        String imageCodeCorret = bucket.get();
//        LOG.info("当前验证码：{}", imageCodeCorret);
//        LOG.info("传输进来的验证码：{}", confirmOrderDoDTO.getImageCode());
//        if(StrUtil.isEmpty(imageCodeCorret)){
//            LOG.info("验证码已过期，请重新请求并验证");
//            throw new BusinessException(BusinessExceptionEnum.BUSINESS_IMAGE_CODE_TIME_OUT);
//        }
//        else{
//            if(!imageCodeCorret.equals(confirmOrderDoDTO.getImageCode().toLowerCase())){
//                LOG.info("验证码错误，请重新输入");
//                throw new BusinessException(BusinessExceptionEnum.BUSINESS_IMAGE_CODE_ERROR);
//            }else{
//                //移除验证码
//                bucket.delete();
//            }
//        }



//        //添加令牌校验
        boolean isTokenValid =skTokenService.getToken(confirmOrderDoDTO.getDate(),confirmOrderDoDTO.getTrainCode(), LoginMemberContext.getId());
        if(isTokenValid){
            LOG.info("令牌校验通过");
        }else{
            throw new BusinessException(BusinessExceptionEnum.BUSINESS_TICKET_TOKEN_IS_EMPTY);
        }



        //保存订单信息，设置状态为初始
        DateTime now=DateTime.now();
        Date date=confirmOrderDoDTO.getDate();
        String trainCode=confirmOrderDoDTO.getTrainCode();
        String start=confirmOrderDoDTO.getStart();
        String end=confirmOrderDoDTO.getEnd();

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
        confirmOrderMapper.insert(confirmOrder);

        //放消息给下订单购票
        ConfirmOrderMQDTO confirmOrderMQDTO = new ConfirmOrderMQDTO();
        confirmOrderMQDTO.setDate(date);
        confirmOrderMQDTO.setTrainCode(trainCode);
        confirmOrderMQDTO.setLogId(MDC.get("LOG_ID"));
        String respJson= JSON.toJSONString(confirmOrderMQDTO);
        LOG.info("排队购票，开始发送mq消息:{}", respJson);
        rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER_TOPIC.getCode(), respJson);
        LOG.info("排队购票，结束发送mq消息");

        return confirmOrder.getId();
    }


    //查询订单状态，如果处于初始化和待处理状态，则返回排队数，否则返回其他状态码
    public int queryLineQueue(Long confirmOrderId){
        //首先查数据库中订单的状态
        ConfirmOrder confirmOrder = confirmOrderMapper.selectByPrimaryKey(confirmOrderId);
        ConfirmOrderStatusEnum statusEnum = EnumUtil.getBy(ConfirmOrderStatusEnum::getCode,confirmOrder.getStatus());
        int result=switch (statusEnum){
            case PENDING -> 0; // 待处理，要排队
            case SUCCESS -> -1; // 成功
            case FAILURE -> -2; // 失败
            case EMPTY -> -3; // 无票
            case CANCEL -> -4; // 取消
            case INIT -> 999;//初始化，要排队
        };
        //针对初始化和待处理状态的订单需要查询排队数
        if(result==999){
            Date currOrderCreateTime = confirmOrder.getCreateTime();
            Date date = confirmOrder.getDate();
            String trainCode = confirmOrder.getTrainCode();
            result=confirmOrderCustomMapper.queryLineQueue(date,trainCode,confirmOrderId,currOrderCreateTime);
        }
        return result;
    }


    //sentinel对于确认订单函数这个资源的限流后的处理
    public Long doConfirmBlock(ConfirmOrderDoDTO confirmOrderDoDTO, BlockException e){
        LOG.info("doConfirm方法被限流");
        throw new BusinessException(BusinessExceptionEnum.BUSINESS_SENTINEL);
    }
}
