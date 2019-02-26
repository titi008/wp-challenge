package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Run 1000 simulations for the given months to predict future prices
 *
 * @author tiborszucs
 */
@Service
@Slf4j
@AllArgsConstructor
// TODO By Tibi: Rename every Prediction to Projected
public class PredictionService {

    // TODO Create a provider and inject here
    private static final double NUMBER_OF_SIMULATION = 1000;

    private final ChangeCalculator changeCalculator;

    /**
     * Predicts future close prices for the given number of months.
     * Returns a list of 3 element: max, medium, min prices of the last predicted month for 1000 simulations.
     *
     * @param numberOfMonths Number of month to predict into the future
     * @param pricingHistory Pricing history day-by-day
     * @return List containing pricing in the following order: {max, med, min}
     */
    public List<Pricing> predictPricing(int numberOfMonths, List<Pricing> pricingHistory) {
        log.debug("Calculate predicted pricest for the next {} months", numberOfMonths);
        // TODO By Tibi: Expand to class and write test
        List<Pricing> pricingHistoryByMonth = filterByLastBusinessDayOfMonth(pricingHistory);

        Map<LocalDate, BigDecimal> monthOverMonthChanges =
                changeCalculator.calculateChangeHistory(pricingHistoryByMonth);

        LocalDate lastPredictedDate = LocalDate.now().plusMonths(numberOfMonths);

        List<Double> predictedCloseValues = predictCloseValues(
                new ArrayList<>(monthOverMonthChanges.values()),
                numberOfMonths,
                // TODO Move to a method
                pricingHistoryByMonth.get(pricingHistoryByMonth.size() - 1).getClosePrice());

        return Arrays.asList(
                toPricing(predictedCloseValues.get(0), lastPredictedDate),
                toPricing(predictedCloseValues.get(1), lastPredictedDate),
                toPricing(predictedCloseValues.get(2), lastPredictedDate)
        );
    }

    /**
     * Convert close value to Pricing
     *
     * @param closeValue Close value
     * @param date Date
     * @return Pricing containing the close value
     */
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

    /**
     * Return the last pricing for every month. The last pricing is on the last business day of the month.
     * This filtering also returns the last data from the pricingHistory even if it is not the last business day of the month
     *
     * @param pricingHistory Day-by-day pricing history
     * @return Filtered list by the last business day of every month. The list is sorted ascending by the trade date
     */
    private List<Pricing> filterByLastBusinessDayOfMonth(List<Pricing> pricingHistory) {
        Map<YearMonth, Pricing> lastBusinessDayPricings = new HashMap<>();

        pricingHistory.forEach(pricing -> {
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

    /**
     * Do the prediction over several simulation using Monte Carlo Simulation
     *
     * @param priceChanges Month over month price changes from the historical data
     * @param numberOfMonths Number of predictable months
     * @param lastPrice Last available price from the history
     * @return
     */
    private List<Double> predictCloseValues(List<BigDecimal> priceChanges,
                                            int numberOfMonths,
                                            BigDecimal lastPrice) {
        double monthlyVolatility = getMonthlyVolatility(priceChanges);

        Set<Double> lastPredictedMonthCloseValues = new HashSet<>();

        // Simulate Monte Carlo forecast
        for (int i = 0; i < NUMBER_OF_SIMULATION; i++) {
            lastPredictedMonthCloseValues.add(monteCarlo(numberOfMonths, lastPrice, monthlyVolatility).getLast());
        }

        return getStatisticalValues(lastPredictedMonthCloseValues);
    }

    /**
     * Return the max, med, min close values of the given set
     *
     * @param lastPredictedMonthCloseValues Predicted prices
     * @return List of 3 elements with this order: {max, med, min}
     */
    private List<Double> getStatisticalValues(Set<Double> lastPredictedMonthCloseValues) {
        List<Double> sortedValues = lastPredictedMonthCloseValues.stream().collect(Collectors.toList());
        sortedValues.sort(Comparator.naturalOrder());

        Double max = sortedValues.get(sortedValues.size() - 1);
        Double med = sortedValues.get((sortedValues.size() - 1) / 2);
        Double min = sortedValues.get(0);

        return Arrays.asList(max, med, min);
    }

    /**
     * Calculate standard deviation as monthly volatility
     *
     * @param changesByMonth Month by month changes
     * @return Monthly volatility
     */
    private double getMonthlyVolatility(List<BigDecimal> changesByMonth) {
        double mean = changesByMonth.stream().mapToDouble(BigDecimal::doubleValue).average().getAsDouble();
        return Math.sqrt(
                changesByMonth.stream()
                        .mapToDouble(value -> Math.pow(value.doubleValue() - mean, 2)).average().getAsDouble());
    }

    /**
     * Monte Carlo Simulation to predict the price
     *
     * @param numberOfMonths Number of predictable months
     * @param lastPrice Last available price from the history
     * @param monthlyVolatility Monthly volatility
     * @return Predicted price in the future for the given month
     */
    private LinkedList<Double> monteCarlo(int numberOfMonths, BigDecimal lastPrice, double monthlyVolatility) {
        LinkedList<Double> prices = new LinkedList<>();

        double price = calculateNextPrice(monthlyVolatility, lastPrice.doubleValue());

        prices.add(limitPriceToNonNegativeValue(price));

        while (prices.size() < numberOfMonths) {
            double nextPrice = calculateNextPrice(monthlyVolatility, prices.getLast());
            prices.add(limitPriceToNonNegativeValue(nextPrice));
        }

        return prices;
    }

    private double limitPriceToNonNegativeValue(double price) {
        return price < 0 ? 0 : price;
    }

    /**
     * Calculate next predicted price with a random number
     *
     * @param monthlyVolatility Monthly volatility
     * @param lastPrice Last calculated price
     * @return
     */
    private double calculateNextPrice(double monthlyVolatility, double lastPrice) {
        // TODO Inject this random instance, do not reinstantiate every time
        Random random = new Random();
        return lastPrice * (1 + (random.nextGaussian() * monthlyVolatility));
    }
}
