#!/bin/sh
set -e

CLASSPATH=$(find "." -name '*.jar' | xargs echo | tr ' ' ':')
java -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -cp ${CLASSPATH} com.naxsoft.Server