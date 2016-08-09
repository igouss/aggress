# Crawler
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java-8
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/crawler
ENV LOG_DIR ${APP_DIR}/logs

USER root

RUN mkdir -p ${APP_DIR} ${LOG_DIR} \
 && addgroup aggress \
 && adduser -D -G aggress -s /bin/sh crawler \

WORKDIR ${APP_DIR}

COPY ["s6", "/"]
COPY ["deps/*.jar", "app/*.jar", "cert/*.sh", "cert/*.crt", "*.sh", "${APP_DIR}/"]

EXPOSE 8000

RUN chown -R crawler:aggress ${APP_DIR} && ./import-certs.sh
