# Crawler
#
# VERSION               0.0.1
# HEALTHCHECK healthcheck.sh

FROM java:8-jre-alpine
MAINTAINER Iouri Goussev

ENV PROJECT_ROOT .
ENV APP_DIR /opt/crawler
ENV LOG_DIR ${APP_DIR}/logs

# Add s6-overlay
ENV S6_OVERLAY_VERSION=v1.17.2.0 \
    GODNSMASQ_VERSION=1.0.6

ENV CONSUL_VERSION=0.6.4 \
    CONSUL_DOMAIN=consul \
    CONSUL_CONFIG_DIR=/etc/consul/conf.d/bootstrap \
    CONSUL_SERVER_NAME=consul

USER root

RUN mkdir -p ${APP_DIR} ${LOG_DIR} \
 && addgroup aggress \
 && adduser -D -G aggress crawler \
 && apk add --no-cache bind-tools \
 && apk add --no-cache curl \
 && curl -sSL https://github.com/just-containers/s6-overlay/releases/download/${S6_OVERLAY_VERSION}/s6-overlay-amd64.tar.gz | tar xfz - -C / \
 && curl -sSL https://github.com/janeczku/go-dnsmasq/releases/download/${GODNSMASQ_VERSION}/go-dnsmasq-min_linux-amd64 -o /bin/go-dnsmasq  \
 && chmod +x /bin/go-dnsmasq \
 && curl -sSLo /tmp/consul.zip https://releases.hashicorp.com/consul/{$CONSUL_VERSION}/consul_${CONSUL_VERSION}_linux_amd64.zip \
 && unzip -d /bin /tmp/consul.zip \
 && rm /tmp/consul.zip \
 && apk del curl \
 && addgroup consul \
 && adduser -D -g "" -s /bin/sh -G consul consul \
 && mkdir -p /data/consul \
 && chown -R consul:consul /data/consul \

WORKDIR ${APP_DIR}

COPY services consul config /

COPY ["deps/*.jar", "cert/*.sh", "cert/*.crt", "*.sh", "${APP_DIR}/"]
COPY ["app/*.jar", "${APP_DIR}/"]

VOLUME ["/data/consul"]

EXPOSE 8300 8301 8301/udp 8302 8302/udp 8400 8500 53 53/udp

RUN chown -R crawler:aggress ${APP_DIR} && ./import-certs.sh

USER crawler

ENTRYPOINT ["/init"]
CMD []
