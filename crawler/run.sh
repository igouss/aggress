#!/usr/bin/env bash
java -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -jar build/libs/crawler-all-1.0-SNAPSHOT.jar -clean -populate -crawl -parse
