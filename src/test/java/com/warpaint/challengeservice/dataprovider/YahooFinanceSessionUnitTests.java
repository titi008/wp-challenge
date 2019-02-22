package com.warpaint.challengeservice.dataprovider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class YahooFinanceSessionUnitTests {
	
	@InjectMocks
    private YahooFinanceSession session;
	
	@Mock
	private HttpHandler httpHandler;
	
	@Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
	
	@Test
	public void testInvalidate() {
		CookieStore cookieStore = new BasicCookieStore();
		doReturn(cookieStore).when(httpHandler).getCookieStore();
		session.invalidate();
		assertNull(session.getCrumb());
	}
	
	@Test
	public void testAcquireCrumbWithTicker() throws UnsupportedOperationException, IOException {
		String ticker = "LOGM";
		String crumb = "5h8F6Ab9PIU";
		String input = "SomeLine\nCrumbStore\":{\"crumb\":\"" + crumb + "\"}";
		
		HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
		HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
        
		InputStream stream = new ByteArrayInputStream(input.getBytes());
		
		doReturn(entity).when(httpResponse).getEntity();
		doReturn(stream).when(entity).getContent();
		
		session.acquireCrumbWithTicker(ticker);
		
		assertEquals(session.getCrumb(), crumb);

		// Already acquired
		session.acquireCrumbWithTicker(ticker);
	}
	
	@Test(expected = Exception.class)
	public void testAcquireCrumbWithTickerException() throws UnsupportedOperationException, IOException {
		String ticker = "LOGM";
		
		HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doThrow(Exception.class).when(httpHandler).fetchResponse(anyObject());
		
		session.acquireCrumbWithTicker(ticker);
	}
	
	@Test
	public void testAcquireCrumbWithTickerNotFound() throws UnsupportedOperationException, IOException {
		String ticker = "LOGM";
		String input = "SomeLine\nSomeOtherLine";
		
		HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
		HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
        
		InputStream stream = new ByteArrayInputStream(input.getBytes());
		
		doReturn(entity).when(httpResponse).getEntity();
		doReturn(stream).when(entity).getContent();
		
		session.acquireCrumbWithTicker(ticker);
		
		assertEquals(null, session.getCrumb());
	}
	
	@Test
	public void testAcquireCrumbWithTickerIOException() throws IOException {
		String ticker = "LOGM";
		
		HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
		HttpEntity entity = mock(HttpEntity.class);
        
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        doReturn(httpResponse).when(httpHandler).fetchResponse(anyObject());
		
		doReturn(entity).when(httpResponse).getEntity();
		doThrow(IOException.class).when(entity).getContent();
		
		session.acquireCrumbWithTicker(ticker);
		
		assertEquals(null, session.getCrumb());
	}
	
}
