#!/bin/sh
cd "$(dirname "$0")"
NBINDEX=$1

#Frist Argument: Port 8080 ... 8081
#Second Argument: Instance No: 0,...1 etc

#  mesh or   or wattsStrogatz
#nodelatency--> in millisecs = 30 -40
#nodebehavior 0 -- HONEST ||| 1 --DISHONEST
# Affects Latency and Conensus Prepare phase

###############################################################################################################################################################
#                       $1         $2        $3           $4         $5           $6          $7         $8  $9              $10           $11
# ./podScriptTest.sh nodeindex totalnodes  nodenetwork blocksize  nodelatency nodebehavior fireboolean notxs totalvalidators isvalidator  roundchangetimeout
###############################################################################################################################################################
#                    $1 $2  $3  $4  $5  $6  $7    $8   $9 $10  $11
# ./podScriptTest.sh 0  4 mesh 100  0  0  true   5000  4  true 60000
################################################################################################################################################################
# Get the IP and IP of the Peers from IP List
filename='iplist.txt'
peeriparray=''
myip=
myport=8080
threshold=4
n=0
for ip in $(cat iplist.txt); do
  if [ $n == $NBINDEX ]; then
    myip=$ip
    peeriparray+=${ip}
    peeriparray+=','
    #echo $ip;
  else
    peeriparray+=${ip}
    peeriparray+=','
  fi
  echo "N Value:"$n
  n=$((n + 1))
done
# Formed Peer IP to Connect for Peer $NBINDEX
echo "Formed Peer IP to Connect for Peer:" $NBINDEX
echo "My IP:" $myip
echo "Peers are:"$peeriparray
#######################################################################################################################

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setNodeNetwork/$3

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setPeers/$peeriparray

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setBlockSize/$4

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setTotalNodes/$2

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setTotalValidators/$9

#new : Set Total Nodes, Total Validators before set node property

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setnodeProperty/$NBINDEX

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setNodeLatency/$5

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setNodeBehavior/$6

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setAsValidator/${10}

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setRoundChange/${11}

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/initiateConnection

sleep 20

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/listentoPeers

# Allow to connect and establish connection with peers sufficiently
sleep 20

#Start Result Collator

curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startResultCollator

if [ "$NBINDEX" -le "$threshold" ]; then

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation1/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation2/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation3/$8

  curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation4/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation5/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation6/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation7/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation8/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation9/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulationsingle/$8

  #curl -s http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulationsingle/$8

  #sleep 500
  #curl http://$myip:$myport/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/stopResultCollator
fi
