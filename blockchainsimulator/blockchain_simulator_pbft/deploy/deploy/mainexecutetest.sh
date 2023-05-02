#!/bin/bash
#cd ./rancher-v2.4.10/

# Remember for BootNode:

#Volume: /etc/testnet/bootnode is not deleted automatically

#REf: https://stackoverflow.com/questions/61747724/how-to-remove-mounted-volumes-pv-pvc-wont-deleteeditpatch

#Deleting manually in the pod seems to work rm -rf /etc/testnet/bootnode

#echo "Get the Shell for the Test Machine"
#./rancher kubectl exec -it -n dltsim-net $(./rancher kubectl -n dltsim-net get pods | awk '/ubuntu-/{printf $1}') -- bash

NBNODES=$1
echo "No of Nodes"
echo $NBNODES
#######################################################################################
#Reinitialise Variables
NWTYPE=$2
BLSIZE=$3
NLATENCY=$4
NBEHAVIOUR=$5
TOTXS=$6
#Divide tx for each node
NOTXS=$((TOTXS / NBNODES))

########################################################################################
value=$((NBNODES - 1))
iter=$value

echo "Clone the Git Repo in the Test Machine"
./rancher kubectl exec -it -n dltsim-net $(./rancher kubectl -n dltsim-net get pods | awk '/ubuntu-/{printf $1}') -- bash -c "git clone https://cyrilnavessamuel:nebzRuvGqLhHmgafxg6y@bitbucket.org/cyrilnavessamuel/pbftdltsimulatordeploy.git"
#######################################################################################

rm -rf nohup.out

echo "Launch the Test"

for ((i = 0; i <= $iter; ++i)); do
    nohup ./rancher kubectl exec -it -n dltsim-net $(./rancher kubectl -n dltsim-net get pods | awk '/ubuntu-/{printf $1}') -- bash -c "bash /pbftdltsimulatordeploy/deploy/podScriptTest.sh "$i" "$NBNODES" "$NWTYPE" "$BLSIZE" "$NLATENCY" "$NBEHAVIOUR" true "$NOTXS"" &
done
