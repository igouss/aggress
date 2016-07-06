#!/bin/bash
set -e

CLASSPATH=$(JARS=("${APP_DIR}"/*.jar); IFS=:; echo "${JARS[*]}")

# Add java as command if needed
if [ "${1:0:1}" = '-' ]; then
	set -- java "$@"
fi

# Drop root privileges if we are running elasticsearch
# allow the container to be started with `--user`
if [ "$1" = 'java' -a "$(id -u)" = '0' ]; then
	chown -R crawler:aggress ${APP_DIR}
	set -- gosu crawler "$@"
fi

# As argument is not related to elasticsearch,
# then assume that user wants to run his own process,
# for example a `bash` shell to explore this image
exec "$@"


java -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -cp ${CLASSPATH} com.naxsoft.Crawler  -crawl  -parse