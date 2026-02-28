package com.jiawa.train.business.mq;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.minlog.Log;
import com.jiawa.train.business.DTO.ConfirmOrderDoDTO;
import com.jiawa.train.business.service.ConfirmOrderService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(topic = "CONFIRM_TRAIN_ORDER", consumerGroup = "default")
public class ConfirmOrderConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private ConfirmOrderService confirmOrderService;

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderConsumer.class);

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        Log.info("MQ接收消息:{}", new String(body));
        confirmOrderService.doConfirm(JSON.parseObject(new String(body), ConfirmOrderDoDTO.class));
    }
}
