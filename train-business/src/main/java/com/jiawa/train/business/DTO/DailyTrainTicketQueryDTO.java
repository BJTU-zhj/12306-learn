package com.jiawa.train.business.DTO;

import com.jiawa.train.common.DTO.PageDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DailyTrainTicketQueryDTO extends PageDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String trainCode;

    private String start;

    private String end;

    @Override
    public String toString() {
        return "DailyTrainTicketQueryDTO{" +
                "date=" + date +
                ", trainCode='" + trainCode + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                '}';
    }
}
