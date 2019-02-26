package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.dataprovider.DividendHistory;
import com.warpaint.challengeservice.dataprovider.PricingHistory;
import com.warpaint.challengeservice.dataprovider.YahooFinanceClient;
import com.warpaint.challengeservice.model.Pricing;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

public class PricingHistoryDataProviderTest {

    private final String SYMBOL = "LOGM";
    
    @InjectMocks
    private PricingHistoryDataProvider historyDataProvider;

    @Mock
    private YahooFinanceClient client;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getPricingHistory() throws Exception {
        LocalDate date1 = LocalDate.of(2018, 10, 31);
        LocalDate date2 = LocalDate.of(2018, 10, 30);
        LocalDate date3 = LocalDate.of(2018, 10, 29);

        LocalDate startDate = date3;
        LocalDate endDate = date1;

        BigDecimal dividend = BigDecimal.valueOf(1.23);

        List<PricingHistory> pricingHistory = Arrays.asList(
                PricingHistory.builder().date(date3).build(),
                PricingHistory.builder().date(date2).build(),
                PricingHistory.builder().date(date1).build()
        );

        List<DividendHistory> dividendHistories = Arrays.asList(
                DividendHistory.builder().date(date2).dividend(dividend).build()
        );

        doReturn(pricingHistory).when(client).fetchPriceData(SYMBOL, startDate, endDate);
        doReturn(dividendHistories).when(client).fetchDividendData(SYMBOL, startDate, endDate);

        List<Pricing> pricings = historyDataProvider.getPricingHistory(SYMBOL, startDate, endDate);

        assertEquals(3, pricings.size());

        assertEquals(pricings.get(0).getTradeDate(), date3);
        assertEquals(pricings.get(0).getDividend(), BigDecimal.ZERO);

        assertEquals(pricings.get(1).getTradeDate(), date2);
        assertEquals(pricings.get(1).getDividend(), dividend);

        assertEquals(pricings.get(2).getTradeDate(), date1);
        assertEquals(pricings.get(2).getDividend(), BigDecimal.ZERO);
    }
}