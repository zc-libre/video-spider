FROM registry.cn-hangzhou.aliyuncs.com/libre/jdk:11

MAINTAINER Libre "zc150622@gmail.com"

VOLUME  /tmp

WORKDIR /libre

ADD  target/*.tar.gz /libre

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom"

ENV LC_ALL zh_CN.UTF-8

EXPOSE 9870

CMD java $JAVA_OPTS -jar app/boot/*.jar --jasypt.encryptor.password=504879189zc..
