package com.warpaint.challengeservice.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PricingHistoryDateRangeProvider implements DateRangeProvider {

    public static final int DEFAULT_HISTORY_YEARS = 5;
    public static final int PROJECTION_YEARS = 5;

    /**
     * Check that start date is before end date
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return True if startDate is before endDate. Throws IllegalArgumentException otherwise
     */
    @Override
    public boolean validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date is before start date");
        }

        return true;
    }

    /**
     * Validate startDate. Return now - 5 years if not presents
     *
     * @param startDate Start date
     * @param now Current date
     */
    @Override
    public LocalDate validateStartDate(Optional<LocalDate> startDate, LocalDate now) {
        if (!startDate.isPresent()) {
            return getDefaultStartDate(now);
        }

        LocalDate date = startDate.get();

        if (date.isAfter(now)) {
            throw new IllegalArgumentException("Start date is after current date");
        }

        return date;
    }

    /**
     * Validate endDate
     *
     * @param endDate End date
     * @param now Current date
     */
    @Override
    public LocalDate validateEndDate(Optional<LocalDate> endDate, LocalDate now) {
        if (!endDate.isPresent() || endDate.get().isAfter(now)) {
            return now;
        }

        return endDate.get();
    }

    @Override
    public LocalDate getProjectionStartDate(LocalDate now) {
        return now.minusYears(PROJECTION_YEARS);
    }

    private LocalDate getDefaultStartDate(LocalDate now) {
        return now.minusYears(DEFAULT_HISTORY_YEARS);
    }
}
