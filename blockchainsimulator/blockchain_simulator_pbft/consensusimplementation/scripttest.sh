
#!/bin/sh

#Frist Argument: Port 8080 ... 8081
#Second Argument: Instance No: 0,...1 etc

#  mesh or ringLattice or wattsStrogatz

# ./scripttest.sh port nodenetwork blocksize totalnodes nodeproperty nodelatency nodebehavior | fireboolean
#nodelatency--> in millisecs = 30 -40
#nodebehavior 0 -- HONEST ||| 1 --DISHONEST
# Affects Latency and Conensus Prepare phase
########################################################################################################################
#                  $1   $2  $3 $4$5$6 $7 $8   $9
# ./scripttest.sh 8080 mesh 100 4 0 0 0 true 5000

# ./scripttest.sh 9080 mesh 100 4 1 0 0 true 5000

# ./scripttest.sh 8088 mesh 100 4 2 0 0 true 5000

# ./scripttest.sh 8089 mesh 100 4 3 0 0 true 5000
########################################################################################################################
#                  $1   $2           $3 $4$5$6 $7 $8   $9
# ./scripttest.sh 8080 wattsStrogatz 100 4 0 0 0 true 5000

# ./scripttest.sh 9080 wattsStrogatz 100 4 1 0 0 true 5000

# ./scripttest.sh 8088 wattsStrogatz 100 4 2 0 0 true 5000

# ./scripttest.sh 8089 wattsStrogatz 100 4 3 0 0 true 5000
########################################################################################################################
# ./scripttest.sh 8080 ringLattice 100 4 0 0 0 true 5000

# ./scripttest.sh 9080 ringLattice 100 4 1 0 0 true 5000

# ./scripttest.sh 8088 ringLattice 100 4 2 0 0 true 5000

# ./scripttest.sh 8089 ringLattice 100 4 3 0 0 false 5000
##########################################################################################################################
#http://localhost:8080/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getVersion

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getVersion

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setNodeNetwork/$2

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setBlockSize/$3

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setTotalNodes/$4

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setnodeProperty/$5

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setNodeLatency/$6

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/setNodeBehavior/$7

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getnodeProperty

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getNodeBehavior

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getBlockSize

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getNodeLatency

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/initiateConnection

sleep 20

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/listentoPeers

# Allow to connect and establish connection with peers sufficiently
sleep 20

#Start Result Collator

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startResultCollator

if $8; then 

#curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/sendMessageToPeer/cyrilmessage

#curl http://localhost:8080/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getBlockNo

#curl http://localhost:9080/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getBlockNo

curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulationsingle/$9

#curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation/100/1

#curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulation/100000/4

#curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulationsingle/90

#curl http://localhost:8080/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/startsimulationsingle/100000
sleep 500
curl http://localhost:$1/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/stopResultCollator
fi 

#To check Block No
#curl http://localhost:8080/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getBlockNo

#curl http://localhost:8080/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getTransactionsValidatedNo
