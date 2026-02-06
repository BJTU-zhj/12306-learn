package com.jiawa.train.member.DTO;

import lombok.Data;

@Data
public class PaseengerQueryDTO {

    public Long memberId;


    @Override
    public String toString() {
        return "PaseengerQueryDTO{" +
                "memberId=" + memberId +
                '}';
    }
}
