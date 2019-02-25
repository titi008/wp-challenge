package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class ChallengeService {

    public static final int DEFAULT_PREDICTABLE_MONTHS = 240;

    private final PricingHistoryDataProvider pricingHistoryDataProvider;

    private final PredictionService predictionService;

    public List<Pricing> getHistoricalAssetData(Asset asset,
                                                Optional<LocalDate> startDate,
                                                Optional<LocalDate> endDate) {
        log.info("Fetching historical price data");

        return pricingHistoryDataProvider.getPricingHistory(asset, startDate, endDate);
    }

    public List<Pricing> getProjectedAssetData(Asset asset, Optional<Integer> numberOfMonthsOptional) {
        log.info("Generating projected price data");

        int numberOfMonths = numberOfMonthsOptional.orElse(DEFAULT_PREDICTABLE_MONTHS);

        List<Pricing> pricingHistoryLastFiveYears = pricingHistoryDataProvider.getPricingHistory(asset);

        return predictionService.predictPricing(numberOfMonths, pricingHistoryLastFiveYears);
    }
}
