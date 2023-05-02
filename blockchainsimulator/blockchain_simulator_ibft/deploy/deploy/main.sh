#!/bin/bash
cd "$(dirname "$0")"

echo "Hope you have set the configuration"
echo "Results are colated in Results Folder"
sleep 10
echo "http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp"
# N-3 Node
echo "http://dltsim-dash-mid.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp"
# N-1 Node
echo "http://dltsim-dash-last.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp"

echo "http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getVersion"
sleep 10
echo "OK Let's Start"
###########################################
##CONFIGURATION
###########################################
consensus=IBFT
totaltxs=3000
noofnodes=(10 20)
noofvalidators=(5 10)
networktype=(mesh ringlattice wattsstrogatz)
latency=(0 10 30 50)
isvalidator=true
#millisecs
roundchangetimeout=60000
blocksize=(500 1000 2000 3000 5000)
nodebehaviour=0
###########################################
for noofnode in "${noofnodes[@]}"; do
  for noofvalidator in "${noofvalidators[@]}"; do
    for nwtype in "${networktype[@]}"; do
      for nwlaten in "${latency[@]}"; do
        for blsize in "${blocksize[@]}"; do

          echo "Main Cloud Deployment Started"
          ./mainredeploy.sh $noofnode

          ##START OF TEST
          starttime=$(date +%s)

          echo "Main Test Execution Started"
          ./mainexecutetest.sh $noofnode $nwtype $blsize $nwlaten $nodebehaviour $totaltxs $noofvalidator $isvalidator $roundchangetimeout

          testtype="NoNode:"$noofnode";NwType:"$nwtype";BlockSize:"$blsize";NwLatency:"$nwlaten";NodeBehaviour:"$nodebehaviour";TotalTxs:"$totaltxs";Consensus:"$consensus";TotalValidators:"$noofvalidator";IsValidator:"$isvalidator";RoundChange:"$roundchangetimeout
          # write test type to a d
          echo $testtype >>results/testtype.txt
          #Do CURL to get values and stop test
          txvalidated=$(curl -s http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getTransactionsValidatedNo | tr -d '"')
          txthreshold=$((totaltxs - 1000))
          sleep 60
          while (("$txvalidated" < "$txthreshold")); do
            #used tr to remove double quotes
            txvalidated=$(curl -s http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getTransactionsValidatedNo | tr -d '"')
            echo "waiting"
            sleep 20
          done
          echo "exited"
          sleep 30
          endtime=$(date +%s)
          delta=$((endtime - starttime))
          ############################################################################################################################################################
          echo "stopping collators"
          #used to stop result collator of all
          curl -s http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/stopResultCollator
          curl -s http://dltsim-dash-mid.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/stopResultCollator
          curl -s http://dltsim-dash-last.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/stopResultCollator

          ############################################################################################################################################################
          # collect the results from 3 resources in local
          mkdir results
          mkdir results/$testtype
          #iterate from 3 different points for comparison
          endpoints=(first mid last)
          for endpoint in "${endpoints[@]}"; do
            #make a subdirectory for each endpoint
            mkdir results/$testtype/$endpoint
            urlcontext=''
            if [ "$endpoint" == "first" ]; then
              urlcontext=dltsim-dash.unice.cust.tasfrance.com
            elif [ "$endpoint" == "mid" ]; then
              urlcontext=dltsim-dash-mid.unice.cust.tasfrance.com
            else
              urlcontext=dltsim-dash-last.unice.cust.tasfrance.com
            fi
            echo $urlcontext
            #Transactions validated
            curl -s -o results/$testtype/$endpoint/txvalidated.json http://$urlcontext/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getTransactionsValidatedNo | tr -d '"'
            #Processed TPS
            curl -s -o results/$testtype/$endpoint/processedtps.json http://$urlcontext/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getProcessedTPS
            #Finalised TPS
            curl -s -o results/$testtype/$endpoint/finalisedtps.json http://$urlcontext/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getFinalisedTPS
            #Prepare Rate
            curl -s -o results/$testtype/$endpoint/preparetps.json http://$urlcontext/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getPrepareRate
            #Commit Rate
            curl -s -o results/$testtype/$endpoint/committps.json http://$urlcontext/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getCommitRate
            #Round Change Rate
            curl -s -o results/$testtype/$endpoint/roundchangetps.json http://$urlcontext/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getRoundChangeRate

            echo "TimeDifference:"$delta >results/$testtype/$endpoint/duration.txt
            echo "finished"
          done
        done
      done
    done
  done
done
echo "cleaning all Do you want to clean?"
sleep 10
./cleandeploy.sh
