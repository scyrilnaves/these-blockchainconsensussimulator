package org.renaultleat.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.consensus.PBFTMessageHandler;
import org.renaultleat.consensus.PBFTMessagePool;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;
import org.json.JSONObject;
import org.json.JSONTokener;

public class P2PHandlerPenta extends Thread {

    private BufferedReader bufferedReader;

    public Blockchain blockchain;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public Wallet wallet;

    public PBFTMessagePool pbftMessagePool;

    public Validator validator;

    public P2PServer p2pServer;

    public NodeCommunicator nodeCommunicator;

    public NodeCommunicatorPenta nodeCommunicatorPenta;

    public QueueResource queueResource;

    public String currentuser;

    public P2PHandlerPenta(Socket socket, Blockchain blockChain, TransactionPool transactionPool, Wallet wallet,
            PBFTMessagePool pbftMessagePool, Validator validator, P2PServer p2pServer,
            NodeCommunicator nodeCommunicator, String currrentuser, BlockPool blockPool, QueueResource queueResource,
            NodeCommunicatorPenta nodeCommunicatorPenta)
            throws IOException {
        this.blockchain = blockChain;
        this.transactionPool = transactionPool;
        this.wallet = wallet;
        this.pbftMessagePool = pbftMessagePool;
        this.blockPool = blockPool;
        this.validator = validator;
        this.p2pServer = p2pServer;
        this.nodeCommunicator = nodeCommunicator;
        this.currentuser = currrentuser;
        this.queueResource = queueResource;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.nodeCommunicatorPenta = nodeCommunicatorPenta;

    }

    public void run() {
        boolean flag = true;
        boolean multithread = true;
        // PBFTMessageHandler pbftMessageHandler = new
        // PBFTMessageHandler(this.blockchain, this.transactionPool,
        // this.wallet, this.pbftMessagePool, this.validator, this.nodeCommunicator,
        // this.blockPool, this.currentuser);
        // int i = 0;
        while (flag) {
            try {

                // Model1
                // https://www.baeldung.com/java-bufferedreader-to-jsonobject
                // String line;
                // while ((line = bufferedReader.readLine()) != null) {
                // sb.append(line);
                // }
                // JSONObject json = new JSONObject(sb.toString());

                // Model2
                // https://www.tabnine.com/web/assistant/code/rs/5c76ac53e70f8700017dc8b2#L57
                // private static String readAll(Reader rd) throws IOException {
                // StringBuilder sb = new StringBuilder();
                // int cp;
                // while ((cp = rd.read()) != -1) {
                // sb.append((char) cp);
                // }
                // return sb.toString();
                // }

                // Model3
                // JSONTokener tokener = new JSONTokener(bufferedReader);
                // JSONObject jsonObject = new JSONObject(tokener);
                //
                // Receiver Logic
                /*
                 * if (jsonObject.getString("username").equals("node1")) {
                 * System.out.println("Hey Buddy");
                 * }
                 */
                // System.out.println(jsonObject);
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
                    // indata = tempData.replaceAll("YYY", "").replaceAll("ZZZ", "");
                    indata = tempData.replaceFirst("YYY", "");
                    finaldata = indata.replaceFirst("ZZZ", "");
                    // System.out.println("data"+indata);
                    if (finaldata.startsWith("{")) {
                        JSONObject jsonObject = new JSONObject(finaldata);

                        String messagevalidity = jsonObject.getString("username");
                        String messageType = jsonObject.getString("type");
                        if (multithread) {
                            if (!messagevalidity.isEmpty() && !messagevalidity.isBlank()) {
                                TimeUnit.MILLISECONDS.sleep(NodeProperty.latency);
                                if (NodeProperty.getnodeBehavior() == 1) {
                                    TimeUnit.MILLISECONDS.sleep(NodeProperty.latency + 60000);
                                }
                                if (messageType.equals("TRANSACTION")) {
                                    this.queueResource.getTransactionBlockingQueuePenta().put(jsonObject);
                                } else {
                                    this.queueResource.getMessageBlockingQueue().put(jsonObject);
                                }
                            }

                        } else {
                            // // Alternative for single thread fulfillment
                            // pbftMessageHandler.handleMessage(jsonObject);

                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
