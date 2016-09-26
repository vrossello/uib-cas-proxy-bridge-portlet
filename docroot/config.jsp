 <%@page import="com.liferay.portal.kernel.util.StringPool"%>
<%@page import="com.liferay.portlet.PortletPreferencesFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.util.Validator"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="javax.portlet.PortletPreferences"%>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ page import="com.liferay.portal.kernel.util.GetterUtil" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />

<liferay-portlet:actionURL portletConfiguration="true" var="configurationURL" />
<liferay-portlet:renderURL portletConfiguration="true" var="configurationRenderURL"/>
<%

String targetServiceProxied_cfg = GetterUtil.getString(portletPreferences.getValue("targetServiceProxied", StringPool.BLANK));
String htmlJsoupSelector_cfg = GetterUtil.getString(portletPreferences.getValue("htmlJsoupSelector", StringPool.BLANK));
String htmlCacheSeconds_cfg = GetterUtil.getString(portletPreferences.getValue("htmlCacheSeconds", "1800")); //30min

PortletPreferences preferences = renderRequest.getPreferences();
String portletResource = ParamUtil.getString(request, "portletResource");
if (Validator.isNotNull(portletResource)) {
preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);
}
/*String targetServiceProxied = preferences.getValue("targetServiceProxied",null);
String htmlJsoupSelector = preferences.getValue("htmlJsoupSelector",null);
String htmlCacheSeconds = preferences.getValue("htmlCacheSeconds",null);*/
%>


<aui:form action="<%= configurationURL %>" method="post" name="fm">

    <aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>"/>
    <aui:input name="redirect" type="hidden" value="<%= configurationRenderURL.toString() %>"/>

	<aui:fieldset>
	                  
		<aui:input inlineField='<%= true %>' id='targetServiceProxied' name='preferences--targetServiceProxied--'
			 value='<%= targetServiceProxied_cfg %>'>
			 <aui:validator name="required" />
		</aui:input>
	
		<aui:input inlineField='<%= true %>' id='htmlJsoupSelector' name='preferences--htmlJsoupSelector--'
			 value='<%= htmlJsoupSelector_cfg %>'>
			 <aui:validator name="required" />
		</aui:input>
		
		<aui:input inlineField='<%= true %>' id='htmlCacheSeconds' name='preferences--htmlCacheSeconds--'
			 value='<%= htmlCacheSeconds_cfg %>'>
			 <aui:validator name="number" />
			 <aui:validator name="required" />
		</aui:input>
		
	</aui:fieldset>
   <aui:button-row>
        <aui:button type="submit" />
    </aui:button-row>
</aui:form>
