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
<div>
    ${json}
</div>
<div class="row">
    <nav>
        <ul class="pager">
            <li class="previous"><a href="#"><span aria-hidden="true">&larr;</span> Previous</a></li>
            <li class="next"><a href="#">Next <span aria-hidden="true">&rarr;</span></a></li>
        </ul>
    </nav>
</div>
</body>
</html>