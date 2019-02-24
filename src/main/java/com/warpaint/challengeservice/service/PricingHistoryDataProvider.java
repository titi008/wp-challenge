package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.dataprovider.DividendHistory;
import com.warpaint.challengeservice.dataprovider.PricingHistory;
import com.warpaint.challengeservice.dataprovider.YahooFinanceClient;
import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PricingHistoryDataProvider {

    private final YahooFinanceClient dataProvider;

    public List<Pricing> getPricingHistoryLastFiveYears(Asset asset) {
        return getPricingHistory(asset, Optional.empty(), Optional.empty());
    }

    public List<Pricing> getPricingHistory(Asset asset, Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate now = LocalDate.now();
        // TODO: By Tibi: Also need to check if startDate is before endDate
        LocalDate validaStartDate = validateStartDate(startDate, now);
        LocalDate validEndDate = validateEndDate(endDate, now);

        List<PricingHistory> priceData = dataProvider.fetchPriceData(asset.getSymbol(), validaStartDate, validEndDate);
        List<DividendHistory> dividendData = dataProvider.fetchDividendData(asset.getSymbol(), validaStartDate, validEndDate);

        return priceData.stream().map(pricingHistory -> {
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
     * Validate startDate
     *
     * @param startDate
     * @param now
     */
    private LocalDate validateStartDate(Optional<LocalDate> startDate, LocalDate now) {
        if (!startDate.isPresent()) {
            return now.minusYears(5);
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
     * @param endDate
     * @param now
     */
    private LocalDate validateEndDate(Optional<LocalDate> endDate, LocalDate now) {
        if (!endDate.isPresent()) {
            return now;
        }

        if (!endDate.get().isAfter(now)) {
            return now;
        }

        return endDate.get();
    }
}
