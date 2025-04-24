package com.jwt.implementation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceHistoryDTO {

    private LocalDate date;
    private BigDecimal price;

    public PriceHistoryDTO() {}

    public PriceHistoryDTO(LocalDate date, BigDecimal price) {
        this.date = date;
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
