package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ChallengeService {

    private final PricingHistoryDataProvider pricingHistoryDataProvider;

    private final ProjectionService projectionService;

    public List<Pricing> getHistoricalAssetData(Asset asset,
                                                Optional<LocalDate> startDate,
                                                Optional<LocalDate> endDate) throws Exception {
        log.info("Fetching historical price data");

        return pricingHistoryDataProvider.getPricingHistory(asset, startDate, endDate);
    }

    public List<Pricing> getProjectedAssetData(Asset asset, int numberOfMonths) throws Exception {
        log.info("Generating projected price data");

        List<Pricing> pricingHistoryLastFiveYears = pricingHistoryDataProvider.getPricingHistory(asset);

        return projectionService.projectedPricing(numberOfMonths, pricingHistoryLastFiveYears);
    }
}
