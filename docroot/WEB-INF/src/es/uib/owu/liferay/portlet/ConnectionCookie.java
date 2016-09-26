package es.uib.owu.liferay.portlet;

import java.net.HttpURLConnection;

public class ConnectionCookie {
	public HttpURLConnection con;
	public String cookie;

	public ConnectionCookie(HttpURLConnection con, String cookie) {
		this.con = con;
		this.cookie = cookie;
	}

	public String getCookie() {
		return cookie;
	}

	public HttpURLConnection getConnection() {
		return con;
	}
}
