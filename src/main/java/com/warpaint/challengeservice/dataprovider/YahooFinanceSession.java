package com.warpaint.challengeservice.dataprovider;

import com.warpaint.challengeservice.dataprovider.HttpHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class YahooFinanceSession {

	private static final String PROFILE_BASE_URL = "https://finance.yahoo.com/quote/%s/profile?p=%s";

	private static final String CRUMB_REGEX = "CrumbStore\":\\{\"crumb\":\"(.*?)\"}";

	private final HttpHandler httpHandler;

	@Getter
	private String crumb;

	YahooFinanceSession(HttpHandler httpHandler) {
		this.httpHandler = httpHandler;
	}

	public void acquireCrumbWithTicker(String ticker) {
		if (crumb == null) {
			String url = getProfileURL(ticker);
			HttpGet request = new HttpGet(url);
			try {
				HttpResponse response = httpHandler.fetchResponse(request);
                crumb = extractCrumbFromStream(response.getEntity().getContent());
                log.debug("Fetched session crumb: {}", crumb);
			}
            catch (IOException e) {
                log.error("Failed to fetch session crumb: {}", e.getLocalizedMessage());
            }
			finally {
				request.releaseConnection();
			}
		} else {
			log.debug("Session crumb already acquired");
		}
	}

	public void invalidate() {
		httpHandler.getCookieStore().clear();
		crumb = null;
	}

	private static String getProfileURL(String ticker) {
		String encodedTicker = HttpHandler.urlEncodeString(ticker);
		return String.format(PROFILE_BASE_URL, encodedTicker.toUpperCase(), encodedTicker.toUpperCase());
	}

	/**
	 * Find session crumb information in a string stream
	 */
	private static String extractCrumbFromStream(InputStream stream) {
		String crumb = null;
		Pattern pattern = Pattern.compile(CRUMB_REGEX);
		try (InputStreamReader reader = new InputStreamReader(stream);
			 BufferedReader br = new BufferedReader(reader))
		{
			String line;
			while ( ( line = br.readLine() ) != null ) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					crumb = matcher.group(1);
					crumb = crumb.replace("\\u002F", "/");
					break;
				}
			}
		}
		catch (IOException e) {
			log.error("Failed to fetch session crumb: {}", e.getLocalizedMessage());
		}
		return crumb;
	}

}
