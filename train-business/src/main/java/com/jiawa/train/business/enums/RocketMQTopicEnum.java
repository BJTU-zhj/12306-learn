package com.jiawa.train.business.enums;

public enum RocketMQTopicEnum {

    CONFIRM_ORDER_TOPIC("CONFIRM_TRAIN_ORDER", "订单服务");

    private String code;

    private String desc;

    RocketMQTopicEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
