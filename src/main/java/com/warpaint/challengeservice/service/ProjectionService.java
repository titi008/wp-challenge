package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Run 1000 simulations for the given months to project future prices
 *
 * @author tiborszucs
 */
@Service
@Slf4j
@AllArgsConstructor
public class ProjectionService {

    private static final double NUMBER_OF_SIMULATION = 1000;
    private static final Random RANDOM = new Random();

    private final ChangeCalculator changeCalculator;

    private final PricingHistoryByMonthFilter monthFilter;

    /**
     * Gives future close prices for the given number of months.
     * Returns a list of 3 element: max, medium, min prices of the last month for 1000 simulations.
     *
     * @param numberOfMonths Number of month to calculate into the future
     * @param pricingHistory Pricing history day-by-day
     * @return List containing pricing in the following order: {max, med, min}
     */
    public List<Pricing> projectedPricing(int numberOfMonths, List<Pricing> pricingHistory) {
        log.debug("Calculate prices for the next {} months", numberOfMonths);
        List<Pricing> pricingHistoryByMonth = monthFilter.filterByLastBusinessDayOfMonth(pricingHistory);

        Map<LocalDate, BigDecimal> monthOverMonthChanges =
                changeCalculator.calculateChangeHistory(pricingHistoryByMonth);

        LocalDate lastCalculatedDate = LocalDate.now().plusMonths(numberOfMonths);

        List<Double> projectedCloseValues = projectedCloseValues(
                new ArrayList<>(monthOverMonthChanges.values()),
                numberOfMonths,
                getLastClosePrice(pricingHistoryByMonth));

        return Arrays.asList(
                toPricing(projectedCloseValues.get(0), lastCalculatedDate),
                toPricing(projectedCloseValues.get(1), lastCalculatedDate),
                toPricing(projectedCloseValues.get(2), lastCalculatedDate)
        );
    }

    private BigDecimal getLastClosePrice(List<Pricing> pricingHistoryByMonth) {
        return pricingHistoryByMonth.get(pricingHistoryByMonth.size() - 1).getClosePrice();
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
     * Do the projection over several simulation using Monte Carlo Simulation
     *
     * @param priceChanges Month over month price changes from the historical data
     * @param numberOfMonths Number of months to calculate
     * @param lastPrice Last available price from the history
     * @return List of 3 elements with this order: {max, med, min}
     */
    private List<Double> projectedCloseValues(List<BigDecimal> priceChanges,
                                              int numberOfMonths,
                                              BigDecimal lastPrice) {
        double monthlyVolatility = getMonthlyVolatility(priceChanges);

        Set<Double> lastProjectedMonthCloseValues = new HashSet<>();

        // Simulate Monte Carlo forecast
        for (int i = 0; i < NUMBER_OF_SIMULATION; i++) {
            lastProjectedMonthCloseValues.add(monteCarlo(numberOfMonths, lastPrice, monthlyVolatility).getLast());
        }

        return getStatisticalValues(lastProjectedMonthCloseValues);
    }

    /**
     * Return the max, med, min close values of the given set
     *
     * @param lastProjectedMonthCloseValues Projected prices
     * @return List of 3 elements with this order: {max, med, min}
     */
    private List<Double> getStatisticalValues(Set<Double> lastProjectedMonthCloseValues) {
        List<Double> sortedValues = new ArrayList<>(lastProjectedMonthCloseValues);
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
     * Monte Carlo Simulation
     *
     * @param numberOfMonths Number of months for calculation
     * @param lastPrice Last available price from the history
     * @param monthlyVolatility Monthly volatility
     * @return Prices in the future for the given month
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
     * Calculate next price with a random number
     *
     * @param monthlyVolatility Monthly volatility
     * @param lastPrice Last calculated price
     * @return The next price
     */
    private double calculateNextPrice(double monthlyVolatility, double lastPrice) {
        return lastPrice * (1 + (RANDOM.nextGaussian() * monthlyVolatility));
    }
}
