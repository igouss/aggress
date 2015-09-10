<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Welcome to Grails</title>
</head>

<body>
<div class="row">
    <div class="col-md-4">
        <div id="status" role="complementary">
            <h1>Application Status</h1>
            <ul>
                <li>Environment: ${grails.util.Environment.current.name}</li>
                <li>App profile: ${grailsApplication.config.grails?.profile}</li>
                <li>App version: <g:meta name="info.app.version"/></li>
                <li>Grails version: <g:meta name="info.app.grailsVersion"/></li>
                <li>Groovy version: ${GroovySystem.getVersion()}</li>
                <li>JVM version: ${System.getProperty('java.version')}</li>
                <li>Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</li>
            </ul>

            <h1>Artefacts</h1>
            <ul>
                <li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
                <li>Domains: ${grailsApplication.domainClasses.size()}</li>
                <li>Services: ${grailsApplication.serviceClasses.size()}</li>
                <li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
            </ul>

            <h1>Installed Plugins</h1>
            <ul>
                <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
                    <li>${plugin.name} - ${plugin.version}</li>
                </g:each>
            </ul>
        </div>
    </div>

    <div id="page-body" role="main" class="col-md-8">
        <div id="controller-list" role="navigation">
            <h2>Available Controllers:</h2>
            <ul class="nav nav-pills">
                <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName }}">
                    <g:if test="${controllerName == c.logicalPropertyName}">
                    <li class="active"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
                    </g:if>
                    <g:else>
                        <li><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
                    </g:else>
                </g:each>
            </ul>
        </div>
    </div>
</div>
</body>
</html>
