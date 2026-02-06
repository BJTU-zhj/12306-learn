package com.jiawa.train.common.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PageDTO {

    @NotNull(message = "页码不能为空")
    private Integer page;

    @NotNull(message = "页大小不能为空")
    @Max(value = 100, message = "页大小不能超过100")
    private Integer size;

}
