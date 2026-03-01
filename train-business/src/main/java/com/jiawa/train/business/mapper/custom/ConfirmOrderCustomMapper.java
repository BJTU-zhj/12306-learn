package com.jiawa.train.business.mapper.custom;

import java.util.Date;

public interface ConfirmOrderCustomMapper {

    int queryLineQueue(Date date, String trainCode,Long id,Date createTime);

}