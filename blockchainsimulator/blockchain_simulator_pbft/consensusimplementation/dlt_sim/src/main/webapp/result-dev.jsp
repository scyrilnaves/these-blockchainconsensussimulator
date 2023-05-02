<!-- ADDED JAVA CLASS IMPORTS-->
<%@page import="org.renaultleat.properties.NodeProperty" %>
	<%@page import="org.renaultleat.node.Wallet" %>
		<!DOCTYPE html>
		<html lang="en">

		<head>
			<meta charset="utf-8">
			<title>DLT Simulator</title>
			<script src='chart/Chart.js' charset="utf-8"></script>
			<style>
				label {
					font-family: sans-serif;
				}

				svg {
					background: #eeeeee;
				}
			</style>
		</head>

		<body>
			<div id="logo">
				<h1>DLT Simulator</h1>
				<img src="images/leat.jpeg" alt="LEAT" width="300" height="150">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp;
				<img src="images/renault.png" alt="Renault" width="300" height="150">
			</div>
			<div id="details">
				<h3>DLT Property</h3>
				<b>TotalNodes:</b> <input type="text" id="TotalNodes" value="" readonly>&nbsp&nbsp;
				<b>NodeIndex:</b><input type="text" id="NodeIndex" value="" readonly>&nbsp&nbsp;
				<b>BlockSize:</b><input type="text" id="BlockSize" value="" readonly><br>&nbsp&nbsp;
				<b>NetworkLatency:</b><input type="text" id="NetworkLatency" value="" readonly>&nbsp&nbsp;
				<b>NodeBehaviour:</b><input type="text" id="NodeBehaviour" value="" readonly>&nbsp&nbsp;
				<b>NetworkType:</b><input type="text" id="NetworkType" value="" readonly><br>&nbsp&nbsp;
				<b>Node IP:</b> <input type="text" id="nodeip" value="" readonly>&nbsp&nbsp;
				<b>Peer IP:</b><textarea id="peerip" name="peerip" rows="4" cols="50"></textarea><br>
				<h3>DLT Network State</h3>
				<b>Blocks:</b> <input type="text" id="TotalBlocks" value="" readonly>&nbsp&nbsp;
				<b>Transactions Validated:</b> <input type="text" id="txvalidated" value="" readonly>&nbsp&nbsp;
			</div>
			<br>
			<div id="txgraph">
				<h3>Transaction Rate</h1>
					<canvas id="dltTX" style="width:100%;max-width:700px"></canvas>
			</div>
			<div id="bftgraph">
				<h3>Consensus Message Rate</h1>
					<canvas id="dltBFT" style="width:100%;max-width:700px"></canvas>
			</div>
			<script type="text/javascript">
				//Added all the necessary Node Property
				var totalNodes = '<%=NodeProperty.totalnodes %>';
				var nodeproperty = '<%=Wallet.nodeproperty %>';
				var blocksize = '<%=NodeProperty.blocksize %>';
				var latency = '<%=NodeProperty.latency %>';
				var behaviour = '<%=NodeProperty.nodeBehavior %>';
				var networkType = '<%=NodeProperty.nodeNetwork %>';
				var port = '<%=NodeProperty.getCurrentPort() %>';
				var ip = '<%=NodeProperty.getIP() %>';
				var peers = '<%=NodeProperty.getIPS() %>';

				document.getElementById("TotalNodes").value = totalNodes;
				document.getElementById("NodeIndex").value = nodeproperty;
				document.getElementById("BlockSize").value = blocksize;
				document.getElementById("NetworkLatency").value = latency;
				if (behaviour == "0") {
					document.getElementById("NodeBehaviour").value = "GOOD NODE :)";
				} else {
					document.getElementById("NodeBehaviour").value = "BAD NODE !! :(";
				}
				document.getElementById("NetworkType").value = networkType;
				document.getElementById("nodeip").value = ip;
				document.getElementById("peerip").value = peers;

				// Get the network State as API CALLS
				var request = new XMLHttpRequest()

				// BlockData
				request.open("GET", "http://localhost:" + port + "/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getBlockNo");
				request.send();
				request.onload = () => {
					const blockdata = JSON.parse(request.response);
					document.getElementById("TotalBlocks").value = blockdata;
				}
				// Transaction Validated
				// Get the network State as API CALLS
				var txvalidatedrequest = new XMLHttpRequest()
				txvalidatedrequest.open("GET", "http://localhost:" + port + "/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getTransactionsValidatedNo");
				txvalidatedrequest.send();
				txvalidatedrequest.onload = () => {
					const txvalidateddata = JSON.parse(txvalidatedrequest.response);
					document.getElementById("txvalidated").value = txvalidateddata;
				}
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// End API Calls
				//PROCESSED TX PART
				var processedtpstime = [];
				var processedtpsdata = [];

				//FINALISED TX PART
				var finalisedtpstime = [];
				var finalisedtpsdata = [];

				async function gettxData() {
					let txprocessedresponse = await fetch("http://localhost:" + port + "/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getProcessedTPS");
					let txprocesseddata = await txprocessedresponse.json();
					for (let i = 0; i < txprocesseddata.length; i++) {
						processedtpstime[i] = txprocesseddata[i].map.time;
						processedtpsdata[i] = Number(txprocesseddata[i].map.tps)
					}
					let txfinalisedresponse = await fetch("http://localhost:" + port + "/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getFinalisedTPS");
					let txfinaliseddata = await txfinalisedresponse.json();
					for (let i = 0; i < txfinaliseddata.length; i++) {
						finalisedtpstime[i] = txfinaliseddata[i].map.time;
						finalisedtpsdata[i] = Number(txfinaliseddata[i].map.tps)
					}
					return txfinaliseddata;
				}
				gettxData().then(data =>
					new Chart("dltTX", {
						//type: "bar",
						type: "line",
						data: {
							labels: processedtpstime,
							datasets: [{
								label: 'TPS Processed Rate',
								fill: false,
								lineTension: 0,
								borderWidth: 1,
								backgroundColor: "rgb(255, 99, 132)",
								borderColor: "rgb(255, 99, 132)",
								data: processedtpsdata
							}, {
								label: 'TPS Finalised Rate',
								fill: false,
								lineTension: 0,
								borderWidth: 1,
								backgroundColor: "rgb(255, 159, 64)",
								borderColor: "rgb(255, 159, 64)",
								data: finalisedtpsdata
							}]
						},
						options: {
							responsive: true,
							interaction: {
								mode: 'index',
								intersect: false,
							},
							stacked: false,
							plugins: {
								title: {
									display: true,
									text: 'Processed and Finalised TPS'
								}
							},
							legend: { display: true }/* ,
							scales: {
								yAxes: [{ ticks: { min: 0, max: 50000 } }],
							} */
						}
					})

				);
				//END PROCESSED TX PART
				//////////////////////////////////////////////////////////////////////////////////////////////
				//CONSENSUS MESSAGE PART
				var consensusmsgtime = [];
				var preparedtpsdata = [];
				var committpsdata = [];
				var roundchangetpsdata = [];

				async function getconsensusData() {
					let txpreparedresponse = await fetch("http://localhost:" + port + "/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getPrepareRate");
					let txprepareddata = await txpreparedresponse.json();
					for (let i = 0; i < txprepareddata.length; i++) {
						consensusmsgtime[i] = txprepareddata[i].map.time;
						preparedtpsdata[i] = Number(txprepareddata[i].map.tps)
					}
					let txcommitresponse = await fetch("http://localhost:" + port + "/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getCommitRate");
					let txcommitdata = await txcommitresponse.json();
					for (let i = 0; i < txcommitdata.length; i++) {
						committpsdata[i] = Number(txcommitdata[i].map.tps)
					}
					let txroundchangeresponse = await fetch("http://localhost:" + port + "/distributed_ledger_simulator_framework_consensus_testbench/rest/simulatorcontroller/getRoundChangeRate");
					let txroundchangedata = await txroundchangeresponse.json();
					for (let i = 0; i < txroundchangedata.length; i++) {
						roundchangetpsdata[i] = Number(txroundchangedata[i].map.tps)
					}
					return txroundchangedata;
				}
				getconsensusData().then(data =>
					new Chart("dltBFT", {
						//type: "bar",
						type: "line",
						data: {
							labels: consensusmsgtime,
							datasets: [{
								label: 'Prepare Message Rate',
								fill: false,
								lineTension: 0,
								borderWidth: 1,
								backgroundColor: "rgb(255, 99, 132)",
								borderColor: "rgb(255, 99, 132)",
								data: preparedtpsdata
							}, {
								label: 'Commit Message Rate',
								fill: false,
								lineTension: 0,
								borderWidth: 1,
								backgroundColor: "rgb(255, 159, 64)",
								borderColor: "rgb(255, 159, 64)",
								data: committpsdata
							}, {
								label: 'Round Change Message Rate',
								fill: false,
								lineTension: 0,
								borderWidth: 1,
								backgroundColor: "rgb(64, 67, 255)",
								borderColor: "rgb(64, 67, 255)",
								data: roundchangetpsdata
							}]
						},
						options: {
							responsive: true,
							interaction: {
								mode: 'index',
								intersect: false,
							},
							stacked: false,
							plugins: {
								title: {
									display: true,
									text: 'Prepare,Commit,RoundChange TPS'
								}
							},
							legend: { display: true }/* ,
							scales: {
								yAxes: [{ ticks: { min: 0, max: 50000 } }],
							} */
						}
					})

				);

			</script>
		</body>

		</html>