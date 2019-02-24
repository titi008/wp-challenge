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

@Service
@Slf4j
public class MonthOverMonthCalculator {

    // TODO By Tibi: Maybe open price instead of close
    public Map<LocalDate, BigDecimal> calculateMonthOverMonthChangeHistory(List<Pricing> pricingHistoryByMonth) {
        Map<LocalDate, BigDecimal> changes = new TreeMap<>();

        for (int i = 1; i < pricingHistoryByMonth.size(); i++) {
            Pricing current = pricingHistoryByMonth.get(i);
            Pricing previous = pricingHistoryByMonth.get(i - 1);

            BigDecimal change = current.getClosePrice()
                    .divide(previous.getClosePrice(), 6, RoundingMode.HALF_EVEN)
                    .add(BigDecimal.valueOf(-1.0));
            changes.put(current.getTradeDate(), change);
        }

        return changes;
    }
}
