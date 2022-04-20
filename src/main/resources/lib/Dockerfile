FROM centos:7

MAINTAINER Libre

RUN mkdir /usr/local/jdk8

ADD jdk-8u202-linux-x64.tar.gz /usr/local/jdk8

ENV JAVA_HOME /usr/local/jdk8/jdk1.8.0_202

ENV JRE_HOME /usr/local/jdk8/jdk1.8.0_202/jre

ENV PATH $JAVA_HOME/bin:$PATH
