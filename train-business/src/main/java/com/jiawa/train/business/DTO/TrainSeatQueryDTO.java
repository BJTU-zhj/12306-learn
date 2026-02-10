package com.jiawa.train.business.DTO;

import com.jiawa.train.common.DTO.PageDTO;

public class TrainSeatQueryDTO extends PageDTO {



    String trainCode;

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    @Override
    public String toString() {
        return "TrainSeatQueryDTO{" +
                "trainCode='" + trainCode + '\'' +
                '}';
    }
}
