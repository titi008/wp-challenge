package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.dataprovider.DividendHistory;
import com.warpaint.challengeservice.dataprovider.PricingHistory;
import com.warpaint.challengeservice.dataprovider.YahooFinanceClient;
import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provide pricing history for the given time range
 */
@Service
@AllArgsConstructor
public class PricingHistoryDataProvider {

    private static final int DEFAULT_HISTORY_YEARS = 5;

    private final YahooFinanceClient dataProvider;

    /**
     * Get pricing history without providing date range. The default value of years is 5.
     *
     * @param asset Asset symbol
     * @return Pricing history for the last 5 years
     */
    public List<Pricing> getPricingHistory(Asset asset) throws Exception {
        return getPricingHistory(asset, Optional.empty(), Optional.empty());
    }

    /**
     * Get pricing history for the given interval. 5 years by default if intervals are empty
     *
     * @param asset
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Pricing> getPricingHistory(Asset asset, Optional<LocalDate> startDate, Optional<LocalDate> endDate)
            throws Exception {
        LocalDate now = LocalDate.now();
        // TODO By Tibi: Bean validation instead of these methods
        LocalDate validStartDate = validateStartDate(startDate, now);
        LocalDate validEndDate = validateEndDate(endDate, now);

        validateDateRange(validStartDate, validEndDate);

        List<PricingHistory> priceData = dataProvider.fetchPriceData(asset.getSymbol(), validStartDate, validEndDate);
        List<DividendHistory> dividendData = dataProvider.fetchDividendData(asset.getSymbol(), validStartDate, validEndDate);

        return priceData.stream().map(pricingHistory -> {
            // TODO By Tibi: Expand to class and test binding
            Optional<DividendHistory> dividend = dividendData.stream()
                    .filter(dividendHistory -> dividendHistory.getDate().equals(pricingHistory.getDate()))
                    .findAny();
            return Pricing.builder()
                    .closePrice(pricingHistory.getClose())
                    .dividend(dividend.isPresent() ? dividend.get().getDividend() : BigDecimal.ZERO)
                    .highPrice(pricingHistory.getHigh())
                    .lowPrice(pricingHistory.getLow())
                    .openPrice(pricingHistory.getOpen())
                    .tradeDate(pricingHistory.getDate())
                    .build();
        }).sorted(Comparator.comparing(Pricing::getTradeDate)).collect(Collectors.toList());
    }

    /**
     * Check that start date is before end date
     *
     * @param startDate Start date
     * @param endDate   End date
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date is before start date");
        }
    }

    /**
     * Validate startDate. Return now - 5 years if not presents
     *
     * @param startDate Start date
     * @param now Current date
     */
    private LocalDate validateStartDate(Optional<LocalDate> startDate, LocalDate now) {
        if (!startDate.isPresent()) {
            return now.minusYears(DEFAULT_HISTORY_YEARS);
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
    private LocalDate validateEndDate(Optional<LocalDate> endDate, LocalDate now) {
        if (!endDate.isPresent() || endDate.get().isAfter(now)) {
            return now;
        }

        return endDate.get();
    }
}
