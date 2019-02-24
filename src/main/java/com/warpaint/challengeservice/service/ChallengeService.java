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

    public static final int DEFAULT_PREDICTABLE_MONTHS = 240;

    private final PricingHistoryDataProvider pricingHistoryDataProvider;

    private final MonthOverMonthCalculator monthOverMonthCalculator;

    public List<Pricing> getHistoricalAssetData(Asset asset,
                                                Optional<LocalDate> startDate,
                                                Optional<LocalDate> endDate) {
        log.info("Fetching historical price data");
        // TODO Implement getHistoricalAssetData()

        return pricingHistoryDataProvider.getPricingHistory(asset, startDate, endDate);
    }

    public List<Pricing> getProjectedAssetData(Asset asset, Optional<Integer> numberOfMonthsOptional) {
        log.info("Generating projected price data");
        // TODO Implement getProjectedAssetData()

        int numberOfMonths = numberOfMonthsOptional.orElse(DEFAULT_PREDICTABLE_MONTHS);

        List<Pricing> pricingHistoryLastFiveYears = pricingHistoryDataProvider.getPricingHistoryLastFiveYears(asset);

        List<Pricing> pricingHistoryByMonth = filterByLastBusinessDayOfMonth(pricingHistoryLastFiveYears);
        Map<LocalDate, BigDecimal> monthOverMonthChanges =
                monthOverMonthCalculator.calculateMonthOverMonthChangeHistory(pricingHistoryByMonth);

        LocalDate lastPredictedDate = LocalDate.now().plusMonths(numberOfMonths);

        List<Double> predictedCloseValues = lastPredictedCloseValues(
                monthOverMonthChanges.values().stream().collect(Collectors.toList()),
                numberOfMonths,
                pricingHistoryByMonth.get(pricingHistoryByMonth.size() - 1));

        return Arrays.asList(
                toPricing(predictedCloseValues.get(0), lastPredictedDate),
                toPricing(predictedCloseValues.get(1), lastPredictedDate),
                toPricing(predictedCloseValues.get(2), lastPredictedDate)
        );
    }

    private Pricing toPricing(Double closeValue, LocalDate date) {
        return Pricing.builder()
                .closePrice(BigDecimal.valueOf(closeValue))
                .dividend(BigDecimal.ZERO)
                .highPrice(BigDecimal.ZERO)
                .lowPrice(BigDecimal.ZERO)
                .openPrice(BigDecimal.ZERO)
                .tradeDate(date)
                .build();
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

    private List<Double> lastPredictedCloseValues(List<BigDecimal> values, int numberOfMonths, Pricing lastPricing) {
        int numberOfSimulations = 1000;

        BigDecimal lastPrice = lastPricing.getClosePrice();

        // Calculate standard deviation as monthly volatility
        double mean = values.stream().mapToDouble(BigDecimal::doubleValue).average().getAsDouble();
        double monthlyVolatility = Math.sqrt(
                values.stream()
                        .mapToDouble(value -> Math.pow(value.doubleValue() - mean, 2)).average().getAsDouble());

        Set<Double> lastPredictedMonthCloseValues = new HashSet<>();

        // Simulate Monte Carlo forecast
        for (int i = 0; i < numberOfSimulations; i++) {
            lastPredictedMonthCloseValues.add(monteCarlo(numberOfMonths, lastPrice, monthlyVolatility).getLast());
        }

        List<Double> sortedValues = lastPredictedMonthCloseValues.stream().collect(Collectors.toList());
        sortedValues.sort(Comparator.naturalOrder());
        Double max = sortedValues.get(sortedValues.size() - 1);
        Double med = sortedValues.get((sortedValues.size() - 1) / 2);
        Double min = sortedValues.get(0);

        return Arrays.asList(max, med, min);
    }

    private LinkedList<Double> monteCarlo(int numberOfMonths, BigDecimal lastPrice, double monthlyVolatility) {
        LinkedList<Double> prices = new LinkedList<>();

        Random random = new Random();
        double price = lastPrice.doubleValue() * (1 + (random.nextGaussian() * monthlyVolatility));
        prices.add(price);

        while (prices.size() < numberOfMonths) {
            prices.add(prices.getLast() * (1 + (random.nextGaussian() * monthlyVolatility)));
        }

        return prices;
    }

}
