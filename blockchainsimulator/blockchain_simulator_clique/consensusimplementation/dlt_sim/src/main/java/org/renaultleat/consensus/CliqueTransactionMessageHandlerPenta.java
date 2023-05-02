package org.renaultleat.consensus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.renaultleat.chain.Block;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.network.NodeCommunicator;
import org.renaultleat.network.NodeCommunicatorPenta;
import org.renaultleat.network.NodeCommunicatorQuar;
import org.renaultleat.network.NodeCommunicatorSec;
import org.renaultleat.network.NodeCommunicatorTer;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class CliqueTransactionMessageHandlerPenta extends Thread {
    public BlockingQueue<JSONObject> transactionBlockingQueue;

    public Blockchain blockchain;

    public TransactionPool transactionPool;

    public BlockPool blockPool;

    public CliqueMessagePool cliqueMessagePool;

    public Wallet wallet;

    public Validator validator;

    public NonValidator nonValidator;

    public NodeCommunicatorPenta nodeCommunicatorPenta;

    public Synchronizer synchronizer;

    private int minapprovals;

    public String currentuser;
    // Be careful as this is thread and need to be concurrent and synchronized
    // Thread Safe and Read from Main Memory
    // public volatile boolean thresholdReached = false;

    // public volatile boolean consensusincourse = false;

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

                    // TransactionPool transactionPool = transactionPoolCRUD.getTransactionPool();

                    this.synchronizer.thresholdReached = transactionPool
                            .addTransaction(inputTransaction);
                    transactionPool.confirmedTransactionIds.add(inputTransaction.getId());
                    // transactionPoolCRUD.updateTransactionPool(transactionPool);

                    try {
                        this.nodeCommunicatorPenta.broadCastTransaction(this.currentuser,
                                "transaction forwarded from", inputTransaction);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public CliqueTransactionMessageHandlerPenta(BlockingQueue<JSONObject> intransactionBlockingQueue,
            Blockchain blockChain,
            TransactionPool transactionPool, BlockPool blockPool, Wallet wallet, Validator validator,
            NonValidator nonValidator,
            NodeCommunicatorPenta nodeCommunicatorPenta, String currentuser,
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
        this.nodeCommunicatorPenta = nodeCommunicatorPenta;
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
