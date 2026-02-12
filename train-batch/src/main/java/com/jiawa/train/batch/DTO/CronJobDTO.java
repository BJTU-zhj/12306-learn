package com.jiawa.train.batch.DTO;

import lombok.Data;

@Data
public class CronJobDTO {

    private String group;
    private String name;
    private String description;
    private String cronExpression;

    @Override
    public String toString() {
        return "CronJobDTO{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                '}';
    }
}
