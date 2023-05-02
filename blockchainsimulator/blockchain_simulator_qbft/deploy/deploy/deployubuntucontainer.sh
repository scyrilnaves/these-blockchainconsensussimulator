#!/bin/sh

# DOCKER PART
echo "REMOVE CONTAINER"
docker rmi --force cyrilthese/masternode:latest

echo "CONTAINER BUILD"
docker build -f masternode -t cyrilthese/masternode:latest . --no-cache

echo "CONTAINER PUSH"
docker push cyrilthese/masternode:latest