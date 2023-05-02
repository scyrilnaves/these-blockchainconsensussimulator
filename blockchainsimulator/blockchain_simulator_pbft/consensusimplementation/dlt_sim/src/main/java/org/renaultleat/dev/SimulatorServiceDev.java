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
import org.renaultleat.consensus.PBFTConsensusMessageHandler;
import org.renaultleat.consensus.PBFTMessagePool;
import org.renaultleat.consensus.PBFTTransactionMessageHandler;
import org.renaultleat.consensus.Synchronizer;
import org.renaultleat.network.P2PServer;
import org.renaultleat.network.QueueResource;
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

        Blockchain blockchain = new Blockchain(validator);

        TransactionPool transactionPool = new TransactionPool();

        PBFTMessagePool pbftMessagePool = new PBFTMessagePool();

        BlockPool blockPool = new BlockPool();

        QueueResource queueResource = new QueueResource();

        P2PServer p2pserver = new P2PServer(blockchain, transactionPool, wallet, pbftMessagePool, validator, blockPool,
                queueResource);

        Synchronizer synchronizer = new Synchronizer();

        Simulator_result simulator_result = new Simulator_result();

        Simulator_collator simulator_collator = new Simulator_collator(simulator_result, transactionPool, blockchain);

        PBFTTransactionMessageHandler pbftTransactionMessageHandler = new PBFTTransactionMessageHandler(
                queueResource.getTransactionBlockingQueue(), blockchain, transactionPool, blockPool, wallet, validator,
                p2pserver.nodeCommunicator, p2pserver.currentuser, pbftMessagePool, synchronizer);

        pbftTransactionMessageHandler.run();

        PBFTConsensusMessageHandler pbftConsensusMessageHandler = new PBFTConsensusMessageHandler(
                queueResource.getMessageBlockingQueue(), blockchain, transactionPool, blockPool, wallet, validator,
                p2pserver.nodeCommunicator, p2pserver.currentuser, pbftMessagePool, synchronizer, simulator_result);

        pbftConsensusMessageHandler.run();

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
