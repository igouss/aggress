#!/bin/sh
set -e

CLASSPATH=$(find "." -name '*.jar' | xargs echo | tr ' ' ':')
echo java -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -cp ${CLASSPATH} com.naxsoft.Crawler  -clean -crawl  -parse
java -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -cp ${CLASSPATH} com.naxsoft.Crawler  -clean -crawl  -parse