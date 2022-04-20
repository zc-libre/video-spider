#!/bin/bash
docker stop app
docker rm app
rm -rf maven* classes
docker rmi app:1.0
cd /root/app/app/bin
docker build -t app:1.0 .
docker run -it --name app -p 9870:9870 -v /root/app/app/config:/libre/app/config  \
      -v /root/app/app/logs:/libre/logs -d app:1.0
