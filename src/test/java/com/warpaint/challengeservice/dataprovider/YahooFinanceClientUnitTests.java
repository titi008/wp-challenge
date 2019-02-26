package com.warpaint.challengeservice.dataprovider;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class YahooFinanceClientUnitTests {

    @InjectMocks
    private YahooFinanceClient client;

    @Mock
    private YahooFinanceSession session;

    @Mock
    private HttpHandler httpHandler;

    @Spy
    private YahooDataParser yahooDataParser;

    private final String SYMBOL = "LOGM";
    private final LocalDate FROM = LocalDate.parse("2017-01-01");
    private final LocalDate TO = LocalDate.parse("2017-01-31");

    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        client.setSession(session);
    }

    @Test
    public void testFetchPriceData() throws IOException {
        LocalDate date1 = LocalDate.parse("2017-01-01");
        LocalDate date2 = LocalDate.parse("2017-01-02");
        BigDecimal close1 = BigDecimal.valueOf(100);
        BigDecimal close2 = BigDecimal.valueOf(101);

        String input = "Date,Open,High,Low,Close,Adj Close,Volume\n"
                     + date1 + ",1,2,3," + close1 + ",5,6\n"
                     + date2 + ",1,2,3," + close2 + ",11,12\n"
                     + "wrongDate,1,2,3,4,5,6\n"
                     + "1,2,3,4,5\n" // invalid number of columns
                     + date2 + ",1,2,3,wrongClose,5,6\n";

        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
        
        InputStream stream = new ByteArrayInputStream(input.getBytes());

        doReturn(entity).when(httpResponse).getEntity();
        doReturn(stream).when(entity).getContent();

        List<PricingHistory> dataSet = client.fetchPriceData(SYMBOL, FROM, TO);
        assertEquals(2, dataSet.size());

        assertEquals(PricingHistory.builder()
                        .date(date1)
                        .open(BigDecimal.valueOf(1.0))
                        .high(BigDecimal.valueOf(2.0))
                        .low(BigDecimal.valueOf(3.0))
                        .close(BigDecimal.valueOf(close1.doubleValue()))
                        .adjClose(BigDecimal.valueOf(5.0))
                        .volume(BigDecimal.valueOf(6.0))
                        .build(),
                dataSet.get(0));

        assertEquals(PricingHistory.builder()
                        .date(date2)
                        .open(BigDecimal.valueOf(1.0))
                        .high(BigDecimal.valueOf(2.0))
                        .low(BigDecimal.valueOf(3.0))
                        .close(BigDecimal.valueOf(close2.doubleValue()))
                        .adjClose(BigDecimal.valueOf(11.0))
                        .volume(BigDecimal.valueOf(12.0))
                        .build(),
                dataSet.get(1));
    }

    @Test(expected = IOException.class)
    public void testFetchPriceData_IOException() throws IOException {

        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());

        doReturn(entity).when(httpResponse).getEntity();
        doThrow(IOException.class).when(entity).getContent();

        client.fetchPriceData(SYMBOL, FROM, TO);
    }

    @Test
    public void testFetchPriceData_ExpiredSession() throws IOException {
        LocalDate date1 = LocalDate.parse("2017-01-01");
        LocalDate date2 = LocalDate.parse("2017-01-02");
        BigDecimal close1 = BigDecimal.valueOf(100);
        BigDecimal close2 = BigDecimal.valueOf(101);

        String input = "Date,Open,High,Low,Close,Adj Close,Volume\n"
                     + date1 + ",1,2,3," + close1 + ",5,6\n"
                     + date2 + ",1,2,3," + close2 + ",11,12\n";

        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
        doReturn(new BasicCookieStore()).when(httpHandler).getCookieStore();
        
        InputStream stream = new ByteArrayInputStream(input.getBytes());

        doReturn(entity).when(httpResponse).getEntity();
        doReturn(stream).when(entity).getContent();

        List<PricingHistory> dataSet = client.fetchPriceData(SYMBOL, FROM, TO);
        assertEquals(2, dataSet.size());

        assertEquals(PricingHistory.builder()
                        .date(date1)
                        .open(BigDecimal.valueOf(1.0))
                        .high(BigDecimal.valueOf(2.0))
                        .low(BigDecimal.valueOf(3.0))
                        .close(BigDecimal.valueOf(close1.doubleValue()))
                        .adjClose(BigDecimal.valueOf(5.0))
                        .volume(BigDecimal.valueOf(6.0))
                        .build(),
                dataSet.get(0));

        assertEquals(PricingHistory.builder()
                        .date(date2)
                        .open(BigDecimal.valueOf(1.0))
                        .high(BigDecimal.valueOf(2.0))
                        .low(BigDecimal.valueOf(3.0))
                        .close(BigDecimal.valueOf(close2.doubleValue()))
                        .adjClose(BigDecimal.valueOf(11.0))
                        .volume(BigDecimal.valueOf(12.0))
                        .build(),
                dataSet.get(1));
    }

    @Test
    public void testFetchDividendData() throws IOException {
        LocalDate date1 = LocalDate.parse("2017-01-01");
        LocalDate date2 = LocalDate.parse("2017-01-02");
        BigDecimal dividend1 = BigDecimal.valueOf(0.1);
        BigDecimal dividend2 = BigDecimal.valueOf(0.24);

        String input = "Date,Dividend\n"
                     + date1 + "," + dividend1 + "\n"
                     + date2 + "," + dividend2 + "\n"
                     + "wrongDate,1\n"
                     + "1\n"  // invalid number of columns
                     + date2 + ",wrongDividend";

        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
        
        InputStream stream = new ByteArrayInputStream(input.getBytes());

        doReturn(entity).when(httpResponse).getEntity();
        doReturn(stream).when(entity).getContent();

        List<DividendHistory> dataSet = client.fetchDividendData(SYMBOL, FROM, TO);
        assertEquals(2, dataSet.size());
        
        assertEquals(DividendHistory.builder()
                        .date(date1)
                        .dividend(dividend1)
                        .build(),
                dataSet.get(0));

        assertEquals(DividendHistory.builder()
                        .date(date2)
                        .dividend(dividend2)
                        .build(),
                dataSet.get(1));
    }

    @Test(expected = IOException.class)
    public void testFetchDividendData_IOException() throws IOException {

        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
        
        doReturn(entity).when(httpResponse).getEntity();
        doThrow(IOException.class).when(entity).getContent();

        client.fetchDividendData(SYMBOL, FROM, TO);
    }

    @Test
    public void testFetchDividendData_ExpiredSession() throws IOException {
        LocalDate date1 = LocalDate.parse("2017-01-01");
        LocalDate date2 = LocalDate.parse("2017-01-02");
        BigDecimal dividend1 = BigDecimal.valueOf(0.1);
        BigDecimal dividend2 = BigDecimal.valueOf(0.24);

        String input = "Date,Dividend\n"
                     + date1 + "," + dividend1 + "\n"
                     + date2 + "," + dividend2 + "\n";

        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
        doReturn(new BasicCookieStore()).when(httpHandler).getCookieStore();

        InputStream stream = new ByteArrayInputStream(input.getBytes());

        doReturn(entity).when(httpResponse).getEntity();
        doReturn(stream).when(entity).getContent();

        List<DividendHistory> dataSet = client.fetchDividendData(SYMBOL, FROM, TO);
        assertEquals(2, dataSet.size());

        assertEquals(DividendHistory.builder()
                        .date(date1)
                        .dividend(dividend1)
                        .build(),
                dataSet.get(0));

        assertEquals(DividendHistory.builder()
                        .date(date2)
                        .dividend(dividend2)
                        .build(),
                dataSet.get(1));
    }

    @Test
    public void testFetchPriceData_Http404() throws Exception {
        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());

        List<PricingHistory> dataSet = client.fetchPriceData(SYMBOL, FROM, TO);

        assertEquals(0, dataSet.size());
    }

    @Test
    public void testFetchDividendData_Http404() throws IOException {
        doNothing().when(session).acquireCrumbWithTicker(SYMBOL);

        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());

        List<DividendHistory> dataSet = client.fetchDividendData(SYMBOL, FROM, TO);

        assertEquals(0, dataSet.size());
    }
}
