<%
	try{unwrapVariables()}catch(Exception e){e.toString()}
	defaultValue("userData", null)
	//userData and userData.displayName are not part of Graelyk by default. They are variables you might define in your controller and make available here by calling wrapVariables(["userData"])
	//defaultValue("welcomeName", userData?.displayName ?: user.toString())
%>
<h3><span>Right</span> Nav</h3><br />
<h4><span>Welcome</span> message</h4>
<%= welcomeName %>
<h4>link <span>list</span></h4>
<ul style="list-style-type: none">
<li> <a href="http://gaelyk.appspot.com/">Gaelyk</a>
<li> <a href="http://graelyk.appspot.com/">Graelyk</a>
<li> <a href="http://code.google.com/appengine/docs/java/overview.html">Google App Engine - Java</a>
<li> More stuff
<li> could go
<li> here
</ul>

<h4>search <span>this site</span></h4>
<form method="post" action="">
<div class="search">
<input type="text" class="search text" name="keywords" />
<input type="submit" class="search button" value="Go" />
<br class="clear" />
</div>
</form>
