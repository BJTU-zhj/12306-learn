package com.jiawa.train.business.DTO;

import com.jiawa.train.common.DTO.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

//@Data注解会帮忙重新生成hashCode和equals方法，注意父类属性
@EqualsAndHashCode(callSuper = true)
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
                "} " + super.toString();
    }
}
