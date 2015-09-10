<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Login</title>
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
        <div class="input-group">
            <input class="form-control" placeholder="E-Mail" type="email" name="username"/>
        </div>

        <div class="input-group">
            <input class="form-control" placeholder="Password" type="password" name="password"/>
        </div>

        <div class="input-group">
            <input type="submit" name="Login" value="Login"/>
        </div>
    </g:form>
    <g:link action="register">Register</g:link>
</g:else>
</body>
</html>