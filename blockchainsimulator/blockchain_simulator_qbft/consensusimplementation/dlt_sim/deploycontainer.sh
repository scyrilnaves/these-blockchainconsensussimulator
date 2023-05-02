#!/bin/sh

# DOCKER PART

echo "REMOVE-CONTAINER"
docker rmi --force cyrilthese/dltsim-qbft:latest

echo "CONTAINER-BUILD"
docker build -f dltdocker -t cyrilthese/dltsim-qbft:latest --no-cache .

echo "CONTAINER-PUSH"
docker push cyrilthese/dltsim-qbft:latest
