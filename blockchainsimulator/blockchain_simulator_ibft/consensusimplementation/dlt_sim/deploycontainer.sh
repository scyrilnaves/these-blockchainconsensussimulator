#!/bin/sh

# DOCKER PART

echo "REMOVE-CONTAINER"
docker rmi --force cyrilthese/dltsim-ibft:latest

echo "CONTAINER-BUILD"
docker build -f dltdocker -t cyrilthese/dltsim-ibft:latest --no-cache .

echo "CONTAINER-PUSH"
docker push cyrilthese/dltsim-ibft:latest
