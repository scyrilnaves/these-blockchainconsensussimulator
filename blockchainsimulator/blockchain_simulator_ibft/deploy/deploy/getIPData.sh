#!/bin/bash

# Get the pod details
NBNODES=$1

rm -rf iplist.txt

rm -rf poddetail.json

# Get Pod Details
./rancher kubectl get pods --namespace=dltsim-net --sort-by=.metadata.creationTimestamp -o json >poddetail.json

echo "Wait for File"
sleep 20

# Get Only Pod IP's
for ((i = 0; i < $NBNODES; i++)); do
    ipOutput=$(jq '.items'[$i]'.status.podIP' poddetail.json)
    # Append output to the file line by line
    # tr -d to remove quotes
    # tee -a to append
    # /dev/null to avoid the output
    echo $ipOutput | tr -d '"' | tee -a iplist.txt >/dev/null
done
# Write IP's to txt
#echo $ipArray > iplist.txt
