package com.jiawa.train.common.exception;

public enum BusinessExceptionEnum {

    MEMBER_MOBILE_EXIST("手机号已经存在"),
    MEMBER_MOBILE_NOT_EXIST("请点击发送验证码"),
    MOMBER_CODE_ERROR("验证码错误"),
    BUSINESS_TRAIN_ALREADY_EXIST("该火车已录入"),
    BUSINESS_STATION_ALREADY_EXIST("该火车站已录入"),
    BUSINESS_TRAIN_STATION_NAME_ALREADY_EXIST("该火车该站点已录入"),
    BUSINESS_TRAIN_STATION_INDEX_ALREADY_EXIST("该火车该站序已录入"),
    BUSINESS_TRAIN_CARRIAGE_ALREADY_EXIST("该火车该厢号已录入"),
    BUSINESS_TICKET_NOT_ENOUGH("余票不足"),
    BUSINESS_ORDER_EXCEPTION("服务器忙，请稍后重试"),
    BUSINESS_LOCK_IS_BUSY("抢票人数太多，锁被占用请重试"),
    BUSINESS_SENTINEL("抢票人数太多，被限流请重试"),
    BUSINESS_TICKET_TOKEN_IS_EMPTY("令牌校验失败！"),
    BUSINESS_TICKET_TOKEN_LOCK_IS_BUSY("请勿频繁提交！"),
    BUSINESS_TICKET_TOKEN_CACHE_ZERO("令牌库存已无令牌！"),
    BUSINESS_IMAGE_CODE_TIME_OUT("图片验证码已过期"),
    BUSINESS_IMAGE_CODE_ERROR("图片验证码错误"),;



    private String desc;

    BusinessExceptionEnum(String desc){
        this.desc = desc;
    }

    public String getDesc(){
        return desc;
    }

    public static String getDescByCode(String code){
        for(BusinessExceptionEnum item : BusinessExceptionEnum.values()){
            if(item.name().equalsIgnoreCase(code)){
                return item.getDesc();
            }
        }
        return "";
    }

}
