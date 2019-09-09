package com.dl.task.model;

import lombok.Data;

@Data
public class JsonResultBasketball {
    private Integer orderDetailId;
    private String  orderSn;
    private String ticketData;
    private Integer changciId;
    private String playCode;
    private String hdcResult;
    private String hiloResult;
    private String mnlResult;
    private String wnmResult;
}
