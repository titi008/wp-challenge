package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PricingHistoryByMonthFilterTest {

    @Test
    public void filterByLastBusinessDayOfMonth() {
        PricingHistoryByMonthFilter filter = new PricingHistoryByMonthFilter();

        Pricing pricing1 = Pricing.builder().tradeDate(LocalDate.of(2018, 11, 30)).build();
        Pricing pricing2 = Pricing.builder().tradeDate(LocalDate.of(2018, 11, 1)).build();
        Pricing pricing3 = Pricing.builder().tradeDate(LocalDate.of(2018, 10, 31)).build();
        Pricing pricing4 = Pricing.builder().tradeDate(LocalDate.of(2018, 10, 30)).build();
        Pricing pricing5 = Pricing.builder().tradeDate(LocalDate.of(2018, 11, 29)).build();

        List<Pricing> pricingByDays = Arrays.asList(pricing1,pricing2, pricing3, pricing4, pricing5);

        List<Pricing> filteredList = filter.filterByLastBusinessDayOfMonth(pricingByDays);

        assertEquals(2, filteredList.size());

        assertEquals("2018-10-31 must be the first element", pricing3, filteredList.get(0));
        assertEquals("2018-11-30 must be the second element", pricing1, filteredList.get(1));
    }
}