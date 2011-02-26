<% try{unwrapVariables()}catch(Exception e){} %>
<html>
    <head>
        <title><% try{out << headerTitle}catch(Exception e){out << "Gaelyk"} %></title>
        
        <link rel="shortcut icon" href="/images/gaelyk-small-favicon.png" type="image/png">
        <link rel="icon" href="/images/gaelyk-small-favicon.png" type="image/png">
        		
		<% try{out << headerHead}catch(Exception e){System.out.println(e)} %>
    </head>
    <body>
        <div>
            <img src="/images/gaelyk.png">
        </div>
        <div>
		<%= user ? """Welcome ${user}! <a href="${users.createLogoutURL(controllerURL)}">${message(code:"default.account.logout", default:"Log Out")}</a>""" : """<a href="${users.createLoginURL(controllerURL)}">${message(code:"default.account.login", default:"Log In")}</a>
		${message(code:"default.account.noaccount", default:"Don't have an account?")}<a href="${users.createLoginURL("/userprofile/create")}">${message(code:"default.account.signup", default:"Sign Up")}</a>""" %>
