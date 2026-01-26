package com.jiawa.train.common.exception;

public enum BusinessExceptionEnum {

    MEMBER_MOBILE_EXIST("手机号已经存在");

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
