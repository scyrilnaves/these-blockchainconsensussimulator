#!/bin/bash

#cd ./rancher-v2.4.10/

echo "Load Deployments"
./rancher kubectl create -f dltsim-kube.yaml --validate=false

echo "Done"