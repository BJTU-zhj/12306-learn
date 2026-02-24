package com.jiawa.train.member.DTO;

import com.jiawa.train.common.DTO.PageDTO;
import lombok.Data;

@Data
public class TicketQueryDTO extends PageDTO {

    private Long memberId;

    @Override
    public String toString() {
        return "TicketQueryDTO{" +
                "memberId=" + memberId +
                '}';
    }
}
