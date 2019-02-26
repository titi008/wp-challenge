package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PricingHistoryByMonthFilter {

    /**
     * Return the last pricing for every month. The last pricing is on the last business day of the month.
     * This filtering also returns the last data from the pricingHistory even if it is not the last business day of the month
     *
     * @param pricingHistory Day-by-day pricing history
     * @return Filtered list by the last business day of every month. The list is sorted ascending by the trade date
     */
    public List<Pricing> filterByLastBusinessDayOfMonth(List<Pricing> pricingHistory) {
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
}
