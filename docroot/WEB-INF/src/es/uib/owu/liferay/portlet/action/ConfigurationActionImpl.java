package es.uib.owu.liferay.portlet.action;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;

import es.uib.owu.liferay.portlet.CasProxyBridgePortlet;

public class ConfigurationActionImpl extends DefaultConfigurationAction {
	private static Log _log = LogFactoryUtil
			.getLog(CasProxyBridgePortlet.class);

	@Override
	public void processAction(PortletConfig portletConfig,
			ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {

		PortletPreferences prefs = actionRequest.getPreferences();

		_log.debug("processAction\n======================================================");
		String targetServiceProxied = prefs
				.getValue("targetServiceProxied", "");
		_log.debug("old targetServiceProxied>>==" + targetServiceProxied);

		String htmlJsoupSelector = prefs.getValue("htmlJsoupSelector", "");
		_log.debug("old htmlJsoupSelector>>==" + htmlJsoupSelector);

		String htmlCacheSeconds = prefs.getValue("htmlCacheSeconds", "");
		_log.debug("old htmlCacheSeconds>>==" + htmlCacheSeconds);

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);
		_log.debug("cmd>>==" + cmd);
		/*
		 * if (cmd.equals(Constants.UPDATE)) { setPreference(actionRequest,
		 * "VIEW_ALL_RESULTS_PER_PAGE", ParamUtil.getString(actionRequest,
		 * "viewAllResultsPerPage")); setPreference(actionRequest,
		 * "SEARCH_RESULTS_PER_PAGE", ParamUtil.getString(actionRequest,
		 * "searchResultsPerPage")); }
		 */
		super.processAction(portletConfig, actionRequest, actionResponse);
	}
}
