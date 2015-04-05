FROM ubuntu:14.10
MAINTAINER Rafael Gálvez-Cañero <galvesband@gmail.com>, Andreas Gerstmayr <andreas.gerstmayr@gmail.com>

# OpenJDK 8 JRE, unix tools
RUN DEBIAN_FRONTEND=noninteractive sed -i "/^# deb.*universe/ s/^# //" /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y openjdk-8-jre-headless wget nano && \
    echo "Adding user vertx-user..." && \
    useradd -s /sbin/nologin -c "Vertx user" -m vertx-user

# install vert.x
WORKDIR /opt
RUN wget https://bintray.com/artifact/download/vertx/downloads/vert.x-2.1.5.tar.gz && \
    tar xvzf vert.x-2.1.5.tar.gz && \
    rm vert.x-2.1.5.tar.gz
RUN mkdir /opt/vert.x-2.1.5/sys-mods && chown vertx-user /opt/vert.x-2.1.5/sys-mods
ENV PATH /opt/vert.x-2.1.5/bin:$PATH

# change to vertx user and its home directory
USER vertx-user
WORKDIR /home/vertx-user

# preinstall required vert.x modules
RUN vertx install io.vertx~lang-groovy~2.1.1-final && \ 
    vertx install io.vertx~mod-mongo-persistor~2.1.1

# add packaged module (generated with `./gradlew modZip`)
# and configuration
COPY build/libs/gameregistry-0.0.1.zip gameregistry.zip
COPY conf-docker.json conf.json

# expose ports and start module
EXPOSE 1080
EXPOSE 8080
CMD ["vertx", "runZip", "gameregistry.zip", "-conf", "conf.json"]
