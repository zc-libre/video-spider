#!/bin/bash
WORK_DIR=/root/app
APP_NAME=video-spider-1.0.0

tar -zxvf $WORK_DIR/package.tgz  -C $WORK_DIR
tar -zxvf $WORK_DIR/$APP_NAME-app.tar.gz -C $WORK_DIR
docker stop app
docker rm app
cd $WORK_DIR
rm -rf maven* classes arc* generated-sources lib
cd $WORK_DIR/$APP_NAME/bin
cp $WORK_DIR/video-spider-1.0.0-app.tar.gz .
sudo docker rmi app:1.0
docker build -t app:1.0 .
docker run -it --name app \
 -p 9870:9870 \
 -v /root/app/$APP_NAME/config:/libre/$APP_NAME/config \
 -v /root/app/$APP_NAME/logs:/libre/logs \
 -v /root/video/:/libre/video/ \
 -d app:1.0
