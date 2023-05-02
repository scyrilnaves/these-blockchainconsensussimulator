package org.renaultleat.api;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;

import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.properties.NodeProperty;

public class Simulator_collator extends Thread {

    public Simulator_result simulator_result;

    public TransactionPool transactionPool;

    public Blockchain blockchain;

    // Get input tx/s from confirmed transaction ids

    // Get processed tx/s from
    boolean canRun = true;

    @Override
    public void run() {
        while (canRun) {
            // Input TPS Fill
            this.simulator_result.inputTpsStorage.put(new Timestamp(System.currentTimeMillis()),
                    this.transactionPool.getTransactionRoundStatus().size() * NodeProperty.getBlockSize());
            // Finalised Block Fill to later extrapolate with block size to get the
            // finalized tps
            this.simulator_result.finalisedTpsStorage.put(new Timestamp(System.currentTimeMillis()),
                    this.blockchain.getChain().size());
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        this.canRun = false;
    }

    // Constructor with result datastructure
    public Simulator_collator(Simulator_result simulator_result, TransactionPool transactionPool,
            Blockchain blockchain) {
        this.simulator_result = simulator_result;
        this.transactionPool = transactionPool;
        this.blockchain = blockchain;
    }

}
