# Crawler
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java:8-jre-alpine
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/aggress
ENV LOG_DIR $APP_DIR/logs

RUN groupadd -r aggress && useradd -r -g aggress crawler
USER crawler

RUN mkdir $APP_DIR $LOG_DIR
VOLUME ["$LOG_DIR"]
WORKDIR $APP_DIR
COPY ["dist", "$APP_DIR"]
# grab gosu for easy step-down from root
ENV GOSU_VERSION 1.7
RUN set -x \
	&& wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture)" \
	&& wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture).asc" \
	&& export GNUPGHOME="$(mktemp -d)" \
	&& gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
	&& gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
	&& rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc \
	&& chmod +x /usr/local/bin/gosu \
	&& gosu nobody true

ENTRYPOINT ["/run.sh"]
CMD ["java"]

