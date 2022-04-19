FROM moxm/java:1.8-full

MAINTAINER Libre "zc150622@gmail.com"

RUN mkdir -p /libre

WORKDIR /libre

ARG JAR_FILE=*.jar

COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Shanghai

EXPOSE 9870

ENTRYPOINT ["java","-jar","app.jar"]
















