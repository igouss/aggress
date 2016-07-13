#!/bin/sh
set -e

CLASSPATH=$(find "." -name '*.jar' | xargs echo | tr ' ' ':')
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -cp ${CLASSPATH} com.naxsoft.Crawler -createESIndex -createESMappings -clean -populate -crawl -parse