package com.jiawa.train.business.DTO;

import com.jiawa.train.common.DTO.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class DailyTrainTicketQueryDTO extends PageDTO implements Serializable {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String trainCode;

    private String start;

    private String end;


}
