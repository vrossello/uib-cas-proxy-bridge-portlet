package es.uib.owu.liferay.portlet;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.portlet.RenderRequest;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.liferay.portal.kernel.cache.MultiVMPoolUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import es.uib.owu.liferay.cas.service.CasServiceUtilLocalService;

public class ConnectionUtils {
	public static final String USER_AGENT_PROXY = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.215 Safari/535.1 JAVA_CLIENT";
	private static final String CACHE_COOKIES_SERVICE_USER = "CACHE_COOKIES_SERVICE_USER";
	private static final int TIME_TO_LIVE_COOKIE = 3600; // 3600 segons *8 = 1h
															// *8 = 8h ->
															// despres
															// tornarÃ  a
															// demanar PT si
															// encara es actiu

	private static Log _log = LogFactoryUtil.getLog(ConnectionUtils.class);
	
	
	

	public static HttpURLConnection getConnection(String keyServiceUser,
			RenderRequest renderRequest,
			CasServiceUtilLocalService casServiceUtilLocalService,
			String targetService) throws MalformedURLException,
			ProtocolException, IOException {
		String cookieCached = (String) MultiVMPoolUtil.getCache(
				CACHE_COOKIES_SERVICE_USER).get(keyServiceUser);
		if (_log.isDebugEnabled()) {
			_log.debug("key (userName-targetService-companyId):"
					+ keyServiceUser);
			_log.debug("modelHTML is null ");
			_log.debug("cookieCached: " + cookieCached);
		}
		HttpURLConnection con = null;
		if (cookieCached == null) {
			// Connectam via PROXY TICKET
			ConnectionCookie cc = ConnectionUtils.connectServiceProxyTicket(
					renderRequest, casServiceUtilLocalService, targetService,
					keyServiceUser);
			if (cc == null)
				return null;
			con = cc.getConnection();
			_log.debug("Connection: " + con);
			if (con == null)
				return null;
			cookieCached = cc.getCookie();
			MultiVMPoolUtil.getCache(CACHE_COOKIES_SERVICE_USER).put(
					keyServiceUser, cookieCached);
		} else { // cookieCached

			URL url = new URL(targetService);
			if (_log.isDebugEnabled())
				_log.debug("\nSending request to URL : " + url
						+ " including Cookie: " + cookieCached);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Cookie", cookieCached);
			CookieManager cm = new CookieManager();
			cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(cm);
			con.setInstanceFollowRedirects(false);
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent",
					ConnectionUtils.USER_AGENT_PROXY);
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setAllowUserInteraction(false);
			int responseStatusCode = con.getResponseCode();
			if (_log.isDebugEnabled())
				_log.debug("\nResponse code : " + responseStatusCode);

			int tryNumber = 0;
			while (responseStatusCode != HttpURLConnection.HTTP_OK
					&& tryNumber < 3) { // RECONNECTA I CANVIA KEY
				MultiVMPoolUtil.getCache(CACHE_COOKIES_SERVICE_USER).remove(
						keyServiceUser);
				ConnectionCookie cc = ConnectionUtils
						.connectServiceProxyTicket(renderRequest,
								casServiceUtilLocalService, targetService,
								keyServiceUser);
				if (cc == null)
					tryNumber = 4;
				con = cc.getConnection();
				cookieCached = cc.getCookie();
				MultiVMPoolUtil.getCache(CACHE_COOKIES_SERVICE_USER).put(
						keyServiceUser, cookieCached, TIME_TO_LIVE_COOKIE);
				if (con != null) {
					responseStatusCode = con.getResponseCode();

				} else {
					tryNumber = 4;
				}
			}

			String location = con.getHeaderField("Location");
			if (_log.isDebugEnabled())
				_log.debug("\nLocation : " + location);
		}
		return con;
	}

	public static ConnectionCookie connectServiceProxyTicket(
			RenderRequest renderRequest,
			CasServiceUtilLocalService casServiceUtilLocalService,
			String targetService, String key) throws MalformedURLException,
			IOException, ProtocolException {

		String cookieCached = null;
		HttpURLConnection con = null;

		String pt = casServiceUtilLocalService.getProxyTicket(renderRequest,
				targetService);

		if (_log.isDebugEnabled())
			_log.debug("proxy ticket: " + pt);

		if (pt == null)
			return null;

		String ampOrInt = targetService.indexOf("?") >= 0 ? "&" : "?";

		URL url = new URL(targetService + ampOrInt + "ticket=" + pt);
		_log.debug("\nSending request to URL : " + url);

		con = (HttpURLConnection) url.openConnection();

		CookieManager cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cm);

		con.setInstanceFollowRedirects(false);

		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT_PROXY);

		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(false);
		con.setAllowUserInteraction(false);

		// _log.debug("getContent : " +con.getContent());

		// con.connect();
		int responseStatusCode = con.getResponseCode();

		_log.debug("Response Code : " + responseStatusCode);

		/*
		 * CookieStore cookieJar = cm.getCookieStore(); List <HttpCookie>
		 * cookiesList = cookieJar.getCookies(); for (HttpCookie cookie:
		 * cookiesList) { _log.debug("CookieHandler retrieved cookie: " +
		 * cookie); }
		 */
		String cookie = null; // if cookies are needed (i.e. for login)
		String keyCookie = getKeyCookie(con);
		if (keyCookie != null) {
			_log.debug("keyCookie: " + keyCookie);
			cookie = con.getHeaderField(keyCookie);
		}

		if (responseStatusCode != HttpURLConnection.HTTP_OK) {
			switch (responseStatusCode) {
			case HttpURLConnection.HTTP_MOVED_TEMP: // handle 302
			case HttpURLConnection.HTTP_MOVED_PERM: // handle 301
			case HttpURLConnection.HTTP_SEE_OTHER:
				// Redirect
				String newUrl = con.getHeaderField("Location");
				String sessionId;

				// String keyCookie = getKeyCookie(con);
				// if (keyCookie != null) {
				// _log.debug("keyCookie: " + keyCookie);
				// cookie = con.getHeaderField(keyCookie);
				if (cookie != null && cookie.trim().length() > 0) {
					_log.debug("cookie: " + cookie);
					sessionId = getSessionId(cookie);
					_log.debug("retrieveSessionID sessionid: " + sessionId);
				} else {
					sessionId = getSessionIdFromUrl(newUrl);
					cookie = "JSESSIONID=" + sessionId;
					_log.debug("cookie: " + cookie);
				}

				if (sessionId != null) {
					// manually redirect using a new connection
					con = (HttpURLConnection) new URL(newUrl).openConnection(); // REDIRECT
					con.setRequestProperty("Cookie", cookie);
					con.addRequestProperty("User-agent", USER_AGENT_PROXY);
				}
				// }
			default:
				// handle default (other) case
			}
		}

		if (cookie != null) {

			cookieCached = cookie;
		}
		ConnectionCookie cc = new ConnectionCookie(con, cookieCached);
		return cc;
	}

	public static String getKeyCookie(HttpURLConnection urlConnection) {
		for (String key : urlConnection.getHeaderFields().keySet()) {
			if (key != null) {
				if (key.equalsIgnoreCase("set-cookie")) {
					return key;
				}
			}
		}
		return null;
	}

	private static final String[] SESS_ID_NAMES = { "JSESSIONID",
			"ASPSESSIONID", "SID", "PHPSESSIONID", "NID" };

	private static String getSessionId(String cookie) {
		if (cookie != null) {
			for (String sidname : SESS_ID_NAMES) {
				if (cookie.contains(sidname)) {
					int index = cookie.indexOf(sidname);
					return cookie.substring(index + sidname.length() + 1,
							cookie.indexOf(";", index));
				}
			}
		}
		return null;
	}

	private static String getSessionIdFromUrl(String url) {
		if (url != null) {
			String urlUpper = url.toUpperCase();
			for (String sidname : SESS_ID_NAMES) {
				if (urlUpper.contains(sidname)) {
					int index = urlUpper.indexOf(";" + sidname + "=");
					return url.substring(index + sidname.length() + 2);
				}
			}
		}
		return null;
	}
}
