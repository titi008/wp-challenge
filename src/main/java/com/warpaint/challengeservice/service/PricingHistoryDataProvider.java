package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.dataprovider.DividendHistory;
import com.warpaint.challengeservice.dataprovider.PricingHistory;
import com.warpaint.challengeservice.dataprovider.YahooFinanceClient;
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
     * Get pricing history without providing date range
     *
     * @param symbol Asset symbol
     * @return Pricing history for the last 5 years
     */
    public List<Pricing> getPricingHistory(String symbol) throws Exception {
        LocalDate now = LocalDate.now();
        return getPricingHistory(symbol,
                dateRangeProvider.getProjectionStartDate(now),
                now);
    }

    /**
     * Get pricing history for the given interval
     *
     * @param symbol Asset symbol
     * @param startDate Start Date
     * @param endDate End Date
     * @return Pricing history between dates
     */
    public List<Pricing> getPricingHistory(String symbol, LocalDate startDate, LocalDate endDate)
            throws Exception {
        List<PricingHistory> priceData = dataProvider.fetchPriceData(symbol, startDate, endDate);
        List<DividendHistory> dividendData = dataProvider.fetchDividendData(symbol, startDate, endDate);

        return priceData.stream().map(pricingHistory -> bindDividendToPricing(dividendData, pricingHistory))
                .sorted(Comparator.comparing(Pricing::getTradeDate)).collect(Collectors.toList());
    }

    private Pricing bindDividendToPricing(List<DividendHistory> dividendData, PricingHistory pricingHistory) {
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
    }
}
