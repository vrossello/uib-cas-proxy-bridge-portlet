package es.uib.owu.liferay.portlet;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import com.liferay.portal.kernel.cache.MultiVMPoolUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

import es.uib.owu.liferay.cas.service.CasServiceUtilLocalService;
import es.uib.owu.liferay.cas.service.CasServiceUtilLocalServiceUtil;

/**
 * Portlet implementation class TestPortlet
 */
public class CasProxyBridgePortlet extends MVCPortlet {

	private static final String CACHE_HTMLRESPONSE_SERVICE_USER = "CACHE_HTMLRESPONSE_SERVICE_USER";
	private static final int TIME_TO_LIVE_HTMLRESPONSE = 30 * 60; // 30minuts

	private static Log _log = LogFactoryUtil
			.getLog(CasProxyBridgePortlet.class);

	@Override
	public void doView(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		CasServiceUtilLocalService casServiceUtilLocalService = CasServiceUtilLocalServiceUtil
				.getService();

		PortletPreferences preferences = renderRequest.getPreferences();
		String targetService = preferences.getValue("targetServiceProxied",
				null);
		String htmlJsoupSelector = preferences.getValue("htmlJsoupSelector",
				null);
		String htmlCacheSeconds = preferences.getValue("htmlCacheSeconds",
				"1800");

		int timeToLiveHtmlCache = TIME_TO_LIVE_HTMLRESPONSE;
		try {
			timeToLiveHtmlCache = Integer.valueOf(htmlCacheSeconds);
		} catch (NumberFormatException nfe) {
			if (_log.isDebugEnabled()) {
				_log.debug("Incorrect value htmlCacheSeconds:"
						+ htmlCacheSeconds);
			}
		}

		if (_log.isDebugEnabled()) {
			_log.debug("CAS_PROXY_BRIDGE_PORTLET----------------------------------------");
			_log.debug("targetService:" + targetService);
			_log.debug("htmlJsoupSelector:" + htmlJsoupSelector);
			if (renderRequest.getPortletSession()!=null)
				_log.debug("renderRequest.getPortletSession().getId(): "+renderRequest.getPortletSession().getId());
		}

		// String targetService = "https://ThirdApp/sso/ProtectedServlet";
		// String selectorJsoup = "#summary > div.bloc:eq(2)";

		if (targetService != null) {

			String userName = renderRequest.getUserPrincipal().getName();
			Long companyId = PortalUtil.getCompanyId(renderRequest);
			
			String keyServiceUser = userName + "-" + targetService + "-"
					+ companyId+'_'+htmlJsoupSelector;

			String modelHTML = (String) MultiVMPoolUtil.getCache(
					CACHE_HTMLRESPONSE_SERVICE_USER).get(keyServiceUser);

			if (modelHTML == null) {
				HttpURLConnection con = ConnectionUtils.getConnection(
						keyServiceUser, renderRequest,
						casServiceUtilLocalService, targetService);

				if (con != null) {					
					modelHTML = htmlProcessing(targetService,
							htmlJsoupSelector, con);
					MultiVMPoolUtil
							.getCache(CACHE_HTMLRESPONSE_SERVICE_USER)
							.put(keyServiceUser, modelHTML, timeToLiveHtmlCache);
					renderRequest.setAttribute("MODEL_DADES", modelHTML);
					con.disconnect();
				} else {
					renderRequest.setAttribute("MODEL_DADES",
							"<h1>ERROR DE CONNEXIO -O- USUARI NO SSO</h1>");
				}
				
			} else {
				if (_log.isDebugEnabled())
					_log.debug("modelHTML is in cache");
				renderRequest.setAttribute("MODEL_DADES", modelHTML);
			}
		} else {
			renderRequest.setAttribute("MODEL_DADES",
					"FALTA DEFINIR TARGETSERVICE -O- USUARI NO SSO");
		}

		super.doView(renderRequest, renderResponse);
	}

	private String htmlProcessing(String targetService,
			String htmlJsoupSelector, HttpURLConnection con) throws IOException {
		Document doc = Jsoup
				.parse(con.getInputStream(), "UTF-8", targetService);

		// Turn any url into an absolute url
		String myTargetedTags = "img, a, link";
		for (Element e : doc.select(myTargetedTags)) {
			switch (e.tagName().toLowerCase()) {
			case "img":
				e.attr("src", e.absUrl("src"));
				break;

			case "a":
				e.attr("href", e.absUrl("href"));
				break;

			case "link":
				e.attr("href", e.absUrl("href"));
				break;

			default:
				throw new RuntimeException("Unexpected element:\n"
						+ e.outerHtml());
			}
		}

		doc.outputSettings().escapeMode(EscapeMode.xhtml);
		String str = doc.outerHtml();	

		Elements blocElements = doc.select(htmlJsoupSelector == null ? "body"
				: htmlJsoupSelector);

		if (!blocElements.isEmpty() && blocElements.size() > 0) {
			Element blocMissatges = blocElements.get(0);
			str = blocMissatges.outerHtml();
		}
		return str;
	}

	

}
