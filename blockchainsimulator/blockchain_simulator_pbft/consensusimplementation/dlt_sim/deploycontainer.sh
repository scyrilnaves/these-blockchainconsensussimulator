#!/bin/sh

# DOCKER PART

echo "REMOVE-CONTAINER"
docker rmi --force cyrilthese/dltsim-pbft:latest

echo "CONTAINER-BUILD"
docker build -f dltdocker -t cyrilthese/dltsim-pbft:latest --no-cache .

echo "CONTAINER-PUSH"
docker push cyrilthese/dltsim-pbft:latest
