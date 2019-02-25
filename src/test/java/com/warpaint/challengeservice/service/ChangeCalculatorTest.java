package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ChangeCalculatorTest {

    @Test
    public void calculateChangeHistoryTest() {
        ChangeCalculator calculator = new ChangeCalculator();

        // Ordered list by trade date
        List<Pricing> history = new ArrayList<>();

        LocalDate date1 = LocalDate.of(2018, 10, 31);
        LocalDate date2 = LocalDate.of(2018, 11, 30);
        LocalDate date3 = LocalDate.of(2018, 12, 28);

        history.add(Pricing.builder()
                .closePrice(BigDecimal.valueOf(10.0)).tradeDate(date1)
                .build());
        history.add(Pricing.builder()
                .closePrice(BigDecimal.valueOf(15.0)).tradeDate(date2)
                .build());
        history.add(Pricing.builder()
                .closePrice(BigDecimal.valueOf(12.0)).tradeDate(date3)
                .build());

        Map<LocalDate, BigDecimal> changes = calculator.calculateChangeHistory(history);

        assertEquals(2, changes.size());
        assertEquals(BigDecimal.valueOf(0.5).setScale(6), changes.get(date2));
        assertEquals(BigDecimal.valueOf(-0.2).setScale(6), changes.get(date3));
    }
}