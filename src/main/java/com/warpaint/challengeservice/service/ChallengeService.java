package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.dataprovider.PricingHistory;
import com.warpaint.challengeservice.dataprovider.YahooFinanceClient;
import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ChallengeService {

    private final YahooFinanceClient dataProvider;

    public List<Pricing> getHistoricalAssetData(Asset asset,
                                                Optional<LocalDate> startDate,
                                                Optional<LocalDate> endDate) {
        log.info("Fetching historical price data");
        // TODO Implement getHistoricalAssetData()

        LocalDate now = LocalDate.now();
        // TODO: By Tibi: Also need to check if startDate is before endDate
        LocalDate validaStartDate = validateStartDate(startDate, now);
        LocalDate validEndDate = validateEndDate(endDate, now);

        List<PricingHistory> priceData = dataProvider.fetchPriceData(asset.getSymbol(), validaStartDate, validEndDate);

        return priceData.stream().map(pricingHistory ->
            Pricing.builder()
                    .closePrice(pricingHistory.getClose())
                    .dividend(BigDecimal.ZERO)
                    .highPrice(pricingHistory.getHigh())
                    .lowPrice(pricingHistory.getLow())
                    .openPrice(pricingHistory.getOpen())
                    .tradeDate(pricingHistory.getDate())
                    .build()
        ).collect(Collectors.toList());
    }

    public List<Pricing> getProjectedAssetData(Asset asset) {
        log.info("Generating projected price data");
        // TODO Implement getProjectedAssetData()
        return null;
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
