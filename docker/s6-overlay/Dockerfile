FROM alpine:3.4
MAINTAINER Iouri Goussev

# Add s6-overlay
ENV S6_OVERLAY_VERSION=v1.17.2.0

USER root

RUN apk add --no-cache curl \
 && curl -sSL https://github.com/just-containers/s6-overlay/releases/download/${S6_OVERLAY_VERSION}/s6-overlay-amd64.tar.gz | tar xfz - -C / \
 && apk del curl


ENTRYPOINT ["/init"]
CMD []
