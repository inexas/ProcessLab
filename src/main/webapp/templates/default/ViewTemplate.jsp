<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<fmt:setBundle basename="templates.default"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html id="top" xmlns="http://www.w3.org/1999/xhtml">

<head>
  <title>
    <fmt:message key="view.title.view">
      <fmt:param><wiki:Variable var="ApplicationName" /></fmt:param>
      <fmt:param><wiki:PageName /></fmt:param>
    </fmt:message>
  </title>
  <wiki:Include page="commonheader.jsp"/>
  <wiki:CheckVersion mode="notlatest">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckVersion>
  <wiki:CheckRequestContext context="diff|info">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckRequestContext>
  <wiki:CheckRequestContext context="!view">
    <meta name="robots" content="noindex,follow" />
  </wiki:CheckRequestContext>
<script type="text/javascript">
var packages = [];
var callbacks = [];
function queueChart(package, callback) {
	var found = false;
	for(i = 0; i < packages.length; i++) {
		if(packages[i] == package) {
			found = true;
			break;
		}
	}
	if(!found) {
		packages[packages.length] = package;
	}
	callbacks[callbacks.length] = callback;
}

function drawCharts() {
	for(i = 0; i < callbacks.length; i++) {
		callbacks[i]();
	}
}

function loadPackages() {
	google.load("visualization", "1", {"packages" : packages, "callback" : drawCharts});
}
</script>

</head>

<body class="view">

<div id="wikibody" class="${prefs.Orientation}">
 
  <wiki:Include page="Header.jsp" />

  <div id="content">

    <div id="page">
      <wiki:Include page="PageActionsTop.jsp"/>
      <wiki:Content/>
      <wiki:Include page="PageActionsBottom.jsp"/>
    </div>

    <wiki:Include page="Favorites.jsp"/>

	<div class="clearbox"></div>
  </div>

  <wiki:Include page="Footer.jsp" />

</div>

<script type='text/javascript'>
if(packages.length > 0) {
	loadPackages();
}
</script>

</body>
</html>