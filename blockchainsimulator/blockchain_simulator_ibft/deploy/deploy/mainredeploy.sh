#!/bin/bash
if [ $# -eq 0 ]; then
  echo "Enter the no of nodes to deploy"
  sleep 6000
fi

#delete the old deployement
echo "Deleted the old deployment"
./delete-dltsim.sh

sleep 20

#generate the Yaml
echo "Generate the new main "
./generatemain.sh $1

sleep 20

#deploy the new deployment
echo "Deploy the new "
./deploy-dltsim.sh

#Wait for certain time
echo "Wait for Deployment Initialisation"
sleep 120

#get the ip
echo "IPs Retrived"
./getIPData.sh $1

#Add the Changes
echo "Git added the changes"
git add .

#Form the comment
timestamp=$(date +%s)
comment='deploymentchanges'+$timestamp
echo $comment

#Commit the changes
echo "Git committed the changes"
git commit -m "$comment'"

#Push the changes
echo "Git pushed the changes"
git push
