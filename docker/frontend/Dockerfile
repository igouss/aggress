# Frontend
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java-8
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/frontend
ENV LOG_DIR ${APP_DIR}/logs

RUN mkdir -p ${APP_DIR} ${LOG_DIR} ${APP_DIR}/basedir \
    && addgroup aggress \
    && adduser -D -G aggress -s /bin/sh frontend

WORKDIR ${APP_DIR}

COPY ["s6", "/"]
COPY ["deps/*.jar", "app/*.jar", "${APP_DIR}/"]
COPY ["basedir", "${APP_DIR}/basedir"]