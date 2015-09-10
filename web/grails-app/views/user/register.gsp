<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Register</title>
</head>

<body>

<g:if test="${flash.message}">
    <div class="message">
        ${flash.message}
    </div>
</g:if>
<g:form controller="user" action="saveUser">
    <div class="login">
        <div class="input-group">
            <input class="form-control" placeholder="E-Mail" type="email" name="username"/>
        </div>

        <div class="input-group">
            <input class="form-control" placeholder="Password" type="password" name="password"/>
        </div>

        <div class="input-group">
            <input class="form-control" placeholder="Password" type="password" name="password2"/>
        </div>

        <div class="input-group">
            <input type="submit" name="Register" value="Register"/>
        </div>
    </div>
</g:form>
</body>
</html>