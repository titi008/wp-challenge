package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProjectionServiceTest {

    @Test
    public void projectedPricingTest() {
        ProjectionService projectionService = new ProjectionService(
                new ChangeCalculator(), new PricingHistoryByMonthFilter());

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

        List<Pricing> pricings = projectionService.projectedPricing(120, history);

        assertEquals(3, pricings.size());

        Pricing max = pricings.get(0);
        Pricing med = pricings.get(1);
        Pricing min = pricings.get(2);

        assertTrue(min.getClosePrice().doubleValue() < med.getClosePrice().doubleValue());
        assertTrue(med.getClosePrice().doubleValue() < max.getClosePrice().doubleValue());
    }
}