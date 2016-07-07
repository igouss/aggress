# Crawler
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java:8-jre-alpine
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/aggress
ENV LOG_DIR $APP_DIR/logs

RUN mkdir -p $APP_DIR $LOG_DIR && addgroup aggress && adduser -D -G aggress crawler
VOLUME ["$LOG_DIR"]
WORKDIR $APP_DIR
COPY ["*.jar", "run.sh", "${APP_DIR}/"]

RUN chown -R crawler:aggress .

USER crawler
CMD $APP_DIR/run.sh


