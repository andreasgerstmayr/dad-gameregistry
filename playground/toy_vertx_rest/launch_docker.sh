#!/bin/bash
VERTICLE=$1
PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/src
CONTAINER_DIR=/home/vertx-user/project_src
CONTAINER_NAME="vertx_instance_`echo $RANDOM`"
IMAGE_NAME=galvesband/vertx:0.3
OPEN_PORT=1080

# Check argument number
if [ ! $# -eq 1 ]; then
  echo "ERROR: Incorrect number of arguments."
  echo "Usage: launch_docker.sh <verticle_name>"
  exit
fi

echo "Launching $IMAGE_NAME image as $CONTAINER_NAME to run verticle $VERTICLE"
echo " Project dir: $PROJECT_DIR (mounted as ro)"
echo " Container dir: $CONTAINER_DIR"
docker run -t --name="$CONTAINER_NAME" -p $OPEN_PORT:$OPEN_PORT -v $PROJECT_DIR:$CONTAINER_DIR:ro $IMAGE_NAME vertx run project_src/$VERTICLE
#docker run -i -t --name="$CONTAINER_NAME" -p $OPEN_PORT:$OPEN_PORT -v $PROJECT_DIR:$CONTAINER_DIR:ro $IMAGE_NAME

echo "Removing container $CONTAINER_NAME..."
docker rm $CONTAINER_NAME 
