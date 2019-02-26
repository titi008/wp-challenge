package com.warpaint.challengeservice.service;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PricingHistoryDateRangeProviderTest {

    private final PricingHistoryDateRangeProvider dateRangeProvider = new PricingHistoryDateRangeProvider();

    @Test
    public void validateDateRangeTest() {
        LocalDate now = LocalDate.now();
        boolean validDateRange = dateRangeProvider.validateDateRange(now.minusYears(5), now);

        assertTrue(validDateRange);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateDateRangeFailsTest() {
        LocalDate now = LocalDate.now();
        dateRangeProvider.validateDateRange(now, now.minusYears(5));
    }

    @Test
    public void validateStartDateTest() {
        LocalDate now = LocalDate.now();
        Optional<LocalDate> startDate = Optional.of(now.minusYears(5));
        LocalDate date = dateRangeProvider.validateStartDate(startDate, now);
        assertEquals(startDate.get(), date);
    }

    @Test
    public void validateStartDateEmptyTest() {
        LocalDate now = LocalDate.now();
        Optional<LocalDate> startDate = Optional.empty();
        LocalDate date = dateRangeProvider.validateStartDate(startDate, now);
        assertEquals(now.minusYears(5), date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateStartDateIsAfterNowTest() {
        LocalDate now = LocalDate.now();
        Optional<LocalDate> startDate = Optional.of(now.plusYears(5));
        dateRangeProvider.validateStartDate(startDate, now);
    }

    @Test
    public void validateEndDateTest() {
        LocalDate now = LocalDate.now();
        Optional<LocalDate> endDate = Optional.of(now.minusDays(1));
        LocalDate date = dateRangeProvider.validateEndDate(endDate, now);
        assertEquals(endDate.get(), date);
    }

    @Test
    public void validateEndDateEmptyTest() {
        LocalDate now = LocalDate.now();
        Optional<LocalDate> endDate = Optional.empty();
        LocalDate date = dateRangeProvider.validateEndDate(endDate, now);
        assertEquals(now, date);
    }

    @Test
    public void validateEndDateIsAfterNowTest() {
        LocalDate now = LocalDate.now();
        Optional<LocalDate> endDate = Optional.of(now.plusDays(1));
        LocalDate date = dateRangeProvider.validateEndDate(endDate, now);
        assertEquals(now, date);
    }

    @Test
    public void getProjectionStartDateTest() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = dateRangeProvider.getProjectionStartDate(now);
        assertEquals(now.minusYears(PricingHistoryDateRangeProvider.PROJECTION_YEARS), startDate);
    }
}