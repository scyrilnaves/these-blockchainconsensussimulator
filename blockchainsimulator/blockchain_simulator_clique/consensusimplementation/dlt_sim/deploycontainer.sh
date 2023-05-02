#!/bin/sh

# DOCKER PART

echo "REMOVE-CONTAINER"
docker rmi --force cyrilthese/dltsim-clique:latest

echo "CONTAINER-BUILD"
docker build -f dltdocker -t cyrilthese/dltsim-clique:latest --no-cache .

echo "CONTAINER-PUSH"
docker push cyrilthese/dltsim-clique:latest
