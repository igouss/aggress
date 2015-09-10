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
<g:form controller="search" action="search">
    <div class="row">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Query</h3>
            </div>
            <div class="panel-body">
                <div class="col-md-12">
                    <span class="label label-default">Query</span>
                    <g:textArea name="query" escapeHtml="true" id="query"/>
                </div>
                <div class="col-md-12">
                    <span class="label label-default">Keywords</span>
                    <g:textArea name="keywords" escapeHtml="true" id="query"/>
                </div>
                <div class="col-lg-12">
                    <input type="submit" class="btn" name="Search"/>
                </div>
            </div>
        </div>
    </div>
</g:form>
</body>
</html>