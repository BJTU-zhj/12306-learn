package com.jiawa.train.business.DTO;

import com.jiawa.train.common.DTO.PageDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DailyTrainQueryDTO extends PageDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String code;

    @Override
    public String toString() {
        return "DailyTrainQueryDTO{" +
                "date=" + date +
                ", code='" + code + '\'' +
                '}';
    }
}
