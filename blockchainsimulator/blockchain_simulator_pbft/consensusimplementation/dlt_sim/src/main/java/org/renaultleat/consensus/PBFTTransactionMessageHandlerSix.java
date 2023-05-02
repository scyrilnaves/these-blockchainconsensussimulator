package org.renaultleat.consensus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.renaultleat.chain.Block;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.network.NodeCommunicator;
import org.renaultleat.network.NodeCommunicatorPenta;
import org.renaultleat.network.NodeCommunicatorQuar;
import org.renaultleat.network.NodeCommunicatorSix;
import org.renaultleat.network.NodeCommunicatorTer;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class PBFTTransactionMessageHandlerSix extends Thread {
    public BlockingQueue<JSONObject> transactionBlockingQueue;

    public Blockchain blockchain;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public PBFTMessagePool pbftMessagePool;

    public Wallet wallet;

    public Validator validator;

    public NodeCommunicatorSix nodeCommunicator;

    public Synchronizer synchronizer;

    private int minapprovals;

    public String currentuser;
    // Be careful as this is thread and need to be concurrent and synchronized
    // Thread Safe and Read from Main Memory
    // public volatile boolean thresholdReached = false;

    // public volatile boolean consensusincourse = false;

    public void broadCastPrePrepare(Block block, String message)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", this.currentuser);
        jsonObject.put("message", message);
        jsonObject.put("blockhash", block.getBlockHash());
        jsonObject.put("type", "PRE-PREPARE");
        Gson gson = new Gson();
        String data = gson.toJson(block);
        jsonObject.put("data", data);
        this.nodeCommunicator.sendMessage(jsonObject.toString());
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (true) {
            int currentround = this.blockchain.getCurrentRound();
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
                    if (!this.transactionPool.confirmedTransactionIds.contains(inputTransaction.getId())
                            && this.transactionPool.verifyTransaction(inputTransaction)
                            && this.validator.isValidValidator(inputTransaction.getFrom())) {
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
            }

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
            if (false) {
                if (this.transactionPool.transactionRoundStatus.containsKey(currentround)
                        && this.transactionPool.transactionRoundStatus.get(currentround)
                        && !this.synchronizer.consensusincourse) {
                    if (this.blockchain.getProposer().equals(this.wallet.getPublicKey())) {
                        this.synchronizer.consensusincourse = true;
                        // this.synchronizer.thresholdReached = false;
                        CopyOnWriteArrayList<Transaction> tempTransactionList = new CopyOnWriteArrayList<Transaction>(
                                this.transactionPool.getTransactions(currentround, wallet));
                        CopyOnWriteArrayList<Transaction> tempTransactionListNull = new CopyOnWriteArrayList<Transaction>();
                        tempTransactionListNull.add(tempTransactionList.get(0));
                        Block block = this.blockchain.createBlock(tempTransactionListNull,
                                this.wallet);
                        // this.blockPool.addBlock(block);
                        // Clear transaction Pool as we formed a block with those transactions as this
                        // for Proposer Node

                        try {
                            this.broadCastPrePrepare(block, "blockproposed");

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        // Heavy Operation
                        // this.transactionPool
                        // .clearTransactionPoolFromIncomingBlock(tempTransactionList);
                        // Setting back to default
                        this.synchronizer.consensusincourse = false;
                        // this.synchronizer.thresholdReached = true;
                    }
                }
            }

        }

    }

    public PBFTTransactionMessageHandlerSix(BlockingQueue<JSONObject> intransactionBlockingQueue,
            Blockchain blockChain,
            TransactionPool transactionPool, BlockPool blockPool, Wallet wallet, Validator validator,
            NodeCommunicatorSix nodeCommunicator, String currentuser,
            PBFTMessagePool pbftMessagePool, Synchronizer synchronizer) {
        this.transactionBlockingQueue = intransactionBlockingQueue;
        this.blockchain = blockChain;
        this.transactionPool = transactionPool;
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
    }

}
