package org.renaultleat.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.consensus.CliqueConsensusMessageHandler;
import org.renaultleat.consensus.CliqueMessageHandler;
import org.renaultleat.consensus.CliqueMessagePool;
import org.renaultleat.consensus.CliqueProposer;
import org.renaultleat.consensus.CliqueTransactionMessageHandler;
import org.renaultleat.consensus.CliqueTransactionMessageHandlerPenta;
import org.renaultleat.consensus.CliqueTransactionMessageHandlerQuar;
import org.renaultleat.consensus.CliqueTransactionMessageHandlerSec;
import org.renaultleat.consensus.CliqueTransactionMessageHandlerTer;
import org.renaultleat.consensus.Synchronizer;
import org.renaultleat.network.NodeCommunicator;
import org.renaultleat.network.P2PServer;
import org.renaultleat.network.QueueResource;
import org.renaultleat.network.TransactionBroadcaster;
import org.renaultleat.network.TransactionBroadcasterPenta;
import org.renaultleat.network.TransactionBroadcasterQuar;
import org.renaultleat.network.TransactionBroadcasterSec;
import org.renaultleat.network.TransactionBroadcasterTer;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

import java.sql.Timestamp;
import java.util.List;
import java.util.Timer;

import javax.inject.Singleton;
import javax.json.Json;

@Path("/simulatorcontroller")
@Singleton
public class Simulator_controller {

        NonValidator nonValidator;

        Validator validator;

        Blockchain blockchain;

        TransactionPool transactionPool;

        CliqueMessagePool cliqueMessagePool;

        BlockPool blockPool;

        Wallet wallet = new Wallet();

        QueueResource queueResource;

        P2PServer p2pserver;

        Simulator_service simulator_service = new Simulator_service(wallet);

        Synchronizer synchronizer;

        CliqueTransactionMessageHandler cliqueTransactionMessageHandler;

        CliqueTransactionMessageHandlerSec cliqueTransactionMessageHandlerSec;

        CliqueTransactionMessageHandlerTer cliqueTransactionMessageHandlerTer;

        CliqueTransactionMessageHandlerQuar cliqueTransactionMessageHandlerQuar;

        CliqueTransactionMessageHandlerPenta cliqueTransactionMessageHandlerPenta;

        CliqueProposer cliqueProposer;

        CliqueConsensusMessageHandler cliqueConsensusMessageHandler;

        Simulator_result simulator_result;

        Simulator_collator simulator_collator;
        Gson gson = new Gson();

        @Path("/getVersion")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getVersion() {
                String version = "Simulator Test Bench:" + " " + simulator_service.getVersion();
                String json = gson.toJson(version);
                return json;
        }

        @Path("/setPeers/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storePeers(@PathParam("value") String value) {
                simulator_service.storeIPS(value);
                return gson.toJson("Peers Value Set");
        }

        @Path("/getPeers")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getPeers() {
                String peers = simulator_service.getIPS();
                return gson.toJson(String.valueOf(peers));
        }

        @Path("/getIP")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getIP() {
                String ip = simulator_service.getIP();
                return gson.toJson(String.valueOf(ip));
        }

        @Path("/setBlockPeriod/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String setBlockPeriod(@PathParam("value") String value) {
                simulator_service.setBlockPeriod(value);
                return gson.toJson("Block Period Time Set");
        }

        @Path("/getBlockPeriod")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getBlockPeriod() {
                long blockPeriod = simulator_service.getBlockPeriod();
                return gson.toJson(String.valueOf(blockPeriod));
        }

        @Path("/setNodeLatency/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeNodeLatency(@PathParam("value") String value) {
                simulator_service.storeNodeLatency(value);
                return gson.toJson("Node Latency Set");
        }

        @Path("/getNodeLatency")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getNodeLatency() {
                long latency = simulator_service.getNodeLatency();
                return gson.toJson(String.valueOf(latency));
        }

        @Path("/setAsValidator/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeAsValidator(@PathParam("value") String value) {
                simulator_service.storeAsValidator(value);
                return gson.toJson("Set As Validator:" + value);
        }

        @Path("/getAsValidator")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getAsValidator() {
                boolean isValidator = simulator_service.getAsValidator();
                return gson.toJson(String.valueOf(isValidator));
        }

        @Path("/setNodeBehavior/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeNodeBehavior(@PathParam("value") String value) {
                simulator_service.storeNodeBehavior(value);
                return gson.toJson("Node Behavior Set");
        }

        @Path("/getTxPoolStatus/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getTxPoolStatus(@PathParam("value") String value) {
                boolean status = this.transactionPool.transactionRoundStatus.get(Integer.valueOf(value));
                return gson.toJson(status);
        }

        @Path("/getNodeBehavior")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getNodeBehavior() {
                String behavior = simulator_service.getNodeBehavior();
                return gson.toJson(behavior);
        }

        @Path("/setNodeNetwork/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeNodeNetwork(@PathParam("value") String value) {
                simulator_service.storeNodeNetwork(value);
                return gson.toJson("Node Network Set");
        }

        @Path("/getMinApproval")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getMinApproval() {
                int minApprovals = cliqueConsensusMessageHandler.getMinApprovals();
                String minApprovalString = String.valueOf(minApprovals);
                return gson.toJson(minApprovalString);
        }

        @Path("/getNodeNetwork")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getNodeNetwork() {
                String network = simulator_service.getNodeNetwork();
                return gson.toJson(network);
        }

        @Path("/setBlockSize/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeBlockSize(@PathParam("value") String value) {
                simulator_service.storeBlockSize(value);
                return gson.toJson("Block Size Set");
        }

        @Path("/getBlockSize")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getBlockSize() {
                int blocksize = simulator_service.getBlockSize();
                return gson.toJson(String.valueOf(blocksize));
        }

        @Path("/setTotalNodes/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeTotalNodes(@PathParam("value") String value) {
                simulator_service.storeTotalNodes(value);
                return gson.toJson("Total Nodes Set");
        }

        @Path("/getTotalNodes")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getTotalNodes() {
                int totalnodes = simulator_service.getTotalNodes();
                return gson.toJson(String.valueOf(totalnodes));
        }

        @Path("/setTotalValidators/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeTotalValidators(@PathParam("value") String value) {
                simulator_service.storeTotalValidators(value);
                return gson.toJson("Total Validators Set");
        }

        @Path("/getTotalValidators")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getTotalValidators() {
                int totalvalidators = simulator_service.getTotalValidators();
                return gson.toJson(String.valueOf(totalvalidators));
        }

        @Path("/setnodeProperty/{value}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String storeNodeProperty(@PathParam("value") String value) {
                simulator_service.storeNodeProperty(value);

                this.nonValidator = new NonValidator();

                // Initialisation based on node id
                this.validator = new Validator();

                this.blockchain = new Blockchain(validator, nonValidator);

                this.transactionPool = new TransactionPool();

                this.cliqueMessagePool = new CliqueMessagePool();

                this.blockPool = new BlockPool();

                this.queueResource = new QueueResource();

                this.p2pserver = new P2PServer(this.blockchain, this.transactionPool, this.wallet,
                                this.cliqueMessagePool,
                                this.validator, this.nonValidator, this.blockPool,
                                this.queueResource);

                this.simulator_result = new Simulator_result();
                this.simulator_service = new Simulator_service(this.p2pserver, this.wallet, this.simulator_result);

                this.synchronizer = new Synchronizer();

                return gson.toJson("Node Property Set");
        }

        @Path("/getnodeProperty")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getNodeProperty() {
                int instance = simulator_service.getNodeProperty();
                return String.valueOf(instance);
        }

        @Path("/initiateConnection")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String initiateConnection() {
                simulator_service.initiateConnection();
                return gson.toJson("Connection initiated");
        }

        @Path("/listentoPeers")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String listentoPeers() {
                simulator_service.listentoPeers();
                // Depends on Node Communicator Object for communication
                this.cliqueTransactionMessageHandler = new CliqueTransactionMessageHandler(
                                this.queueResource.getTransactionBlockingQueue(), this.blockchain, this.transactionPool,
                                this.blockPool,
                                this.wallet,
                                this.validator, this.nonValidator,
                                this.p2pserver.nodeCommunicator, this.p2pserver.currentuser, this.cliqueMessagePool,
                                this.synchronizer);
                this.cliqueTransactionMessageHandler.start();

                this.cliqueTransactionMessageHandlerSec = new CliqueTransactionMessageHandlerSec(
                                this.queueResource.getTransactionBlockingQueueSec(), this.blockchain,
                                this.transactionPool,
                                this.blockPool,
                                this.wallet,
                                this.validator, this.nonValidator,
                                this.p2pserver.nodeCommunicatorSec, this.p2pserver.currentuser, this.cliqueMessagePool,
                                this.synchronizer);
                this.cliqueTransactionMessageHandlerSec.start();

                this.cliqueTransactionMessageHandlerTer = new CliqueTransactionMessageHandlerTer(
                                this.queueResource.getTransactionBlockingQueueTer(), this.blockchain,
                                this.transactionPool,
                                this.blockPool,
                                this.wallet,
                                this.validator, this.nonValidator,
                                this.p2pserver.nodeCommunicatorTer, this.p2pserver.currentuser, this.cliqueMessagePool,
                                this.synchronizer);
                this.cliqueTransactionMessageHandlerTer.start();

                this.cliqueTransactionMessageHandlerQuar = new CliqueTransactionMessageHandlerQuar(
                                this.queueResource.getTransactionBlockingQueueQuar(), this.blockchain,
                                this.transactionPool,
                                this.blockPool,
                                this.wallet,
                                this.validator, this.nonValidator,
                                this.p2pserver.nodeCommunicatorQuar, this.p2pserver.currentuser, this.cliqueMessagePool,
                                this.synchronizer);
                this.cliqueTransactionMessageHandlerQuar.start();

                this.cliqueTransactionMessageHandlerPenta = new CliqueTransactionMessageHandlerPenta(
                                this.queueResource.getTransactionBlockingQueuePenta(), this.blockchain,
                                this.transactionPool,
                                this.blockPool,
                                this.wallet,
                                this.validator, this.nonValidator,
                                this.p2pserver.nodeCommunicatorPenta, this.p2pserver.currentuser,
                                this.cliqueMessagePool,
                                this.synchronizer);
                this.cliqueTransactionMessageHandlerPenta.start();

                this.cliqueProposer = new CliqueProposer(
                                this.queueResource.getTransactionBlockingQueue(), this.blockchain, this.transactionPool,
                                this.blockPool,
                                this.wallet,
                                this.validator, this.nonValidator,
                                this.p2pserver.nodeCommunicator, this.p2pserver.currentuser, this.cliqueMessagePool,
                                this.synchronizer);

                this.cliqueProposer.scheduleTimer();

                this.cliqueConsensusMessageHandler = new CliqueConsensusMessageHandler(
                                this.queueResource.getMessageBlockingQueue(), this.blockchain, this.transactionPool,
                                this.blockPool,
                                this.wallet,
                                this.validator, this.nonValidator,
                                this.p2pserver.nodeCommunicator, this.p2pserver.currentuser, this.cliqueMessagePool,
                                this.synchronizer,
                                this.simulator_result);
                this.cliqueConsensusMessageHandler.start();

                return gson.toJson("Started Listening to Peers");
        }

        @Path("/sendMessageToPeer/{message}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String sendMessageToPeer(@PathParam("message") String message) {
                simulator_service.sendMessageToPeer(message);
                return gson.toJson("Message Sent/Broadcasted");
        }
        /*
         * @Path("/startsimulation/{notxs}/{nothreads}")
         * 
         * @GET
         * 
         * @Produces(MediaType.APPLICATION_JSON)
         * public String startSimulation(@PathParam("notxs") String
         * notxs, @PathParam("nothreads") String nothreads) {
         * simulator_service.startSimulation(notxs, nothreads);
         * return "Simulation Started";
         * }
         */

        // Better way of simulation with nodes interconnected
        @Path("/startResultCollator")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startResultCollator() {
                // Start the result collator thread
                this.simulator_collator = new Simulator_collator(this.simulator_result, this.transactionPool,
                                this.blockchain);
                this.simulator_collator.start();
                return gson.toJson("Collator Started");
        }

        @Path("/stopResultCollator")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String stopResultCollator() {
                // Stop the result collator thread
                this.simulator_collator.stopThread();
                return gson.toJson("Collator Stopped");
        }

        // Better way of simulation with nodes interconnected
        @Path("/startsimulationsinglesimple/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulationSingleSimple(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                simulator_service.startSimulationSingle(notxs);

                return gson.toJson("Simulation Started");
        }

        @Path("/startsimulation1/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation1(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                return gson.toJson("Simulation Started 1");
        }

        @Path("/startsimulation2/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation2(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                return gson.toJson("Simulation Started 2");
        }

        @Path("/startsimulation3/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation3(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                TransactionBroadcasterSec transactionBroadcasterTer = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterTer.start();

                return gson.toJson("Simulation Started 3");
        }

        @Path("/startsimulation4/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation4(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                TransactionBroadcasterSec transactionBroadcasterTer = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterTer.start();

                TransactionBroadcasterSec transactionBroadcasterfour = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterfour.start();

                return gson.toJson("Simulation Started 4");
        }

        @Path("/startsimulation5/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation5(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                TransactionBroadcasterSec transactionBroadcasterTer = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterTer.start();

                TransactionBroadcasterSec transactionBroadcasterfour = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterfour.start();

                TransactionBroadcasterTer transactionBroadcasterFive = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterFive.start();

                return gson.toJson("Simulation Started 5");
        }

        @Path("/startsimulation6/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation6(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                TransactionBroadcasterSec transactionBroadcasterTer = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterTer.start();

                TransactionBroadcasterSec transactionBroadcasterfour = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterfour.start();

                TransactionBroadcasterTer transactionBroadcasterFive = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterFive.start();

                TransactionBroadcasterTer transactionBroadcasterSix = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSix.start();

                return gson.toJson("Simulation Started 6");
        }

        @Path("/startsimulation7/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation7(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                TransactionBroadcasterSec transactionBroadcasterTer = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterTer.start();

                TransactionBroadcasterSec transactionBroadcasterfour = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterfour.start();

                TransactionBroadcasterTer transactionBroadcasterFive = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterFive.start();

                TransactionBroadcasterTer transactionBroadcasterSix = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSix.start();

                TransactionBroadcasterQuar transactionBroadcasterSeven = new TransactionBroadcasterQuar(
                                this.p2pserver.nodeCommunicatorQuar,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSeven.start();

                return gson.toJson("Simulation Started 7");
        }

        @Path("/startsimulation8/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulation8(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();

                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                TransactionBroadcasterSec transactionBroadcasterTer = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterTer.start();

                TransactionBroadcasterSec transactionBroadcasterfour = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterfour.start();

                TransactionBroadcasterTer transactionBroadcasterFive = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterFive.start();

                TransactionBroadcasterTer transactionBroadcasterSix = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSix.start();

                TransactionBroadcasterQuar transactionBroadcasterSeven = new TransactionBroadcasterQuar(
                                this.p2pserver.nodeCommunicatorQuar,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSeven.start();

                TransactionBroadcasterQuar transactionBroadcasterEight = new TransactionBroadcasterQuar(
                                this.p2pserver.nodeCommunicatorQuar,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterEight.start();

                return gson.toJson("Simulation Started 8");
        }

        // Better way of simulation with nodes interconnected
        @Path("/startsimulationsingle/{notxs}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String startSimulationSingle(@PathParam("notxs") String notxs) {
                // Start Actual Simulation
                // simulator_service.startSimulationSingle(notxs);
                TransactionBroadcaster transactionBroadcaster = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcaster.start();
                TransactionBroadcaster transactionBroadcasterSec = new TransactionBroadcaster(
                                this.p2pserver.nodeCommunicator,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSec.start();

                TransactionBroadcasterSec transactionBroadcasterTer = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterTer.start();

                TransactionBroadcasterSec transactionBroadcasterfour = new TransactionBroadcasterSec(
                                this.p2pserver.nodeCommunicatorSec,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterfour.start();

                TransactionBroadcasterTer transactionBroadcasterFive = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterFive.start();

                TransactionBroadcasterTer transactionBroadcasterSix = new TransactionBroadcasterTer(
                                this.p2pserver.nodeCommunicatorTer,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSix.start();

                TransactionBroadcasterQuar transactionBroadcasterSeven = new TransactionBroadcasterQuar(
                                this.p2pserver.nodeCommunicatorQuar,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterSeven.start();

                TransactionBroadcasterQuar transactionBroadcasterEight = new TransactionBroadcasterQuar(
                                this.p2pserver.nodeCommunicatorQuar,
                                wallet, Integer.valueOf(notxs));
                transactionBroadcasterEight.start();

                /*
                 * TransactionBroadcasterPenta transactionBroadcasterNine = new
                 * TransactionBroadcasterPenta(
                 * this.p2pserver.nodeCommunicatorPenta,
                 * wallet, Integer.valueOf(notxs));
                 * transactionBroadcasterNine.start();
                 * 
                 * TransactionBroadcasterPenta transactionBroadcasterTen = new
                 * TransactionBroadcasterPenta(
                 * this.p2pserver.nodeCommunicatorPenta,
                 * wallet, Integer.valueOf(notxs));
                 * transactionBroadcasterTen.start();
                 */
                return gson.toJson("Simulation Started");
        }

        @Path("/getBlocks")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getBlocks() {
                return gson.toJson(simulator_service.getBlocks());
        }

        @Path("/getBlocksinPool")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getBlocksinPool() {
                return gson.toJson(simulator_service.getBlocksinBlockPool());
        }

        @Path("/getTransactionsValidatedNo")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getTransactionsValidatedNo() {
                return gson.toJson(simulator_service.getTotalTransactionsValidatedNo());
        }

        @Path("/getTransactionsinPoolNo/{roundno}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getTransactionsinPoolNo(@PathParam("roundno") String roundno) {
                return gson.toJson(simulator_service.getTransactionsinPoolNo(roundno));
        }

        @Path("/getBlockNo")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getBlockNo() {
                return gson.toJson(String.valueOf(simulator_service.getNoofBlocks()));
        }

        @Path("/getProcessedTPS")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getProcessedTPS() {
                return gson.toJson(simulator_service.getInputTPSJSON());
        }

        @Path("/getEntryTPS")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getEntryTPS() {
                return gson.toJson(simulator_service.getProcessedTPS());
        }

        @Path("/getStartTime")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getStartTime() {
                return gson.toJson(new Timestamp(System.currentTimeMillis()));
        }

        @Path("/getFinalisedTPS")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getFinalisedTPS() {
                return gson.toJson(simulator_service.getFinalisedTPSJSON());
        }

        @Path("/getPrepareRate")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getPrepareRate() {
                return gson.toJson(simulator_service.getPrepareTPSJSON());
        }

        @Path("/getCommitRate")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getCommitRate() {
                return gson.toJson(simulator_service.getCommitTPSJSON());
        }

        @Path("/getRoundChangeRate")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getRoundChangeRate() {
                return gson.toJson(simulator_service.getRoundChangeTPSJSON());
        }
}
