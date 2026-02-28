package com.jiawa.train.business.enums;

public enum RedisKeyPreEnum {

    CONFIRM_ORDER_LOCK("confirm_order_lock_", "订单确认锁"),
    TICKET_TOKEN_LOCK("ticket_token_lock_", "抢令牌锁"),
    TICKET_TOKEN_CACHE("ticket_token_number_cache_", "令牌数量缓存");


    private String code;

    private String desc;

    RedisKeyPreEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
