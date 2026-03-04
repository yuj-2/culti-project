package com.culti.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformancePriceDTO {
    private Long contentId; 
    private Integer vipPrice;
    private Integer rPrice;
    private Integer sPrice;
    private Integer aPrice;
}
