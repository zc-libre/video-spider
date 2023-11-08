FROM registry.cn-hangzhou.aliyuncs.com/libre/jdk:11

MAINTAINER Libre "zc150622@gmail.com"

VOLUME  /tmp

WORKDIR /libre

ADD  target/*.tar.gz /libre

RUN mkdir /opt/clash && cd /opt/clash \
            &&  wget https://dl3.ssrss.club/clash-linux-amd64-v1.9.0.gz  \
            && wget -O /opt/clash/config.yaml "https://new.ssrss.de/xxx" \
            && wget https://dl3.ssrss.club/Country.mmdb \
            && gunzip -c *.gz > clash && chmod +x clash \
            && cat > /usr/lib/systemd/system/clash.service <'EOF' \
              [Unit] \
              Description=clash \
              [Service] \
              TimeoutStartSec=0 \
              ExecStart=/opt/clash/clash -d /opt/clash \
              [Install] \
              WantedBy=multi-user.target \
              EOF \
           && nohup /opt/clash/clash -d /opt/clash > /dev/null 2>&1 &

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom"

ENV LC_ALL zh_CN.UTF-8

ENV ALL_PROXY http://127.0.0.1:7890
ENV http_proxy http://127.0.0.1:7890
ENV https_proxy http://127.0.0.1:7890

EXPOSE 9870

CMD java $JAVA_OPTS -jar app/boot/*.jar

ENTRYPOINT ["sh","-c","java  ${JAVA_OPTS} -jar app/boot/*.jar --spinrg.profiles.active=${ACTIVE} --jasypt.encryptor.password=${VIDEO_PASSWORD}"]
