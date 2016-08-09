# Crawler
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java-8
MAINTAINER Iouri Goussev

ENV APP_DIR /opt/crawler \
    LOG_DIR /opt/crawler/logs

USER root

RUN mkdir -p ${APP_DIR} ${LOG_DIR} \
 && addgroup aggress \
 && adduser -D -G aggress -s /bin/sh crawler

WORKDIR ${APP_DIR}

COPY ["s6", "/"]
COPY ["deps/*.jar", "app/*.jar", "cert/*.sh", "cert/*.crt", "${APP_DIR}/"]

EXPOSE 8000

RUN ./import-certs.sh
