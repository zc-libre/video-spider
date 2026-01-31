FROM maven:3.9-eclipse-temurin-21 AS build

ARG MAVEN_USERNAME
ARG MAVEN_PASSWORD

ENV MAVEN_USERNAME=${MAVEN_USERNAME}
ENV MAVEN_PASSWORD=${MAVEN_PASSWORD}

WORKDIR /build

COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src/main/resources/lib ./src/main/resources/lib
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM registry.cn-hangzhou.aliyuncs.com/libre/jdk:21

LABEL maintainer="Libre <zc150622@gmail.com>"

WORKDIR /libre

COPY --from=build /build/target/*.tar.gz /libre/
RUN tar -xzf *.tar.gz && rm *.tar.gz

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 9000

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app/boot/*.jar"]
