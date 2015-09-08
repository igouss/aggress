<%--
  Created by IntelliJ IDEA.
  User: Iouri
  Date: 9/7/2015
  Time: 11:32 AM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Login</title>
    <g:meta name="layout" content="main"/>
</head>

<body>
        <g:if test="${flash.message}">
            <div class="message">
                ${flash.message}
            </div>
        </g:if>
        <g:if test="${session.user}">
            Hello ${session.user} | <g:link controller="user" action="logout">Logout</g:link>
        </g:if>
        <g:else>
            <g:form controller="user" action="login">
                <div class="login">
                    <label>E-mail</label><input type="email" name="username"/>
                    <label>Password</label><input type="password" name="password"/>
                    <label>&nbsp;</label><input type="submit" name="Login"/>
                </div>
            </g:form>
            <g:link action="register">Register</g:link>
        </g:else>
</body>
</html>