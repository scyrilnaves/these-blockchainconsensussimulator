package org.renaultleat.consensus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.renaultleat.api.Simulator_result;
import org.renaultleat.chain.Block;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.network.NodeCommunicator;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

/**
 * This Handler is used in case of Multi threaded call from P2PHandler or single
 * threaded we call PBFTMessageHandler
 * 
 */

public class CliqueConsensusHandler extends Thread {

    public BlockingQueue<JSONObject> messageBlockingQueue;

    public Blockchain blockchain;

    public Wallet wallet;

    public CliqueMessagePool ibftMessagePool;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public Validator validator;

    public NonValidator nonValidator;

    public NodeCommunicator nodeCommunicator;

    public String currentuser;

    private int minapprovals;

    public Synchronizer synchronizer;

    public Simulator_result simulator_result;

    // Be careful as this is thread and need to be concurrent and synchronized
    // Thread Safe and Read from Main Memory
    // public volatile Map<String, Boolean> consensusReached = new HashMap<String,
    // Boolean>();

    public void broadCastCommit(Block block, Message message, String messageComment)
            throws IOException {
        if (NodeProperty.isValidator()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.currentuser);
            Gson gson = new Gson();
            String messagejson = gson.toJson(message);
            jsonObject.put("message", messagejson);
            jsonObject.put("messagecomment", messageComment);
            jsonObject.put("blockhash", block.getBlockHash());
            jsonObject.put("type", "COMMIT");
            String data = gson.toJson(block);
            jsonObject.put("data", data);
            this.nodeCommunicator.sendMessage(jsonObject.toString());
        }
    }

    public int getMinApprovals() {
        return this.minapprovals;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (true) {
            // System.out.println("running");
            if (!this.messageBlockingQueue.isEmpty()) {
                // System.out.println("mesg added");
                Gson gson = new Gson();
                JSONObject jsonObject;
                try {
                    jsonObject = this.messageBlockingQueue.take();
                    Type blockObject = new TypeToken<Block>() {
                    }.getType();
                    Block inblock = gson.fromJson(jsonObject.getString("data"), blockObject);
                    String messageType = jsonObject.getString("type");
                    String blockhash = inblock.getBlockHash();
                    if (!this.synchronizer.consensusReached.containsKey(blockhash)) {
                        this.synchronizer.consensusReached.put(blockhash, false);
                    }
                    if (!this.synchronizer.consensusReached.get(blockhash)) {
                        Type messageObject = new TypeToken<Message>() {
                        }.getType();
                        Message commitmessage = gson.fromJson(jsonObject.getString("message"), messageObject);
                        if (!this.ibftMessagePool.existingCommitMessage(commitmessage)
                                && this.ibftMessagePool.isValidMessage(commitmessage)
                                && this.validator.isValidValidator(commitmessage.getMessageSender())) {
                            this.ibftMessagePool.addMessage(commitmessage);
                        }
                        if (this.blockchain.isValidBlock(inblock)) {
                            if (!this.blockchain.blockIds.contains(inblock.getBlockNumber())) {
                                // Heavy: if (!this.blockPool.blockExists(inblock) &&
                                // this.blockchain.isValidBlock(inblock)) {
                                this.blockchain.addBlockFinalized(inblock);
                                // All Nodes delete the transaction Pool message confimed
                                // NOT NECESSARY CAN SLOW DOWN AS IT IS IN MEMEORY
                                /*
                                 * this.transactionPool
                                 * .clearTransactionPoolFromIncomingBlock(inblock.getBlockNumber(),
                                 * inblock.getBlockData());
                                 */
                                try {
                                    this.broadCastCommit(inblock, commitmessage, "commit forwarded from");
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else {
                                this.blockchain.handleFork(inblock.getBlockNumber(), inblock.getWeight(), inblock);
                            }
                            this.blockchain.incrementRoundCounter();
                            this.synchronizer.consensusReached.put(blockhash,
                                    true);
                            // Commit Message
                            this.simulator_result.commitCounter
                                    .put(new Timestamp(System.currentTimeMillis()), this.ibftMessagePool
                                            .getSizeofCommitMessagePoolForBlockHash(blockhash));
                        }
                    }
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }

    }

    public CliqueConsensusHandler(BlockingQueue<JSONObject> inMessageBlockingQueue, Blockchain blockChain,
            TransactionPool transactionPool,
            BlockPool blockPool, Wallet wallet, Validator validator, NonValidator nonValidator,
            NodeCommunicator nodeCommunicator, String currentuser, CliqueMessagePool ibftMessagePool,
            Synchronizer synchronizer, Simulator_result simulator_result) {
        this.messageBlockingQueue = inMessageBlockingQueue;
        this.transactionPool = transactionPool;
        this.blockchain = blockChain;
        this.blockPool = blockPool;
        this.wallet = wallet;
        this.validator = validator;
        this.nonValidator = nonValidator;
        int total = Integer.valueOf(NodeProperty.totalnodes);
        int totalValidators = Integer.valueOf(NodeProperty.validators);
        this.nodeCommunicator = nodeCommunicator;
        this.ibftMessagePool = ibftMessagePool;
        // (2N/3)+1
        float totalfloat = (float) totalValidators;
        float minapprovalsfloat = (totalfloat / 2.0f) + 1.0f;
        int minapprovalint = (int) minapprovalsfloat;
        this.minapprovals = minapprovalint;

        this.currentuser = currentuser;
        this.synchronizer = synchronizer;
        this.simulator_result = simulator_result;
    }
}