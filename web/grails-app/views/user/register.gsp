<%--
  Created by IntelliJ IDEA.
  User: Iouri
  Date: 9/7/2015
  Time: 1:52 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Register</title>
    <g:meta name="layout" content="main"/>
</head>

<body>
        <g:if test="${flash.message}">
            <div class="message">
                ${flash.message}
            </div>
        </g:if>
        <g:form controller="user" action="saveUser">
            <div class="login">
                <label>E-mail</label><input type="email" name="username"/>
                <label>Password</label><input type="password" name="password"/>
                <label>Password</label><input type="password" name="password2"/>
                <label>&nbsp;</label><input type="submit" name="Register"/>
            </div>
        </g:form>
</body>
</html>