package com.jiawa.train.business.mapper.custom;

import java.util.Date;

public interface SkTokenCustomMapper {

    int decrease(Date  date,String trainCode,int decreaseCount);

}