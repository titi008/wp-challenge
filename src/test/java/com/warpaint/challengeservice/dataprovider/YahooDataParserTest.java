package com.warpaint.challengeservice.dataprovider;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class YahooDataParserTest {

    private final YahooDataParser yahooDataParser = new YahooDataParser();

    @Test
    public void parsePricingDataTest() {
        LocalDate date1 = LocalDate.parse("2017-01-01");
        LocalDate date2 = LocalDate.parse("2017-01-02");
        BigDecimal close1 = BigDecimal.valueOf(100);
        BigDecimal close2 = BigDecimal.valueOf(101);
        List<String> lines = Arrays.asList(
                "Date,Open,High,Low,Close,Adj Close,Volume",
                date1 + ",1,2,3," + close1 + ",5,6",
                date2 + ",1,2,3," + close2 + ",11,12",
                "wrongDate,1,2,3,4,5,6",
                "1,2,3,4,5", // invalid number of columns
                date2 + ",1,2,3,wrongClose,5,6");
        List<PricingHistory> pricingHistories = yahooDataParser.parsePricingData(lines);

        assertEquals(2, pricingHistories.size());

        assertEquals(PricingHistory.builder()
                        .date(date1)
                        .open(BigDecimal.valueOf(1.0))
                        .high(BigDecimal.valueOf(2.0))
                        .low(BigDecimal.valueOf(3.0))
                        .close(BigDecimal.valueOf(close1.doubleValue()))
                        .adjClose(BigDecimal.valueOf(5.0))
                        .volume(BigDecimal.valueOf(6.0))
                        .build(),
                pricingHistories.get(0));

        assertEquals(PricingHistory.builder()
                        .date(date2)
                        .open(BigDecimal.valueOf(1.0))
                        .high(BigDecimal.valueOf(2.0))
                        .low(BigDecimal.valueOf(3.0))
                        .close(BigDecimal.valueOf(close2.doubleValue()))
                        .adjClose(BigDecimal.valueOf(11.0))
                        .volume(BigDecimal.valueOf(12.0))
                        .build(),
                pricingHistories.get(1));
    }

    @Test
    public void parseDividendDataTest() {
        LocalDate date1 = LocalDate.parse("2017-01-01");
        LocalDate date2 = LocalDate.parse("2017-01-02");
        BigDecimal dividend1 = BigDecimal.valueOf(0.1);
        BigDecimal dividend2 = BigDecimal.valueOf(0.24);

        List<String> lines = Arrays.asList(
                "Date,Dividend",
                date1 + "," + dividend1,
                date2 + "," + dividend2,
                "wrongDate,1",
                "1",  // invalid number of columns
                date2 + ",wrongDividend");

        List<DividendHistory> dividendHistories = yahooDataParser.parseDividendData(lines);

        assertEquals(2, dividendHistories.size());

        assertEquals(DividendHistory.builder()
                        .date(date1)
                        .dividend(dividend1)
                        .build(),
                dividendHistories.get(0));

        assertEquals(DividendHistory.builder()
                        .date(date2)
                        .dividend(dividend2)
                        .build(),
                dividendHistories.get(1));
    }
}