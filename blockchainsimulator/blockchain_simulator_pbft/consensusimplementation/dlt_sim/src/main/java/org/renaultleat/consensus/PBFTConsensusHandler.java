package org.renaultleat.consensus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.renaultleat.api.Simulator_result;
import org.renaultleat.chain.Block;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.network.NodeCommunicator;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

/**
 * This Handler is used in case of Multi threaded call from P2PHandler or single
 * threaded we call PBFTMessageHandler
 * 
 */

public class PBFTConsensusHandler extends Thread {

    public BlockingQueue<JSONObject> messageBlockingQueue;

    public Blockchain blockchain;

    public Wallet wallet;

    public PBFTMessagePool pbftMessagePool;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public Validator validator;

    public NodeCommunicator nodeCommunicator;

    public String currentuser;

    private int minapprovals;

    public Synchronizer synchronizer;

    public Simulator_result simulator_result;

    // Be careful as this is thread and need to be concurrent and synchronized
    // Thread Safe and Read from Main Memory
    // public volatile Map<String, Boolean> consensusReached = new HashMap<String,
    // Boolean>();

    public void broadCastPrePrepare(Block block, String message, String blockhash)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("blockhash", blockhash);
        jsonObject.put("type", "PRE-PREPARE");
        Gson gson = new Gson();
        String data = gson.toJson(block);
        jsonObject.put("data", data);

        // System.out.println("Messagestart");
        this.nodeCommunicator.sendMessage(jsonObject.toString());
        // System.out.println("MessageSent");
    }

    public void broadCastPrepare(Block block, Message data, String message, String blockhash)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("blockhash", blockhash);
        jsonObject.put("type", "PREPARE");
        Gson gson = new Gson();
        String datajson = gson.toJson(data);
        jsonObject.put("data", datajson);
        Gson blockGson = new Gson();
        String blockdata = blockGson.toJson(block);
        jsonObject.put("blockdata", blockdata);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    public void broadCastCommit(Block block, Message data, String message, String blockhash)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("blockhash", blockhash);
        jsonObject.put("type", "COMMIT");
        Gson gson = new Gson();
        String datajson = gson.toJson(data);
        jsonObject.put("data", datajson);
        Gson blockGson = new Gson();
        String blockdata = blockGson.toJson(block);
        jsonObject.put("blockdata", blockdata);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    public void broadCastRoundChange(Message data, String message, String user, String blockhash)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("blockhash", blockhash);
        jsonObject.put("type", "ROUNDCHANGE");
        Gson gson = new Gson();
        String datajson = gson.toJson(data);
        jsonObject.put("data", datajson);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    public int getMinApprovals() {
        return this.minapprovals;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (true) {
            {
                if (!this.messageBlockingQueue.isEmpty()) {
                    // System.out.println("Entry");
                    JSONObject jsonObject;
                    try {
                        jsonObject = this.messageBlockingQueue.take();
                        String messageType = jsonObject.getString("type");
                        String blockhash = jsonObject.getString("blockhash");
                        if (!this.synchronizer.consensusReached.containsKey(blockhash)) {
                            this.synchronizer.consensusReached.put(blockhash, false);
                        }
                        if (!this.synchronizer.consensusReached.get(blockhash)) {
                            if (messageType.equals("PRE-PREPARE")) {
                                Gson gson = new Gson();
                                Type blockObject = new TypeToken<Block>() {
                                }.getType();
                                Block inblock = gson.fromJson(jsonObject.getString("data"), blockObject);
                                // System.out.println("Entry1");
                                // System.out.println("Entry1+" + inblock.getBlockNumber());
                                // System.out.println("Entry1Valid+" + this.blockchain.isValidBlock(inblock));

                                // if (!this.blockPool.blockIds.contains(inblock.getBlockNumber())
                                // && this.blockchain.isValidBlock(inblock)) {

                                if (!this.blockPool.blockIds.contains(inblock.getBlockNumber())
                                        && this.blockchain.isValidBlock(inblock)) {
                                    // System.out.println("Entred");
                                    // Heavy: if (!this.blockPool.blockExists(inblock) &&
                                    // this.blockchain.isValidBlock(inblock)) {

                                    this.blockPool.addBlock(inblock);
                                    // All Nodes delete the transaction Pool message confimed
                                    // NOT NECESSARY CAN SLOW DOWN AS IT IS IN MEMEORY
                                    /*
                                     * this.transactionPool
                                     * .clearTransactionPoolFromIncomingBlock(inblock.getBlockNumber(),
                                     * inblock.getBlockData());
                                     */
                                    try {
                                        this.broadCastPrePrepare(inblock, "pre-prepare forwarded from",
                                                inblock.getBlockHash());
                                        // System.out.println("EntryBroadcast");
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    // System.out.println("Entry2");
                                    Message message = this.pbftMessagePool.message("PREPARE", inblock, this.wallet);
                                    try {
                                        this.broadCastPrepare(inblock, message, "PREPARE MESSAGE",
                                                inblock.getBlockHash());
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                }
                            } else if (messageType.equals("PREPARE")) {
                                if (!this.synchronizer.prepareconsensusReached.containsKey(blockhash)) {
                                    this.synchronizer.prepareconsensusReached.put(blockhash, false);
                                }
                                Gson gson = new Gson();
                                Type messageObject = new TypeToken<Message>() {
                                }.getType();

                                Gson preparegson = new Gson();
                                Type blockObject = new TypeToken<Block>() {
                                }.getType();
                                Block inblock = preparegson.fromJson(jsonObject.getString("blockdata"), blockObject);

                                // System.out.println("Entry3");

                                Message inmessage = gson.fromJson(jsonObject.getString("data"), messageObject);
                                // System.out.println("Entry3.1" +
                                // this.pbftMessagePool.existingPrepareMessage(inmessage));
                                // System.out.println("Entry3.2" +
                                // this.pbftMessagePool.isValidMessage(inmessage));
                                // System.out
                                // .println("Entry3.3" +
                                // this.synchronizer.prepareconsensusReached.get(blockhash));
                                // System.out.println(
                                // "Entry3.4" + this.validator.isValidValidator(inmessage.getMessageSender()));
                                this.blockPool.addBlock(inblock);
                                if (!this.pbftMessagePool.existingPrepareMessage(inmessage)
                                        && this.pbftMessagePool.isValidMessage(inmessage)
                                        && this.validator.isValidValidator(inmessage.getMessageSender())
                                        && !this.synchronizer.prepareconsensusReached.get(blockhash)) {
                                    this.pbftMessagePool.addMessage(inmessage);

                                    try {
                                        this.broadCastPrepare(inblock, inmessage, "PREPARE RECEIVED",
                                                inmessage.getBlockHash());
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                    // System.out.println("PRERPARE"
                                    // + this.pbftMessagePool.preparePool.get(inmessage.getBlockHash())
                                    // .size());
                                    if (this.pbftMessagePool.preparePool.get(inmessage.getBlockHash())
                                            .size() >= this.minapprovals) {
                                        // System.out.println("PRERPARE"
                                        // + this.pbftMessagePool.preparePool.get(inmessage.getBlockHash())
                                        // .size());
                                        if (NodeProperty.getnodeBehavior() == 0) {

                                            this.synchronizer.prepareconsensusReached.put(blockhash, true);
                                            Message message = this.pbftMessagePool.message("COMMIT", inmessage,
                                                    this.wallet);

                                            try {
                                                this.broadCastCommit(inblock, message, "COMMIT FORWARD",
                                                        message.getBlockHash());
                                            } catch (Exception e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            } else if (messageType.equals("COMMIT")) {
                                if (!this.synchronizer.commitconsensusReached.containsKey(blockhash)) {
                                    this.synchronizer.commitconsensusReached.put(blockhash, false);
                                }
                                Gson gson = new Gson();
                                Type messageObject = new TypeToken<Message>() {
                                }.getType();
                                // System.out.println("Entry4");
                                Message commitmessage = gson.fromJson(jsonObject.getString("data"), messageObject);

                                Gson preparegson = new Gson();
                                Type blockObject = new TypeToken<Block>() {
                                }.getType();
                                Block inblock = preparegson.fromJson(jsonObject.getString("blockdata"), blockObject);
                                this.blockPool.addBlock(inblock);
                                if (!this.pbftMessagePool.existingCommitMessage(commitmessage)
                                        && this.pbftMessagePool.isValidMessage(commitmessage)
                                        && this.validator.isValidValidator(commitmessage.getMessageSender())
                                        && !this.synchronizer.commitconsensusReached.get(blockhash)) {

                                    this.pbftMessagePool.addMessage(commitmessage);
                                }
                                try {
                                    this.broadCastCommit(inblock, commitmessage, "COMMIT MESSAGE",
                                            commitmessage.getBlockHash());
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                // System.out.println("COMMIT"
                                // + this.pbftMessagePool.commitPool.get(commitmessage.getBlockHash())
                                // .size());
                                // System.out.println("MINAPPROVAL"
                                // + this.minapprovals);
                                if (this.pbftMessagePool.commitPool.get(commitmessage.getBlockHash())
                                        .size() >= this.minapprovals) {

                                    this.synchronizer.commitconsensusReached.put(blockhash, true);
                                    this.blockchain.addUpdatedBlock(inblock, this.blockPool,
                                            this.pbftMessagePool);
                                } else {
                                    // Retry Commit
                                    Message message = this.pbftMessagePool.message("COMMIT", commitmessage,
                                            this.wallet);
                                    try {
                                        this.broadCastCommit(inblock, message, "COMMIT FORWARD",
                                                message.getBlockHash());
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                                // Get the latest block
                                Message roundchangemessage = this.pbftMessagePool.message("ROUNDCHANGE",
                                        this.blockchain.getChain().get(this.blockchain.getChain().size() - 1),
                                        this.wallet);
                                try {
                                    this.broadCastRoundChange(roundchangemessage, "ROUNDCHANGE INITIATE",
                                            this.wallet.getPublicKey(), roundchangemessage.getBlockHash());
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else if (messageType.equals("ROUNDCHANGE")) {
                                if (!this.synchronizer.roundchangeconsensusReached.containsKey(blockhash)) {
                                    this.synchronizer.roundchangeconsensusReached.put(blockhash, false);
                                }
                                // System.out.println("Entry5");
                                Gson gson = new Gson();
                                Type messageObject = new TypeToken<Message>() {
                                }.getType();
                                Message roundchangeMessage = gson.fromJson(jsonObject.getString("data"),
                                        messageObject);
                                if (!this.pbftMessagePool.existingRoundChangeMessage(roundchangeMessage)
                                        && this.pbftMessagePool.isValidMessage(roundchangeMessage)
                                        && this.validator.isValidValidator(roundchangeMessage.getMessageSender())
                                        && !this.synchronizer.roundchangeconsensusReached.get(blockhash)) {
                                    this.pbftMessagePool.addMessage(roundchangeMessage);
                                    try {
                                        this.broadCastRoundChange(roundchangeMessage, "ROUND CHANGE",
                                                this.wallet.getPublicKey(), roundchangeMessage.getBlockHash());
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    // System.out.println("RCHANGE"
                                    // + this.pbftMessagePool.roundChangePool
                                    // .get(blockhash)
                                    // .size());
                                    // Clear the message Pool
                                    if (this.pbftMessagePool.roundChangePool.get(blockhash)
                                            .size() >= this.minapprovals) {

                                        // Parallelizing opportunity to run with round as key and then other data
                                        // No Need as we remove in pre-prepare stage as we might lose incoming
                                        // transaction as well
                                        // Later to change as hashmap if need as (round,transactionpool)
                                        // TOCHECK!!!!
                                        // this.transactionPool.clearTransactionPool();
                                        // Thread Safe
                                        this.synchronizer.roundchangeconsensusReached.put(blockhash, true);
                                        // Result Collation
                                        // Prepare Message
                                        this.simulator_result.prepareCounter
                                                .put(new Timestamp(System.currentTimeMillis()), this.pbftMessagePool
                                                        .getSizeofPrepareMessagePoolForBlockHash(blockhash));
                                        // Commit Message
                                        this.simulator_result.commitCounter
                                                .put(new Timestamp(System.currentTimeMillis()), this.pbftMessagePool
                                                        .getSizeofCommitMessagePoolForBlockHash(blockhash));
                                        // RoundChange Message
                                        this.simulator_result.roundChangeCounter
                                                .put(new Timestamp(System.currentTimeMillis()), this.pbftMessagePool
                                                        .getSizeofRoundChangeMessagePoolForBlockHash(blockhash));
                                        // Heavy But Not Necessary as we already place under buckets of storage
                                        /*
                                         * this.pbftMessagePool
                                         * .clearAllMessagePoolForBlockHash(roundchangeMessage.getBlockHash());
                                         */
                                        this.synchronizer.consensusReached.put(blockhash,
                                                true);

                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    public PBFTConsensusHandler(BlockingQueue<JSONObject> inMessageBlockingQueue, Blockchain blockChain,
            TransactionPool transactionPool,
            BlockPool blockPool, Wallet wallet, Validator validator,
            NodeCommunicator nodeCommunicator, String currentuser, PBFTMessagePool pbftMessagePool,
            Synchronizer synchronizer, Simulator_result simulator_result) {
        this.messageBlockingQueue = inMessageBlockingQueue;
        this.transactionPool = transactionPool;
        this.blockchain = blockChain;
        this.blockPool = blockPool;
        this.wallet = wallet;
        this.validator = validator;
        int total = Integer.valueOf(NodeProperty.totalnodes);
        this.nodeCommunicator = nodeCommunicator;
        this.pbftMessagePool = pbftMessagePool;
        // (2N/3)+1
        float totalfloat = (float) total;
        float minapprovalsfloat = (totalfloat * 2.0f / 3.0f) + 1.0f;
        int minapprovalint = (int) minapprovalsfloat;
        this.minapprovals = minapprovalint;
        this.currentuser = currentuser;
        this.synchronizer = synchronizer;
        this.simulator_result = simulator_result;
    }
}