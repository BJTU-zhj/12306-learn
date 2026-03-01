package com.jiawa.train.business.DTO;

import com.jiawa.train.common.DTO.PageDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DailyTrainStationAllDTO extends PageDTO {

    @NotBlank(message = "【车次编号】不能为空")
    private String trainCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "【日期】不能为空")
    private Date date;


}
