package org.renaultleat.consensus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.gson.Gson;

import org.json.JSONObject;
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

public class CliqueTransactionMessageHandler extends Thread {
    public BlockingQueue<JSONObject> transactionBlockingQueue;

    public Blockchain blockchain;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public CliqueMessagePool cliqueMessagePool;

    public Wallet wallet;

    public Validator validator;

    public NonValidator nonValidator;

    public NodeCommunicator nodeCommunicator;

    public Synchronizer synchronizer;

    private int minapprovals;

    public String currentuser;

    // public volatile AtomicInteger lastBlockIndexRead = new AtomicInteger(1);
    // Be careful as this is thread and need to be concurrent and synchronized
    // Thread Safe and Read from Main Memory
    // public volatile boolean thresholdReached = false;

    // public volatile boolean consensusincourse = false;

    public void broadCastCommit(Block block, Message message, String messagecomment)
            throws IOException {
        if (NodeProperty.isValidator()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.currentuser);
            Gson gson = new Gson();
            String messagejson = gson.toJson(message);
            jsonObject.put("message", messagejson);
            jsonObject.put("messagecomment", messagecomment);
            jsonObject.put("blockhash", block.getBlockHash());
            jsonObject.put("type", "COMMIT");
            String data = gson.toJson(block);
            jsonObject.put("data", data);
            this.nodeCommunicator.sendMessage(jsonObject.toString());
        }
    }

    @Override
    public void run() {
        while (true) {
            // System.out.println("tx running");
            if (!this.transactionBlockingQueue.isEmpty()) {
                JSONObject jsonObject;
                try {
                    jsonObject = this.transactionBlockingQueue.take();
                    Gson gson = new Gson();
                    Type blockObject = new TypeToken<Transaction>() {
                    }.getType();
                    Transaction inputTransaction = gson.fromJson(jsonObject.getString("data"), blockObject);
                    // if (!this.transactionPool.transactionExists(inputTransaction)
                    // &&
                    // !this.transactionPool.confirmedTransactionIdexists(inputTransaction.getId())
                    // && this.transactionPool.verifyTransaction(inputTransaction)
                    // && this.validator.isValidValidator(inputTransaction.getFrom()))
                    // REMOVED ADDITIOANL TRANSZCION EXIST
                    // System.out.println("tx added");
                    if (!this.transactionPool.confirmedTransactionIds.contains(inputTransaction.getId())
                            && this.transactionPool.verifyTransaction(inputTransaction)) {
                        this.synchronizer.thresholdReached = this.transactionPool
                                .addTransaction(inputTransaction);
                        this.transactionPool.confirmedTransactionIds.add(inputTransaction.getId());
                        try {
                            this.nodeCommunicator.broadCastTransaction(this.currentuser,
                                    "transaction forwarded from", inputTransaction);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                try {
                    int currentround = this.blockchain.getRoundCounter();
                    // System.out.println("Propose calledd" + currentround);
                    // System.out.println("Indexread" + this.blockchain.lastBlockIndexRead.get());
                    // System.out.println("Sizeread" + this.blockchain.getChain().size());
                    /*
                     * System.out.println("round" + this.blockchain.getCurrentRound());
                     * System.out.println("containkey" +
                     * this.transactionPool.transactionRoundStatus.containsKey(this.blockchain.
                     * getCurrentRound()));
                     * System.out.println(
                     * "roundstatus" +
                     * this.transactionPool.transactionRoundStatus.get(this.blockchain.
                     * getCurrentRound()));
                     */
                    // INROUND BLOCK CREATION WITH WEIGHT 2
                    if (this.transactionPool.transactionRoundStatus.containsKey(currentround)
                            && this.transactionPool.transactionRoundStatus.get(currentround)) {
                        // System.out.println("Propose entered" + currentround);
                        // Check if a validator
                        if (this.blockchain.getProposer(currentround).equals(this.wallet.getPublicKey())
                                || this.blockchain.getAltProposer(currentround)
                                        .equals(this.wallet.getPublicKey())) {
                            // this.synchronizer.consensusincourse = true;
                            // this.synchronizer.thresholdReached = false;
                            CopyOnWriteArrayList<Transaction> tempTransactionList = new CopyOnWriteArrayList<Transaction>(
                                    this.transactionPool.getTransactions(currentround, wallet));

                            CopyOnWriteArrayList<Transaction> tempTransactionListNull = new CopyOnWriteArrayList<Transaction>();
                            tempTransactionListNull.add(tempTransactionList.get(0));
                            Block block = this.blockchain.createBlock(tempTransactionListNull,
                                    this.wallet, 2);
                            // System.out.println("blocvk created" + block.getBlockNumber());
                            String blockhash = block.getBlockHash();
                            Message message = this.cliqueMessagePool.message("COMMIT", block, this.wallet,
                                    this.blockchain.getRoundCounter());
                            if (!this.synchronizer.consensusReached.containsKey(blockhash)) {
                                this.synchronizer.consensusReached.put(blockhash, true);
                            }
                            this.blockchain.incrementRoundCounter();
                            // if (!this.blockchain.blockIds.contains(block.getBlockNumber())) {
                            // Heavy: if (!this.blockPool.blockExists(inblock) &&
                            // this.blockchain.isValidBlock(inblock)) {
                            this.blockchain.addBlockFinalized(block);
                            this.blockchain.lastBlockIndexRead.set(this.blockchain.getChain().size());
                            // } else {
                            // // HANDLE FORK
                            // // FIND THE BLOCK & its WEIGHT
                            // // REPLACE IF ITS LOWER WXEIGHT OR LEAVE AS IT IS
                            // this.blockchain.handleFork(block.getBlockNumber(), block.getWeight(), block);
                            // }
                            // this.blockPool.addBlock(block);
                            // Clear transaction Pool as we formed a block with those transactions as this
                            // for Proposer Node
                            try {
                                this.broadCastCommit(block, message, "blockproposed");
                                // this.synchronizer.consensusincourse = false;

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

                            // OFFROUND BLOCK CREATION with WEIGHT 1

                        } else if (false) {
                            if (this.blockchain.lastBlockIndexRead.get() == this.blockchain.getChain().size()) {
                                //System.out.println("Propose ALTERED" + currentround);
                                // this.synchronizer.consensusincourse = true;
                                // this.synchronizer.thresholdReached = false;
                                CopyOnWriteArrayList<Transaction> tempTransactionList = new CopyOnWriteArrayList<Transaction>(
                                        this.transactionPool.getTransactions(currentround, wallet));

                                Block block = this.blockchain.createBlock(tempTransactionList,
                                        this.wallet, 1);
                                // if (!this.blockchain.blockIds.contains(block.getBlockNumber())) {
                                // // Heavy: if (!this.blockPool.blockExists(inblock) &&
                                // // this.blockchain.isValidBlock(inblock)) {
                                this.blockchain.addBlockFinalized(block);
                                this.blockchain.lastBlockIndexRead.set(this.blockchain.getChain().size());
                                // }
                                // String blochash = block.getBlockHash();
                                Message message = this.cliqueMessagePool.message("COMMIT", block,
                                        this.wallet,
                                        this.blockchain.getRoundCounter());
                                // if (!this.synchronizer.consensusReached.containsKey(blochash)) {
                                // this.synchronizer.consensusReached.put(blochash, true);
                                // }
                                this.blockchain.incrementRoundCounter();
                                // // this.blockPool.addBlock(block);
                                // // Clear transaction Pool as we formed a block with those transactions as
                                // this
                                // // for Proposer Node

                                this.broadCastCommit(block, message, "blockproposed");
                                // this.synchronizer.consensusincourse = false;

                            }
                        }
                    }
                } catch (Exception e) {
                    // // TODO Auto-generated catch block
                    System.out.println("CliqueProposer" + e.toString());
                    e.printStackTrace();
                }

            }
        }
    }

    public CliqueTransactionMessageHandler(BlockingQueue<JSONObject> intransactionBlockingQueue, Blockchain blockChain,
            TransactionPool transactionPool, BlockPool blockPool, Wallet wallet, Validator validator,
            NonValidator nonValidator,
            NodeCommunicator nodeCommunicator, String currentuser,
            CliqueMessagePool cliqueMessagePool, Synchronizer synchronizer) {
        this.transactionBlockingQueue = intransactionBlockingQueue;
        this.blockchain = blockChain;
        this.transactionPool = transactionPool;
        this.blockPool = blockPool;
        this.wallet = wallet;
        this.validator = validator;
        this.nonValidator = nonValidator;
        int total = Integer.valueOf(NodeProperty.totalnodes);
        int totalValidators = Integer.valueOf(NodeProperty.validators);
        this.nodeCommunicator = nodeCommunicator;
        this.cliqueMessagePool = cliqueMessagePool;
        // (2N/3)+1
        float totalfloat = (float) totalValidators;
        float minapprovalsfloat = (totalfloat / 2.0f) + 1.0f;
        int minapprovalint = (int) minapprovalsfloat;
        this.minapprovals = minapprovalint;

        this.currentuser = currentuser;
        this.synchronizer = synchronizer;
    }

}
