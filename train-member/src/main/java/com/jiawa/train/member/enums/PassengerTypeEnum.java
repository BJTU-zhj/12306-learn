package com.jiawa.train.member.enums;

import lombok.Getter;

@Getter
public enum PassengerTypeEnum {

    ADULT("1", "成人"),
    CHILD("2", "儿童"),
    STUDENT("3", "学生");

    private String code;
    private String desc;

    PassengerTypeEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
