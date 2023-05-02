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

public class IBFTConsensusMessageHandler extends Thread {

    public BlockingQueue<JSONObject> messageBlockingQueue;

    public Blockchain blockchain;

    public Wallet wallet;

    public IBFTMessagePool ibftMessagePool;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public Validator validator;

    public NonValidator nonValidator;

    public NodeCommunicator nodeCommunicator;

    public String currentuser;

    private int minapprovals;

    private int minapprovalsalt;

    public Synchronizer synchronizer;

    public Simulator_result simulator_result;

    public IBFTConsensusRoundChangeHandler ibftConsensusRoundChangeHandler;

    // Be careful as this is thread and need to be concurrent and synchronized
    // Thread Safe and Read from Main Memory
    // public volatile Map<String, Boolean> consensusReached = new HashMap<String,
    // Boolean>();

    public void broadCastPrePrepare(Block block, String message, String blockhash)
            throws IOException {
        if (NodeProperty.isValidator()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.currentuser);
            jsonObject.put("message", message);
            jsonObject.put("blockhash", blockhash);
            jsonObject.put("type", "PRE-PREPARE");
            Gson gson = new Gson();
            String data = gson.toJson(block);
            jsonObject.put("data", data);
            this.nodeCommunicator.sendMessage(jsonObject.toString());
        }
    }

    public void broadCastPrepare(Block block, Message data, String message, String blockhash)
            throws IOException {
        if (NodeProperty.isValidator()) {
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
    }

    public void broadCastCommit(Block block, Message data, String message, String blockhash)
            throws IOException {
        if (NodeProperty.isValidator()) {
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
    }

    public void broadCastRoundChange(Message data, String message, String user, String blockhash)
            throws IOException {
        if (NodeProperty.isValidator()) {
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
                                if (this.blockchain.isValidBlock(inblock)) {
                                    if (!this.blockPool.blockIds.contains(inblock.getBlockNumber())) {
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
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                        Message message = this.ibftMessagePool.message("PREPARE", inblock, this.wallet,
                                                this.blockchain.getRoundCounter());
                                        try {
                                            this.broadCastPrepare(inblock, message, "PREPARE MESSAGE",
                                                    inblock.getBlockHash());
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }

                                } else {
                                    // Incase of invalid Block
                                    this.ibftConsensusRoundChangeHandler.task();
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

                                Message inmessage = gson.fromJson(jsonObject.getString("data"), messageObject);
                                if (!this.ibftMessagePool.existingPrepareMessage(inmessage)
                                        && this.ibftMessagePool.isValidMessage(inmessage)
                                        && this.validator.isValidValidator(inmessage.getMessageSender())
                                        && !this.synchronizer.prepareconsensusReached.get(blockhash)) {
                                    this.ibftMessagePool.addMessage(inmessage);

                                    try {
                                        this.broadCastPrepare(inblock, inmessage, "PREPARE RECEIVED",
                                                inmessage.getBlockHash());
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    if (this.ibftMessagePool.preparePool.get(inmessage.getBlockHash())
                                            .size() >= this.minapprovals
                                            || this.ibftMessagePool.preparePool.get(inmessage.getBlockHash())
                                                    .size() >= this.minapprovalsalt) {
                                        if (NodeProperty.getnodeBehavior() == 0) {

                                            this.synchronizer.prepareconsensusReached.put(blockhash, true);
                                            Message message = this.ibftMessagePool.message("COMMIT", inmessage,
                                                    this.wallet, this.blockchain.getRoundCounter());
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
                                Message commitmessage = gson.fromJson(jsonObject.getString("data"), messageObject);
                                Gson preparegson = new Gson();
                                Type blockObject = new TypeToken<Block>() {
                                }.getType();
                                Block inblock = preparegson.fromJson(jsonObject.getString("blockdata"), blockObject);
                                if (!this.ibftMessagePool.existingCommitMessage(commitmessage)
                                        && this.ibftMessagePool.isValidMessage(commitmessage)
                                        && this.validator.isValidValidator(commitmessage.getMessageSender())
                                        && !this.synchronizer.commitconsensusReached.get(blockhash)) {

                                    this.ibftMessagePool.addMessage(commitmessage);
                                }
                                try {
                                    this.broadCastCommit(inblock, commitmessage, "COMMIT MESSAGE",
                                            commitmessage.getBlockHash());
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if (this.ibftMessagePool.commitPool.get(commitmessage.getBlockHash())
                                        .size() >= this.minapprovals
                                        || this.ibftMessagePool.commitPool.get(commitmessage.getBlockHash())
                                                .size() >= this.minapprovalsalt) {
                                    // Reschedule
                                    // this.ibftConsensusRoundChangeHandler.rescheduleTimer();
                                    this.synchronizer.commitconsensusReached.put(blockhash, true);
                                    this.blockchain.addUpdatedBlock(inblock, this.blockPool,
                                            this.ibftMessagePool);
                                    this.blockchain.incrementRoundCounter();
                                    this.synchronizer.consensusincourse = false;
                                    this.synchronizer.consensusReached.put(commitmessage.getBlockHash(),
                                            true);
                                }
                                // Result Collation
                                // Prepare Message
                                this.simulator_result.prepareCounter
                                        .put(new Timestamp(System.currentTimeMillis()), this.ibftMessagePool
                                                .getSizeofPrepareMessagePoolForBlockHash(blockhash));
                                // Commit Message
                                this.simulator_result.commitCounter
                                        .put(new Timestamp(System.currentTimeMillis()), this.ibftMessagePool
                                                .getSizeofCommitMessagePoolForBlockHash(blockhash));
                                // RoundChange Message
                                this.simulator_result.roundChangeCounter
                                        .put(new Timestamp(System.currentTimeMillis()), this.ibftMessagePool
                                                .getSizeofRoundChangeMessagePoolForBlockHash(blockhash));

                            } else if (messageType.equals("ROUNDCHANGE")) {
                                if (!this.synchronizer.roundchangeconsensusReached.containsKey(blockhash)) {
                                    this.synchronizer.roundchangeconsensusReached.put(blockhash, false);
                                }
                                Gson gson = new Gson();
                                Type messageObject = new TypeToken<Message>() {
                                }.getType();
                                Message roundchangeMessage = gson.fromJson(jsonObject.getString("data"),
                                        messageObject);
                                if (!this.ibftMessagePool.existingRoundChangeMessage(roundchangeMessage)
                                        && this.ibftMessagePool.isValidMessage(roundchangeMessage)
                                        && this.validator.isValidValidator(roundchangeMessage.getMessageSender())
                                        && !this.synchronizer.roundchangeconsensusReached.get(blockhash)) {
                                    this.ibftMessagePool.addMessage(roundchangeMessage);

                                    try {
                                        this.broadCastRoundChange(roundchangeMessage, "ROUND CHANGE",
                                                this.wallet.getPublicKey(), roundchangeMessage.getBlockHash());
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    // Clear the message Pool
                                    if (this.ibftMessagePool.roundChangePool.get(roundchangeMessage.getBlockHash())
                                            .size() >= this.minapprovals
                                            || this.ibftMessagePool.roundChangePool
                                                    .get(roundchangeMessage.getBlockHash())
                                                    .size() >= this.minapprovalsalt) {
                                        // Parallelizing opportunity to run with round as key and then other data
                                        // No Need as we remove in pre-prepare stage as we might lose incoming
                                        // transaction as well
                                        // Later to change as hashmap if need as (round,transactionpool)
                                        // TOCHECK!!!!
                                        // this.transactionPool.clearTransactionPool();
                                        // Thread Safe
                                        this.synchronizer.consensusReached.put(blockhash,
                                                true);
                                        this.synchronizer.roundchangeconsensusReached.put(blockhash, true);
                                        // Increment Round Change
                                        this.blockchain.incrementRoundCounter();

                                        Block expiredBlock = this.blockPool.getBlockforHash(blockhash);
                                        this.blockPool.removeBlock(expiredBlock.getBlockHash());

                                        // Re-Propose the block if you are a validator
                                        if (this.blockchain.getRoundChangeProposer()
                                                .equals(this.wallet.getPublicKey()) && expiredBlock != null) {
                                            this.synchronizer.consensusincourse = true;
                                            // this.synchronizer.thresholdReached = false;
                                            CopyOnWriteArrayList<Transaction> tempTransactionList = expiredBlock
                                                    .getTransactions();
                                            Block newBlock = this.blockchain.createBlock(tempTransactionList,
                                                    this.wallet);
                                            // this.blockPool.addBlock(block);
                                            // Clear transaction Pool as we formed a block with those transactions as
                                            // this
                                            // for Proposer Node

                                            try {
                                                this.broadCastPrePrepare(newBlock, "blockproposed",
                                                        newBlock.getBlockHash());

                                            } catch (Exception e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                            // Heavy Operation
                                            // this.transactionPool
                                            // .clearTransactionPoolFromIncomingBlock(tempTransactionList);
                                            // Setting back to default
                                            // this.synchronizer.consensusincourse = false;
                                            // this.synchronizer.thresholdReached = true;
                                        }

                                        // Heavy But Not Necessary as we already place under buckets of storage
                                        /*
                                         * this.pbftMessagePool
                                         * .clearAllMessagePoolForBlockHash(roundchangeMessage.getBlockHash());
                                         */

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

    public IBFTConsensusMessageHandler(BlockingQueue<JSONObject> inMessageBlockingQueue, Blockchain blockChain,
            TransactionPool transactionPool,
            BlockPool blockPool, Wallet wallet, Validator validator, NonValidator nonValidator,
            NodeCommunicator nodeCommunicator, String currentuser, IBFTMessagePool ibftMessagePool,
            Synchronizer synchronizer, Simulator_result simulator_result,
            IBFTConsensusRoundChangeHandler ibftConsensusRoundChangeHandler) {
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
        float minapprovalsfloat = (totalfloat * 2.0f / 3.0f) + 1.0f;
        int minapprovalint = (int) minapprovalsfloat;
        this.minapprovals = minapprovalint;
        float minapprovalsfloatalt = (totalfloat * 1.0f / 2.0f);
        int minapprovalintalt = (int) minapprovalsfloatalt;
        this.minapprovalsalt = minapprovalintalt;

        this.currentuser = currentuser;
        this.synchronizer = synchronizer;
        this.simulator_result = simulator_result;
        this.ibftConsensusRoundChangeHandler = ibftConsensusRoundChangeHandler;
    }
}