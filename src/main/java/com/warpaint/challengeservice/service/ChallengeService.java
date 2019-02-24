package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ChallengeService {

    private final PricingHistoryDataProvider pricingHistoryDataProvider;

    private final MonthOverMonthCalculator monthOverMonthCalculator;

    public List<Pricing> getHistoricalAssetData(Asset asset,
                                                Optional<LocalDate> startDate,
                                                Optional<LocalDate> endDate) {
        log.info("Fetching historical price data");
        // TODO Implement getHistoricalAssetData()

        return pricingHistoryDataProvider.getPricingHistory(asset, startDate, endDate);
    }

    public List<Pricing> getProjectedAssetData(Asset asset) {
        log.info("Generating projected price data");
        // TODO Implement getProjectedAssetData()

        List<Pricing> pricingHistoryLastFiveYears = pricingHistoryDataProvider.getPricingHistoryLastFiveYears(asset);

        Map<LocalDate, BigDecimal> monthOverMonthChanges =
                monthOverMonthCalculator.calculateMonthOverMonthChangeHistory(
                filterByLastBusinessDayOfMonth(pricingHistoryLastFiveYears));
        return null;
    }

    // TODO: By Tibi: Currently this algo includes the last available date as last business day of month even if it is not the last business day
    private List<Pricing> filterByLastBusinessDayOfMonth(List<Pricing> pricingHistory) {
        Map<YearMonth, Pricing> lastBusinessDayPricings = new TreeMap<>();

        pricingHistory.stream().forEach(pricing -> {
            LocalDate tradeDate = pricing.getTradeDate();
            YearMonth yearMonth = YearMonth.of(tradeDate.getYear(), tradeDate.getMonth());

            Pricing lastPricing = lastBusinessDayPricings.get(yearMonth);

            if (lastPricing == null || tradeDate.isAfter(lastPricing.getTradeDate())) {
                lastBusinessDayPricings.put(yearMonth, pricing);
            }
        });

        return lastBusinessDayPricings.values().stream()
                .sorted(Comparator.comparing(Pricing::getTradeDate))
                .collect(Collectors.toList());
    }

}
