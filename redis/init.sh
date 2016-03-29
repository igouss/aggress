#!/bin/bash

apt-get update > /dev/null
#apt-get -y upgrade
apt-get -y install make

mkdir /opt/redis

cd /opt/redis
# Use latest stable
wget -q http://download.redis.io/redis-stable.tar.gz
# Only update newer files
tar -xz --keep-newer-files -f redis-stable.tar.gz

cd redis-stable
make
make install
mkdir -p /etc/redis

useradd redis

mkdir /var/redis

chmod -R 755 /var/redis && chown redis /var/redis
echo `pwd`
ls
cp -u redis.conf /etc/redis/6379.conf
cp -u redis.init.d /etc/init.d/redis_6379

update-rc.d redis_6379 defaults

chmod a+x /etc/init.d/redis_6379
/etc/init.d/redis_6379 start
