# syntax=docker/dockerfile:1.4
FROM maven:3.9-eclipse-temurin-21 AS build

ARG MAVEN_USERNAME
ARG MAVEN_PASSWORD

ENV MAVEN_USERNAME=${MAVEN_USERNAME}
ENV MAVEN_PASSWORD=${MAVEN_PASSWORD}

WORKDIR /build

COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src/main/resources/lib ./src/main/resources/lib

# 安装本地 jar 到 Maven 仓库并下载依赖
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn install:install-file -Dfile=src/main/resources/lib/jave-1.0.2.jar \
        -DgroupId=it.sauronsoftware -DartifactId=jave -Dversion=1.0.2 -Dpackaging=jar && \
    mvn install:install-file -Dfile=src/main/resources/lib/bcprov-jdk16-139.jar \
        -DgroupId=org.bouncycastle -DartifactId=bcprov-jdk16-139 -Dversion=1.0.0 -Dpackaging=jar && \
    mvn dependency:go-offline -B

COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn install:install-file -Dfile=src/main/resources/lib/jave-1.0.2.jar \
        -DgroupId=it.sauronsoftware -DartifactId=jave -Dversion=1.0.2 -Dpackaging=jar && \
    mvn install:install-file -Dfile=src/main/resources/lib/bcprov-jdk16-139.jar \
        -DgroupId=org.bouncycastle -DartifactId=bcprov-jdk16-139 -Dversion=1.0.0 -Dpackaging=jar && \
    mvn package -DskipTests -Dmaven.test.skip=true -B

FROM registry.cn-hangzhou.aliyuncs.com/libre/jdk:21

LABEL maintainer="Libre <zc150622@gmail.com>"

WORKDIR /libre

COPY --from=build /build/target/*.tar.gz /libre/
RUN tar -xzf *.tar.gz && rm *.tar.gz

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 9000

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app/boot/*.jar"]
