package org.renaultleat.dev;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.renaultleat.api.Simulator_collator;
import org.renaultleat.api.Simulator_result;
import org.renaultleat.chain.Block;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.consensus.IBFTConsensusMessageHandler;
import org.renaultleat.consensus.IBFTConsensusRoundChangeHandler;
import org.renaultleat.consensus.IBFTMessagePool;
import org.renaultleat.consensus.IBFTTransactionMessageHandler;
import org.renaultleat.consensus.IBFTTransactionMessageHandlerPenta;
import org.renaultleat.consensus.IBFTTransactionMessageHandlerQuar;
import org.renaultleat.consensus.IBFTTransactionMessageHandlerSec;
import org.renaultleat.consensus.IBFTTransactionMessageHandlerSix;
import org.renaultleat.consensus.IBFTTransactionMessageHandlerTer;
import org.renaultleat.consensus.Synchronizer;
import org.renaultleat.network.P2PServer;
import org.renaultleat.network.QueueResource;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeDevProperty;
import org.renaultleat.properties.NodeProperty;

public class SimulatorServiceDev {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        // Create All Object

        Wallet wallet = new Wallet();

        String nodeid = String.valueOf(args[0]);
        // Initialise Wallet Object
        Wallet.setnodeproperty(nodeid);
        wallet.initalise(Integer.parseInt(nodeid));
        //////////////////////////////////////////////////////////////////

        Validator validator = new Validator();

        NonValidator nonValidator = new NonValidator();

        Blockchain blockchain = new Blockchain(validator, nonValidator);

        TransactionPool transactionPool = new TransactionPool();

        IBFTMessagePool ibftMessagePool = new IBFTMessagePool();

        BlockPool blockPool = new BlockPool();

        QueueResource queueResource = new QueueResource();

        P2PServer p2pserver = new P2PServer(blockchain, transactionPool, wallet, ibftMessagePool, validator,
                nonValidator, blockPool,
                queueResource);

        Synchronizer synchronizer = new Synchronizer();

        Simulator_result simulator_result = new Simulator_result();

        Simulator_collator simulator_collator = new Simulator_collator(simulator_result, transactionPool, blockchain);

        IBFTTransactionMessageHandler ibftTransactionMessageHandler = new IBFTTransactionMessageHandler(
                queueResource.getTransactionBlockingQueue(), blockchain, transactionPool, blockPool, wallet, validator,
                nonValidator,
                p2pserver.nodeCommunicator, p2pserver.currentuser, ibftMessagePool, synchronizer);

        ibftTransactionMessageHandler.start();

        IBFTTransactionMessageHandlerSec ibftTransactionMessageHandlerSec = new IBFTTransactionMessageHandlerSec(
                queueResource.getTransactionBlockingQueueSec(), blockchain, transactionPool, blockPool, wallet,
                validator,
                nonValidator,
                p2pserver.nodeCommunicatorSec, p2pserver.currentuser, ibftMessagePool, synchronizer);

        ibftTransactionMessageHandlerSec.start();

        IBFTTransactionMessageHandlerTer ibftTransactionMessageHandlerTer = new IBFTTransactionMessageHandlerTer(
                queueResource.getTransactionBlockingQueueTer(), blockchain, transactionPool, blockPool, wallet,
                validator,
                nonValidator,
                p2pserver.nodeCommunicatorTer, p2pserver.currentuser, ibftMessagePool, synchronizer);
        ibftTransactionMessageHandlerTer.start();

        IBFTTransactionMessageHandlerQuar ibftTransactionMessageHandlerQuar = new IBFTTransactionMessageHandlerQuar(
                queueResource.getTransactionBlockingQueueQuar(), blockchain, transactionPool, blockPool, wallet,
                validator,
                nonValidator,
                p2pserver.nodeCommunicatorQuar, p2pserver.currentuser, ibftMessagePool, synchronizer);

        ibftTransactionMessageHandlerQuar.start();

        IBFTTransactionMessageHandlerPenta ibftTransactionMessageHandlerPenta = new IBFTTransactionMessageHandlerPenta(
                queueResource.getTransactionBlockingQueuePenta(), blockchain, transactionPool, blockPool, wallet,
                validator,
                nonValidator,
                p2pserver.nodeCommunicatorPenta, p2pserver.currentuser, ibftMessagePool, synchronizer);

        ibftTransactionMessageHandlerPenta.start();

        IBFTTransactionMessageHandlerSix ibftTransactionMessageHandlerSix = new IBFTTransactionMessageHandlerSix(
                queueResource.getTransactionBlockingQueueSix(), blockchain, transactionPool, blockPool, wallet,
                validator,
                nonValidator,
                p2pserver.nodeCommunicatorSix, p2pserver.currentuser, ibftMessagePool, synchronizer);

        ibftTransactionMessageHandlerSix.start();

        IBFTConsensusRoundChangeHandler ibftConsensusRoundChangeHandler = new IBFTConsensusRoundChangeHandler(
                p2pserver.nodeCommunicator, p2pserver.currentuser, synchronizer, blockchain, ibftMessagePool, wallet,
                blockPool);

        IBFTConsensusMessageHandler ibftConsensusMessageHandler = new IBFTConsensusMessageHandler(
                queueResource.getMessageBlockingQueue(), blockchain, transactionPool, blockPool, wallet, validator,
                nonValidator,
                p2pserver.nodeCommunicator, p2pserver.currentuser, ibftMessagePool, synchronizer, simulator_result,
                ibftConsensusRoundChangeHandler);
        ibftConsensusRoundChangeHandler.scheduleTimer();
        ibftConsensusMessageHandler.run();

        ////////////////////////////////////////
        System.out.println("Connection Initialised" + nodeid);
        // Initiate Connection
        p2pserver.connect();
        ////////////////////////////////////////////////////////////////////
        TimeUnit.SECONDS.sleep(40);
        // Connect to Peers
        System.out.println("Connecting to Peers" + nodeid);
        p2pserver.connectToPeers();
        ////////////////////////////////////////////////////////////////////
        // Start Simulation
        // Only Node 0 sends txs
        System.out.println("Nodeid" + nodeid);
        if (nodeid.equals("0")) {
            System.out.println("Starting Simulation" + nodeid);
            p2pserver.startSimulationBroadcastSingle(500);
        }
        TimeUnit.SECONDS.sleep(60);
        System.out.println("End Simulation" + nodeid);
        // Result Collation
        System.out.println("Results" + nodeid);
        // Transaction in Pool
        // Total Transactions Validated
        List<Block> chain = p2pserver.blockPool.getBlocks();
        Integer total = 0;
        for (Block block : chain) {
            total += block.getBlockData().size();
        }
        System.out.println("Total No of Transation validated" + String.valueOf(total));
        // Chain Size
        int noofblocks = p2pserver.blockchain.getChain().size();
        System.out.println("Chain Size" + noofblocks);

    }

}
