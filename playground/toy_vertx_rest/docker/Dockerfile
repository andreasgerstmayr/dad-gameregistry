FROM ubuntu:14.10
MAINTAINER Rafael Gálvez-Cañero <galvesband@gmail.com>

RUN DEBIAN_FRONTEND=noninteractive sed -i "/^# deb.*universe/ s/^# //" /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y openjdk-8-jdk wget nano && \
    echo "Adding user vertx-user..." && \
    useradd -s /sbin/nologin -c "Vertx user" -m vertx-user

WORKDIR /opt
RUN wget https://bintray.com/artifact/download/vertx/downloads/vert.x-2.1.5.tar.gz && \
    tar xvzf vert.x-2.1.5.tar.gz && \
    rm vert.x-2.1.5.tar.gz
ENV PATH="/opt/vert.x-2.1.5/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

USER vertx-user
WORKDIR /home/vertx-user
CMD ["/bin/bash"]
