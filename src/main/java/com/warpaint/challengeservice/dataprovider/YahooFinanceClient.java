package com.warpaint.challengeservice.dataprovider;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Stable client libraries (e.g. https://financequotes-api.com/) are broken since Yahoo discontinued
 * support for the public API.
 * The below code works for now -- if not, please reach out to us.
 */
@Service
@Slf4j
public class YahooFinanceClient {

	private static final String PRICE_FORMAT_URL = "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=history&interval=1d&crumb=%s";
	private static final String DIVIDEND_FORMAT_URL = "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=div&interval=1d&crumb=%s";

	@Setter
	private YahooFinanceSession session;
	private HttpHandler httpHandler;

	public YahooFinanceClient(HttpHandler httpHandler) {
	    this.httpHandler = httpHandler;

		this.session = new YahooFinanceSession(httpHandler);
	}

	private String constructURL(String formatURL, String ticker, LocalDate from, LocalDate to) {
		long fromEpoch = from.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		long toEpoch = to.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		String crumb = (session.getCrumb() != null) ? HttpHandler.urlEncodeString(session.getCrumb()) : "";
		String encodedTicker = HttpHandler.urlEncodeString(ticker);
		return String.format(formatURL, encodedTicker, fromEpoch, toEpoch, crumb);
	}

    private HttpEntity fetchURL(String url, String symbol, LocalDate fromDate, LocalDate toDate) {

        HttpGet request = new HttpGet(url);
        HttpResponse response = httpHandler.fetchResponse(request);
        HttpStatus statusCode = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
        if (statusCode == HttpStatus.UNAUTHORIZED) {
            log.debug("Unauthorized response using crumb and cookies:");
            log.debug("crumb: {} cookies: {}", session.getCrumb(), httpHandler.getCookieStore().getCookies());
            session.invalidate();
            session.acquireCrumbWithTicker(symbol);
            log.info("Retrying connection after unauthorized response");

            request.setURI(URI.create(constructURL(PRICE_FORMAT_URL, symbol, fromDate, toDate))); // Acquire new crumb
            request.reset();
            EntityUtils.consumeQuietly(response.getEntity());
            response = httpHandler.fetchResponse(request);
        } else if (statusCode == HttpStatus.NOT_FOUND) {
            EntityUtils.consumeQuietly(response.getEntity());
            return null;
        }
        return response.getEntity();
    }


	public List<?> fetchPriceData(String symbol, LocalDate fromDate, LocalDate toDate) {
		log.info("Acquiring price data for {} from {} to {}", symbol, fromDate, toDate);
		session.acquireCrumbWithTicker(symbol);

		String priceURL = constructURL(PRICE_FORMAT_URL, symbol, fromDate, toDate);
		HttpEntity entity = fetchURL(priceURL, symbol, fromDate, toDate);
		if (entity == null) {
            log.warn("No price data available for {} from {} to {}", symbol, fromDate, toDate);
            return emptyList();
        }

        //TODO parse entity.getContent() and return data
        return emptyList();
	}


	public List<?> fetchDividendData(String symbol, LocalDate fromDate, LocalDate toDate) {
		log.info("Acquiring dividend data for {} from {} to {}", symbol, fromDate, toDate);
		session.acquireCrumbWithTicker(symbol);

		String dividendURL = constructURL(DIVIDEND_FORMAT_URL, symbol, fromDate, toDate);
        HttpEntity entity = fetchURL(dividendURL, symbol, fromDate, toDate);
        if (entity == null) {
            log.warn("No dividend data available for {} from {} to {}", symbol, fromDate, toDate);
            return emptyList();
        }

        //TODO parse entity.getContent() and return data
        return emptyList();
	}
}
