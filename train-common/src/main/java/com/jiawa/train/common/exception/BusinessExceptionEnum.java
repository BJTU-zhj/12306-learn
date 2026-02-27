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
    BUSINESS_LOCK_IS_BUSY("锁被占用");

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
