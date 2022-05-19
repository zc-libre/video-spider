FROM centos:7

MAINTAINER Libre

RUN mkdir /usr/local/jdk8

RUN yum install kde-l10n-Chinese -y
RUN yum install glibc-common -y
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8

ADD jdk-8u202-linux-x64.tar.gz /usr/local/jdk8

ENV LC_ALL zh_CN.UTF-8

ENV TZ=Asia/Shanghai

ENV JAVA_HOME /usr/local/jdk8/jdk1.8.0_202

ENV JRE_HOME /usr/local/jdk8/jdk1.8.0_202/jre

ENV PATH $JAVA_HOME/bin:$PATH
