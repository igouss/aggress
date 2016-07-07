# Frontend
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java:8-jre-alpine
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/frontend
ENV LOG_DIR ${APP_DIR}/logs

RUN mkdir -p ${APP_DIR} ${LOG_DIR} ${APP_DIR}/basedir && addgroup aggress && adduser -D -G aggress frontend
WORKDIR ${APP_DIR}
COPY ["../frontend/build/libs/*.jar", "run.sh", "${APP_DIR}/"]
COPY ["../frontend/basedir", "${APP_DIR}/basedir"]

RUN chown -R frontend:aggress .

USER frontend
CMD ${APP_DIR}/run.sh
