 <%@page import="com.liferay.portal.kernel.util.StringPool"%>
<%@page import="com.liferay.portlet.PortletPreferencesFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.util.Validator"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="javax.portlet.PortletPreferences"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:renderURL var="savePreferencesRenderURL" windowState="normal" portletMode="edit">
</portlet:renderURL>
<%
PortletPreferences preferences = renderRequest.getPreferences();
String portletResource = ParamUtil.getString(request, "portletResource");
if (Validator.isNotNull(portletResource)) {
preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);
}
String targetServiceProxied = preferences.getValue("targetServiceProxied",null);
String htmlJsoupSelector = preferences.getValue("htmlJsoupSelector",null);
%>

<h1>EDIT PREFERENCES</h1>
<form action="<%=savePreferencesRenderURL%>" name="savePreferences"  method="POST">

	<div class="row-fluid">
	<label for="<portlet:namespace/>targetServiceProxied">Target Service Proxied: </label>
	<input  type="text" name="<portlet:namespace/>targetServiceProxied" id="<portlet:namespace/>targetServiceProxied" value='<%=targetServiceProxied!=null?targetServiceProxied:StringPool.BLANK%>'/>
	</div>
	
	<div class="row-fluid">
	<label for="<portlet:namespace/>htmlJsoupSelector">Html Jsoup Selector: </label>
	<input  type="text" name="<portlet:namespace/>htmlJsoupSelector" id="<portlet:namespace/>htmlJsoupSelector" value='<%=htmlJsoupSelector!=null?htmlJsoupSelector:StringPool.BLANK%>'/>
	</div>
	<input type="submit" name="<portlet:namespace />savepref" id="savepref" value="Save Preferences"/>
</form>


<%--
<portlet:actionURL var="savePreferensesActionURL" windowState="normal" portletMode="edit">
</portlet:actionURL>
 --%>