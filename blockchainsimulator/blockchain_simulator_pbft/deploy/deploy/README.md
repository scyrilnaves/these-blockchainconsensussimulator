# About

https://rancher.com/docs/rancher/v2.x/en/cli/

https://gist.github.com/sharepointoscar/0c35e6fb9151a1967bd68253b1bf802f


## Requirements

- AMD64 Arch (or download and replace the ./rancher binary)
- https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/#install-using-native-package-management
- Rancher API Key (with no scope added !)
<!-- - `export RANCHER_BEARER_TOKEN=token-XXXX` -->

# Execute Following for Deploy

1) generateMain.sh [No of Nodes]
2) deploy-dltsim.sh or delete-dltsim.sh
# Parse Data of IP from cloud kubernetes
3) getIPData.sh [No of Nodes]
4) Commit the files to GIT

OR

./mainredeploy.sh
# Second Node [index at 1, after 0]
http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp
# N-3 Node
http://dltsim-dash-mid.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp
# N-1 Node
http://dltsim-dash-last.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp

http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getVersion

# Enter the ubuntu VM 

1) clone the deploy repo

git clone https://cyrilnavessamuel@bitbucket.org/cyrilnavessamuel/dltsimulatordeploy.git

2) Execute the launchPodTest.sh [No of Nodes]

# SIMPLE:
  # For Build and container deploy
  ./maindeploy.sh
  # For Deploy and test firing
  ./main.sh [No of nodes]



