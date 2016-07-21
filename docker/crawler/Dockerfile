# Crawler
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java:8-jre-alpine
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/crawler
ENV LOG_DIR ${APP_DIR}/logs

USER root
RUN mkdir -p ${APP_DIR} && mkdir ${LOG_DIR} && addgroup aggress && adduser -D -G aggress crawler

WORKDIR ${APP_DIR}

COPY ["deps/*.jar", "cert/*.sh", "cert/*.crt", "*.sh", "${APP_DIR}/"]
COPY ["app/*.jar", "${APP_DIR}/"]

RUN chown -R crawler:aggress ${APP_DIR} && ./import-certs.sh
USER crawler

CMD ${APP_DIR}/run.sh
