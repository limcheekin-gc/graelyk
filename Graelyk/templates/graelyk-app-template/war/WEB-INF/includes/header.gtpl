<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
	try{unwrapVariables()}catch(Exception e){e.toString()}
	defaultValue("userData", null)
	//userData and userData.displayName are not part of Graelyk by default. They are variables you might define in your controller and make available here by calling wrapVariables(["userData"])
	defaultValue("welcomeName", userData ? userData?.displayName : (user ? user.toString() : "Guest"))
	wrapVariables(["welcomeName", "userData"])
	try{test = processedController}catch(Exception e){this.processController(false, null)}
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />
<title><% try{out << headTitle}catch(Exception e){out << "Comment/How?"} %></title>
<meta name="keywords" content="" />
<meta name="description" content="" />
<link rel="shortcut icon" href="/images/gaelyk-small-favicon.png" type="image/png">
<link rel="icon" href="/images/gaelyk-small-favicon.png" type="image/png">
<link rel="stylesheet" type="text/css" href="/css/microCSSNonZero.css" />
<link rel="stylesheet" type="text/css" href="/css/blue.css" />  <!-- blue, brown, green, magenta, red -->
<link rel="stylesheet" type="text/css" href="/css/GraelykForm.css" />
<% try{out << headIncludes}catch(Exception e){} %>
</head>
<body>
<div id="header"  class="wrap fixed1200">
	<div id="header_inner">
		
		<div id="menu">
			<ul>
				<li><a href="/">Home</a></li> <!-- class="active" -->
				<li><a href="/forum">Forums</a></li>
				<% try{out << navIncludes}catch(Exception e){} %>
				<li>
					<span>
						<% if(user) { %>
							<%= """Welcome ${welcomeName}! 
							<span class="menuButton"><a href="${users.createLogoutURL("/")}">${message(code:"default.account.logout", default:"Log Out")}</a></span>""" %>
							<span class="menuButton"><% g_link(controller:"userprofile", action:"edit"){%><% g_message(code:"default.userprofile.edit") %><% } %></span>
						<% } else { %>
							<%= """<a href="${users.createLoginURL(controllerURL)}">${message(code:"default.account.login", default:"Log In")}</a>
								${message(code:"default.account.noaccount", default:"Don't have an account?")}<a href="${users.createLoginURL("/userprofile/create")}">${message(code:"default.account.signup", default:"Sign Up")}</a>"""
							%>		
						<% } %>
					</span>
				</li>
			</ul>
		</div>
		
		<div id="logo" class="wrap">
			<h1><span>Comment/How?</span></h1>
			<div class="right75" style="margin-top: 0.5em">
			<h2>
			<ul>
			<li>Hoe</li><!--Afrikaans, Dutch-->
			<li>Si</li><!--Albanian-->
			<li>كيف؟</li><!--Arabic-->
			<li>Ինչպես</li><!--Armenian-->
			<li>Necə</li><!--Azerbaijani-->
			<li>Nola</li><!--Basque-->
			<li>Як</li><!--Belarusian, Ukrainian-->
			<li>Как</li><!--Bulgarian, Russian-->
			<li>Com</li><!--Catalan-->
			<li>怎么样</li><!--Chinese Simplified-->
			<li>怎麼樣</li><!--Chinese Traditional-->
			<li>Kako</li><!--Croatian, Slovenian-->
			<li>Jak</li><!--Czech-->
			<li>Hvordan</li><!--Danish, Norwegian-->
			<li>Kuidas</li><!--Estonian-->
			<li>Paano</li><!--Filipino-->
			<li>Miten</li><!--Finnish-->
			<li>Como</li><!--Galician, Portuguese-->
			<li>როგორ</li><!--Georgian-->
			<li>Wie</li><!--German-->
			<li>Πώς;</li><!--Greek-->
			<li>Kouman</li><!--Haitian Creole-->
			<li>כיצד</li><!--Hebrew-->
			<li>कैसे</li><!--Hindi-->
			<li>Hogyan</li><!--Hungarian-->
			<li>Hvernig</li><!--Icelandic-->
			<li>Bagaimana</li><!--Indonesian, Malay-->
			<li>Conas</li><!--Irish-->
			<li>Come</li><!--Italian-->
			<li>どうやって</li><!--Japanese-->
			<li>어떻게</li><!--Korean-->
			<li>Quomodo</li><!--Latin-->
			<li>Kā</li><!--Latvian-->
			<li>Kaip</li><!--Lithuanian-->
			<li>Како</li><!--Macedonian, Serbian-->
			<li>Kif</li><!--Maltese-->
			<li>چگونه؟</li><!--Persian-->
			<li>W jaki sposób</li><!--Polish-->
			<li>Cum</li><!--Romanian-->
			<li>Ako</li><!--Slovak-->
			<li>¿Cómo?</li><!--Spanish-->
			<li>Jinsi gani</li><!--Swahili-->
			<li>Hur</li><!--Swedish-->
			<li>ได้อย่างไร</li><!--Thai-->
			<li>Nasıl</li><!--Turkish-->
			<li>کیسے؟</li><!--Urdu-->
			<li>Làm thế nào</li><!--Vietnamese-->
			<li>Sut</li><!--Welsh-->
			<li>ווי</li><!--Yiddish-->
			</ul>
			</h2>
			</div>
			<!--@Logo@-->
		</div>
	</div>
</div>

<div id="submenu" class="fixed1200">
	<div id="submenu_inner">
		<ul>
			<!--
			<li><span>Breadcrumb: <a href="#">You</a> &gt; <a href="#">Are</a> &gt; <a href="#">Here</a></span></li>
			<li><a href="#">List</a></li>
			-->
			<% try{out << subNavIncludes}catch(Exception e){} %>
		</ul>
	</div>
</div>

<div id="main" class="fixed1200">

	<div id="main_inner" class="left fixed1200 wrap">
	
		<div class="leftAuto">
		
			<div class="leftAuto columnLeft">
				<% try{out << leftColumnIncludes}catch(Exception e){} %>
			</div>
			
			<div class="rightAuto columnCenter">