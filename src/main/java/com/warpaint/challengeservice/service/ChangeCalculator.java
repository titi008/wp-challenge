package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.model.Pricing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calculate percentage of price changes
 *
 * @author tiborszucs
 */
@Service
@Slf4j
public class ChangeCalculator {

    /**
     * Calculate percentage of price changes
     *
     * @param pricingHistory Pricing history. The list must be sorted ascending by the trading date
     * @return Map of change value and the trade date
     */
    public Map<LocalDate, BigDecimal> calculateChangeHistory(List<Pricing> pricingHistory) {
        log.debug("Calculate percentage of price changes");
        Map<LocalDate, BigDecimal> changes = new TreeMap<>();

        for (int i = 1; i < pricingHistory.size(); i++) {
            Pricing current = pricingHistory.get(i);
            Pricing previous = pricingHistory.get(i - 1);

            BigDecimal change = current.getClosePrice()
                    .divide(previous.getClosePrice(), 6, RoundingMode.HALF_EVEN)
                    .add(BigDecimal.valueOf(-1.0));
            changes.put(current.getTradeDate(), change);
        }

        return changes;
    }
}
