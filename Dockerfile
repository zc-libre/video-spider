FROM jdk/video:1.0

MAINTAINER Libre "zc150622@gmail.com"

VOLUME  /tmp

WORKDIR /libre

ADD app-app.tar.gz /libre

ENV LANG en_US.UTF-8

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 9870

CMD java  -jar /libre/app/boot/Video91-0.0.1-SNAPSHOT.jar $JAVA_OPTS
