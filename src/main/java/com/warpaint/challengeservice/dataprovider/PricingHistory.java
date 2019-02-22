package com.warpaint.challengeservice.dataprovider;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PricingHistory {

    @JsonProperty("Date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("Open")
    private BigDecimal open;

    @JsonProperty("High")
    private BigDecimal high;

    @JsonProperty("Low")
    private BigDecimal low;

    @JsonProperty("Close")
    private BigDecimal close;

    @JsonProperty("Adj Close")
    private BigDecimal adjClose;

    @JsonProperty("Volume")
    private BigDecimal volume;
}
