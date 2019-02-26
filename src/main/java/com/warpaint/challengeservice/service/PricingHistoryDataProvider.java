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

    private final YahooFinanceClient dataProvider;

    private final DateRangeProvider dateRangeProvider;

    /**
     * Get pricing history without providing date range. The default value of years is 5.
     *
     * @param asset Asset symbol
     * @return Pricing history for the last 5 years
     */
    public List<Pricing> getPricingHistory(Asset asset) throws Exception {
        LocalDate now = LocalDate.now();
        return getPricingHistory(asset,
                dateRangeProvider.getProjectionStartDate(now),
                now);
    }

    /**
     * Get pricing history for the given interval. 5 years by default if intervals are empty
     *
     * @param asset
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Pricing> getPricingHistory(Asset asset, LocalDate startDate, LocalDate endDate)
            throws Exception {
        // TODO By Tibi: Bean validation instead of these methods

        List<PricingHistory> priceData = dataProvider.fetchPriceData(asset.getSymbol(), startDate, endDate);
        List<DividendHistory> dividendData = dataProvider.fetchDividendData(asset.getSymbol(), startDate, endDate);

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
}
