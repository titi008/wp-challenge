package com.warpaint.challengeservice.dataprovider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class YahooDataParser {

    public static final int PAYLOAD_START_INDEX = 1;

    /**
     * Parse pricing history content
     *
     * @param lines Pricing history lines containing header at first line
     * @return List of {@link PricingHistory}
     */
    public List<PricingHistory> parsePricingData(List<String> lines) {
        List<PricingHistory> pricingHistories = lines.stream().skip(PAYLOAD_START_INDEX)
                .map(line -> line.split(","))
                .filter(columns -> columns.length == 7)
                .map(this::parsePricingHistory).filter(this::validatePricingHistory).collect(Collectors.toList());

        return pricingHistories;
    }

    /**
     * Parse dividend history content
     *
     * @param lines Dividend history lines containing header at first line
     * @return List of {@link DividendHistory}
     */
    public List<DividendHistory> parseDividendData(List<String> lines) {
        List<DividendHistory> dividendHistories = lines.stream().skip(1)
                .map(line -> line.split(","))
                .filter(columns -> columns.length == 2)
                .map(this::parseDividendHistory).filter(this::validateDividendHistory).collect(Collectors.toList());

        return dividendHistories;
    }

    private boolean validatePricingHistory(PricingHistory pricingHistory) {
        return pricingHistory.getDate() != null
                && pricingHistory.getOpen().intValue() != -1
                && pricingHistory.getHigh().intValue() != -1
                && pricingHistory.getLow().intValue() != -1
                && pricingHistory.getClose().intValue() != -1
                && pricingHistory.getAdjClose().intValue() != -1
                && pricingHistory.getVolume().intValue() != -1;
    }

    private PricingHistory parsePricingHistory(String[] columns) {

        PricingHistory pricingHistory = PricingHistory.builder()
                .date(parseTradeDate(columns[0]))
                .open(parseDecimalValue(columns[1]))
                .high(parseDecimalValue(columns[2]))
                .low(parseDecimalValue(columns[3]))
                .close(parseDecimalValue(columns[4]))
                .adjClose(parseDecimalValue(columns[5]))
                .volume(parseDecimalValue(columns[6]))
                .build();

        return pricingHistory;
    }

    private DividendHistory parseDividendHistory(String[] columns) {
        DividendHistory dividendHistory = DividendHistory.builder()
                .date(parseTradeDate(columns[0]))
                .dividend(parseDecimalValue(columns[1]))
                .build();

        return dividendHistory;
    }

    private boolean validateDividendHistory(DividendHistory dividendHistory) {
        return dividendHistory.getDate() != null
                && dividendHistory.getDividend().intValue() != -1;
    }

    private BigDecimal parseDecimalValue(String value) {
        try {
            return BigDecimal.valueOf(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return new BigDecimal(-1);
        }
    }

    private LocalDate parseTradeDate(String tradeDate) {
        try {
            return LocalDate.parse(tradeDate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
