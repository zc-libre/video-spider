#!/bin/bash
tar -zxvf package.tgz
tar -zxvf app-app.tar.gz
docker stop app
docker rm app
cd /root/app
rm -rf maven* classes
sudo docker rmi app:1.0
docker build -t app:1.0 .
docker run -it --name app -p 9870:9870 -v /root/app/app/config:/libre/app/config  -v /root/app/app/logs:/libre/logs -d app:1.0
