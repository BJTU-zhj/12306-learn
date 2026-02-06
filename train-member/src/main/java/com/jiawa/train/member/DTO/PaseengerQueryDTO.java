package com.jiawa.train.member.DTO;

import com.jiawa.train.common.DTO.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaseengerQueryDTO extends PageDTO {

    public Long memberId;


    @Override
    public String toString() {
        return "PaseengerQueryDTO{" +
                "memberId=" + memberId +
                '}';
    }
}
