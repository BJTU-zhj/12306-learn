package com.jiawa.train.common.VO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageVO<T> implements Serializable {

    private Long total;

    private List<T> list;

}
