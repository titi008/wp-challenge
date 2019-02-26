package com.warpaint.challengeservice.dataprovider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class DividendHistory {

    private LocalDate date;

    private BigDecimal dividend;
}
