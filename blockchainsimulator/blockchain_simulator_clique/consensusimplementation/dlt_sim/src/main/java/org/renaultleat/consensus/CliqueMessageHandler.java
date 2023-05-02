package org.renaultleat.consensus;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.renaultleat.chain.Block;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.network.NodeCommunicator;
import org.renaultleat.network.P2PServer;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;
import org.json.JSONObject;

/**
 * 
 * This Handler is used in case of single threaded call from P2PHandler or
 * 
 * multithreaded we call PBFTConsensusMessageHandler
 * 
 * 
 * 
 */

public class CliqueMessageHandler {

    public Blockchain blockchain;

    public TransactionPool transactionPool;

    public Wallet wallet;

    public CliqueMessagePool pbftMessagePool;

    public BlockPool blockPool;

    public Validator validator;

    public NonValidator nonValidator;

    public NodeCommunicator nodeCommunicator;

    public String currentuser;

    private int minapprovals;

    public CliqueMessageHandler(Blockchain blockChain, TransactionPool transactionPool, Wallet wallet,
            CliqueMessagePool pbftMessagePool, Validator validator, NonValidator nonValidator,
            NodeCommunicator nodeCommunicator, BlockPool blockpool, String currentuser) {
        this.blockchain = blockChain;
        this.transactionPool = transactionPool;
        this.wallet = wallet;
        this.pbftMessagePool = pbftMessagePool;
        this.blockPool = blockpool;
        this.validator = validator;
        this.nonValidator = nonValidator;
        this.nodeCommunicator = nodeCommunicator;

        int total = Integer.valueOf(NodeProperty.totalnodes);
        int totalValidators = Integer.valueOf(NodeProperty.validators);
        // (2N/3)+1

        float totalfloat = (float) totalValidators;
        float minapprovalsfloat = (totalfloat / 2.0f) + 1.0f;
        int minapprovalint = (int) minapprovalsfloat;
        this.minapprovals = minapprovalint;

        this.currentuser = currentuser;
    }

    public void broadCastPrePrepare(Block block, String message)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("type", "PRE-PREPARE");
        Gson gson = new Gson();
        String data = gson.toJson(block);
        jsonObject.put("data", data);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    public void broadCastPrepare(Message data, String message)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("type", "PREPARE");
        Gson gson = new Gson();
        String datajson = gson.toJson(data);
        jsonObject.put("data", datajson);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    public void broadCastCommit(Message data, String message)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("type", "COMMIT");
        Gson gson = new Gson();
        String datajson = gson.toJson(data);
        jsonObject.put("data", datajson);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    public void broadCastRoundChange(Message data, String message, String user)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("type", "ROUNDCHANGE");
        Gson gson = new Gson();
        String datajson = gson.toJson(data);
        jsonObject.put("data", datajson);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    public void handleMessage(JSONObject jsonObject) {
        String messageType = jsonObject.getString("type");
        if (messageType.equals("TRANSACTION")) {
            Gson gson = new Gson();
            Type blockObject = new TypeToken<Transaction>() {
            }.getType();
            Transaction inputTransaction = gson.fromJson(jsonObject.getString("data"), blockObject);
            if (!this.transactionPool.transactionExists(inputTransaction, this.blockchain.getRoundCounter())
                    && this.transactionPool.verifyTransaction(inputTransaction)
                    && this.validator.isValidValidator(inputTransaction.getFrom())) {
                boolean thresholdReached = this.transactionPool.addTransaction(
                        inputTransaction);
                try {
                    this.nodeCommunicator.broadCastTransaction(this.currentuser,
                            "transaction forwarded from", inputTransaction);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (thresholdReached) {
                    int round = 0;
                    if (this.blockchain.getProposer(round).equals(this.wallet.getPublicKey())) {
                        Block block = this.blockchain.createBlock(
                                this.transactionPool.getTransactions(this.blockchain.getRoundCounter(), wallet),
                                this.wallet, 2);
                        // Clear transaction Pool as we formed a block with those transactions as this
                        // for Proposer Node
                        // this.transactionPool
                        // .clearTransactionPoolFromIncomingBlock(this.transactionPool.getTransactions());
                        try {
                            this.broadCastPrePrepare(block, "blockproposed");

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } /*
                   * else {
                   * System.out.println("transaction added");
                   * }
                   */
            }
        } else if (messageType.equals("PRE-PREPARE")) {
            // Need to change here and signature
            Gson gson = new Gson();
            Type blockObject = new TypeToken<Block>() {
            }.getType();
            Block inblock = gson.fromJson(jsonObject.getString("data"), blockObject);
            // data, blockproposer, signature, blocknum)
            // System.out.println("exists" + this.blockPool.blockExists(inblock));
            // System.out.println("valid" + this.blockchain.isValidBlock(inblock));
            if (!this.blockPool.blockExists(inblock) && this.blockchain.isValidBlock(inblock)) {
                this.blockPool.addBlock(inblock);
                // All Nodes delete the transaction Pool message confimed
                // this.transactionPool.clearTransactionPoolFromIncomingBlock(inblock.getBlockData());
                try {
                    this.broadCastPrePrepare(inblock, "pre-prepare forwarded from");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Message message = this.pbftMessagePool.message("PREPARE", inblock, this.wallet,
                        this.blockchain.getRoundCounter());
                try {
                    this.broadCastPrepare(message, "PREPARE MESSAGE");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } else if (messageType.equals("PREPARE")) {
            Gson gson = new Gson();
            Type messageObject = new TypeToken<Message>() {
            }.getType();
            Message inmessage = gson.fromJson(jsonObject.getString("data"), messageObject);
            if (!this.pbftMessagePool.existingMessage(inmessage) && this.pbftMessagePool.isValidMessage(inmessage)
                    && this.validator.isValidValidator(inmessage.getMessageSender())) {
                this.pbftMessagePool.addMessage(inmessage);

                try {
                    this.broadCastPrepare(inmessage, "PREPARE RECEIVED");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (this.pbftMessagePool.preparePool.get(inmessage.getBlockHash()).size() >= this.minapprovals) {
                    Message message = this.pbftMessagePool.message("COMMIT", inmessage, this.wallet,
                            this.blockchain.getRoundCounter());
                    try {
                        this.broadCastCommit(message, "COMMIT FORWARD");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else if (messageType.equals("COMMIT")) {
            Gson gson = new Gson();
            Type messageObject = new TypeToken<Message>() {
            }.getType();
            Message commitmessage = gson.fromJson(jsonObject.getString("data"), messageObject);
            if (!this.pbftMessagePool.existingMessage(commitmessage)
                    && this.pbftMessagePool.isValidMessage(commitmessage)
                    && this.validator.isValidValidator(commitmessage.getMessageSender())) {
                this.pbftMessagePool.addMessage(commitmessage);
            }
            try {
                this.broadCastCommit(commitmessage, "COMMIT MESSAGE");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (this.pbftMessagePool.commitPool.get(commitmessage.getBlockHash()).size() >= this.minapprovals) {
                // this.blockchain.addUpdatedBlock(commitmessage.getBlockHash(), this.blockPool,
                // this.pbftMessagePool);
            }
            // Get the latest block
            Message roundchangemessage = this.pbftMessagePool.message("ROUNDCHANGE",
                    this.blockchain.getChain().get(this.blockchain.getChain().size() - 1), this.wallet,
                    this.blockchain.getRoundCounter());
            try {
                this.broadCastRoundChange(roundchangemessage, "ROUNDCHANGE INITIATE",
                        this.wallet.getPublicKey());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (messageType.equals("ROUNDCHANGE")) {
            Gson gson = new Gson();
            Type messageObject = new TypeToken<Message>() {
            }.getType();
            Message roundchangeMessage = gson.fromJson(jsonObject.getString("data"), messageObject);
            if (!this.pbftMessagePool.existingMessage(roundchangeMessage)
                    && this.pbftMessagePool.isValidMessage(roundchangeMessage)
                    && this.validator.isValidValidator(this.wallet.getPublicKey())) {
                this.pbftMessagePool.addMessage(roundchangeMessage);
                try {
                    this.broadCastRoundChange(roundchangeMessage, "ROUND CHANGE",
                            this.wallet.getPublicKey());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // Clear the message Pool
                if (this.pbftMessagePool.getRoundChangePool().get(roundchangeMessage.getBlockHash())
                        .size() >= this.minapprovals) {
                    // Parallelizing opportunity to run with round as key and then other data
                    // No Need as we remove in pre-prepare stage as we might lose incoming
                    // transaction as well
                    // Later to change as hashmap if need as (round,transactionpool)
                    // TOCHECK!!!!
                    // this.transactionPool.clearTransactionPool();
                    // this.pbftMessagePool.clearAllMessagePool();
                    // this.blockPool.clearBlockPool();

                }
            }
        }
    }

}
