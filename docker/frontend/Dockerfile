# Frontend
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java:8-jre-alpine
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/frontend
ENV LOG_DIR ${APP_DIR}/logs

RUN mkdir -p ${APP_DIR} ${LOG_DIR} && addgroup aggress && adduser -D -G aggress frontend
VOLUME ["$LOG_DIR"]
WORKDIR ${APP_DIR}
COPY ["*.jar", "run.sh", "basedir", "${APP_DIR}/"]

RUN chown -R frontend:aggress .

USER frontend
CMD ${APP_DIR}/run.sh
