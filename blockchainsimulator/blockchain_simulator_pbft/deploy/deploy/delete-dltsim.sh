#!/bin/bash
#cd ./rancher-v2.4.10/

# Remember for BootNode:

#Volume: /etc/testnet/bootnode is not deleted automatically

#REf: https://stackoverflow.com/questions/61747724/how-to-remove-mounted-volumes-pv-pvc-wont-deleteeditpatch

#Deleting manually in the pod seems to work rm -rf /etc/testnet/bootnode

./rancher kubectl delete -f dltsim-kube.yaml --force

echo "Done"