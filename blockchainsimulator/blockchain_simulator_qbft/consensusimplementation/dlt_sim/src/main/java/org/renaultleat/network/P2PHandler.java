package org.renaultleat.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.consensus.QBFTMessageHandler;
import org.renaultleat.consensus.QBFTMessagePool;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;
import org.json.JSONObject;
import org.json.JSONTokener;

public class P2PHandler extends Thread {

    private BufferedReader bufferedReader;

    public Blockchain blockchain;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public Wallet wallet;

    public QBFTMessagePool pbftMessagePool;

    public Validator validator;

    public NonValidator nonValidator;

    public P2PServer p2pServer;

    public NodeCommunicator nodeCommunicator;

    public QueueResource queueResource;

    public String currentuser;

    public P2PHandler(Socket socket, Blockchain blockChain, TransactionPool transactionPool, Wallet wallet,
            QBFTMessagePool pbftMessagePool, Validator validator, NonValidator nonValidator, P2PServer p2pServer,
            NodeCommunicator nodeCommunicator, String currrentuser, BlockPool blockPool, QueueResource queueResource)
            throws IOException {
        this.blockchain = blockChain;
        this.transactionPool = transactionPool;
        this.wallet = wallet;
        this.pbftMessagePool = pbftMessagePool;
        this.blockPool = blockPool;
        this.validator = validator;
        this.nonValidator = nonValidator;
        this.p2pServer = p2pServer;
        this.nodeCommunicator = nodeCommunicator;
        this.currentuser = currrentuser;
        this.queueResource = queueResource;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    public void run() {
        boolean flag = true;
        boolean multithread = true;
        // QBFTMessageHandler qbftMessageHandler = new
        // QBFTMessageHandler(this.blockchain, this.transactionPool,
        // this.wallet, this.pbftMessagePool, this.validator, this.nonValidator,
        // this.nodeCommunicator,
        // this.blockPool, this.currentuser);
        // int i = 0;
        while (flag) {
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                String tempData;
                String indata;
                String finaldata;
                do {
                    line = bufferedReader.readLine();
                    sb.append(line);
                } while (sb.toString().startsWith("YYY") && !line.endsWith("ZZZ"));
                tempData = sb.toString();
                if (tempData.startsWith("YYY") && tempData.endsWith("ZZZ")) {
                    indata = tempData.replaceFirst("YYY", "");
                    finaldata = indata.replaceFirst("ZZZ", "");
                    // System.out.println("data"+indata);
                    if (finaldata.startsWith("{")) {
                        JSONObject jsonObject = new JSONObject(finaldata);
                        // Receiver Logic
                        /*
                         * if (jsonObject.getString("username").equals("node1")) {
                         * System.out.println("Hey Buddy");
                         * }
                         */
                        // System.out.println(jsonObject);
                        String messagevalidity = jsonObject.getString("username");
                        String messageType = jsonObject.getString("type");
                        if (multithread) {
                            if (!messagevalidity.isEmpty() && !messagevalidity.isBlank()) {
                                TimeUnit.MILLISECONDS.sleep(NodeProperty.latency);
                                if (NodeProperty.getnodeBehavior() == 1) {
                                    TimeUnit.MILLISECONDS.sleep(NodeProperty.latency + 60000);
                                }
                                if (messageType.equals("TRANSACTION")) {
                                    this.queueResource.getTransactionBlockingQueue().put(jsonObject);
                                } else {
                                    this.queueResource.getMessageBlockingQueue().put(jsonObject);
                                }
                            }

                        } // else {
                          // // Alternative for single thread fulfillment
                          // qbftMessageHandler.handleMessage(jsonObject);

                        // }
                    }
                }
            } catch (Exception e) {
                System.out.println("P2PException" + e.toString());
                // flag = false;
                // interrupt();
            }
        }
    }

}
