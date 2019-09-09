package com.dl.task.model;

import lombok.Data;

@Data
public class CountBasketBaseInfo {
    private Integer orderDetailId;
    private String  orderSn;
    private String ticketData;
    private Integer changCiId;
    private String playCode;
    private String score;
    private String rangFen;
    private String forecastScore;
}
