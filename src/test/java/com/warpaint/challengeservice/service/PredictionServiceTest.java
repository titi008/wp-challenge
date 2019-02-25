package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PredictionServiceTest {

    @Test
    public void predictPricing() {
        PredictionService predictionService = new PredictionService(new ChangeCalculator());

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

        List<Pricing> predictedPricings = predictionService.predictPricing(120, history);

        assertEquals(3, predictedPricings.size());

        Pricing max = predictedPricings.get(0);
        Pricing med = predictedPricings.get(1);
        Pricing min = predictedPricings.get(2);

        assertTrue(min.getClosePrice().doubleValue() < med.getClosePrice().doubleValue());
        assertTrue(med.getClosePrice().doubleValue() < max.getClosePrice().doubleValue());
    }
}