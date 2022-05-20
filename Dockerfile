FROM jdk/video:1.0

MAINTAINER Libre "zc150622@gmail.com"

VOLUME  /tmp

WORKDIR /libre

RUN yum install kde-l10n-Chinese -y
RUN yum install glibc-common -y
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8

ADD video-spider-1.0.0-app.tar.gz /libre

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV LC_ALL zh_CN.UTF-8

EXPOSE 9870

CMD java -jar /libre/video-spider-1.0.0/bin/boot/video-spider-1.0.0.jar $JAVA_OPTS --jasypt.encryptor.password=504879189zc..
