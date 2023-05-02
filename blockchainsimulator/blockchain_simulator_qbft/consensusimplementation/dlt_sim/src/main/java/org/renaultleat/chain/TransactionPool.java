package org.renaultleat.chain;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.renaultleat.node.Transaction;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class TransactionPool {

    public volatile CopyOnWriteArrayList<String> confirmedTransactionIds;

    public volatile AtomicInteger roundcounter = new AtomicInteger(1);

    // Map of Key: Iteration / Round No / Block No | Value: List of Transactions
    public volatile Map<Integer, CopyOnWriteArrayList<Transaction>> transactionStorage;

    public Blockchain blockchain;

    // Map of Key: Iteration / Round No / Block No | Value: Full or Available Space
    // for adding txs
    public volatile Map<Integer, Boolean> transactionRoundStatus;

    public int transactionThreshold;

    public Map<Integer, Boolean> getTransactionRoundStatus() {
        return this.transactionRoundStatus;
    }

    public synchronized void addconfirmedTransactionId(String id) {
        this.confirmedTransactionIds.add(id);

    }

    public synchronized void addconfirmedTransactionIds(List<String> ids) {
        this.confirmedTransactionIds.addAll(ids);

    }

    public synchronized boolean confirmedTransactionIdsexists(List<String> ids) {
        return this.confirmedTransactionIds.containsAll(ids);

    }

    public synchronized boolean confirmedTransactionIdexists(String id) {
        return this.confirmedTransactionIds.contains(id);

    }

    public CopyOnWriteArrayList<Transaction> getTransactions(int roundno, Wallet wallet) {
        if (this.transactionStorage.containsKey(roundno)) {
            return this.transactionStorage.get(roundno);
        } else {
            Transaction inputTransaction = new Transaction("Simulation Test", wallet);
            CopyOnWriteArrayList<Transaction> tempTransactionListNull = new CopyOnWriteArrayList<Transaction>();
            tempTransactionListNull.add(inputTransaction);
            return tempTransactionListNull;
        }

    }

    public synchronized void setTransactions(CopyOnWriteArrayList<Transaction> transactions, int roundno) {
        this.transactionStorage.put(roundno, transactions);
    }

    public int getTransactionThreshold() {
        return this.transactionThreshold;
    }

    public void setTransactionThreshold(int transactionThreshold) {
        this.transactionThreshold = transactionThreshold;
    }

    public TransactionPool() {
        this.transactionStorage = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<Transaction>>();
        this.confirmedTransactionIds = new CopyOnWriteArrayList<String>();
        this.transactionRoundStatus = new ConcurrentHashMap<Integer, Boolean>();

        int transactionThreshold = Integer.valueOf(NodeProperty.threshold);
        this.transactionThreshold = transactionThreshold;
    }

    public synchronized boolean addTransaction(Transaction transaction) {
        int currentround = roundcounter.get();
        if (!this.transactionStorage.containsKey(currentround)) {
            CopyOnWriteArrayList<Transaction> transactions = new CopyOnWriteArrayList<Transaction>();
            this.transactionStorage.put(currentround, transactions);
        }
        if (!this.transactionRoundStatus.containsKey(currentround)) {
            this.transactionRoundStatus.put(currentround, false);
        }
        // Check if the transaction can be added to the round
        // Or we need to synchronise the current blockchain
        if (!this.transactionRoundStatus.get(currentround)) {
            this.transactionStorage.get(currentround).add(transaction);
        } /*
           * else {
           * // Careful: Add to next round recursive function!!!
           * addTransaction(this.roundcounter + 1, transaction);
           * }
           */
        // THRESHOLD REACHED
        if (this.transactionStorage.get(currentround).size() >= this.transactionThreshold) {
            this.transactionRoundStatus.put(currentround, true);
            this.roundcounter = new AtomicInteger(this.roundcounter.incrementAndGet());
            return true;
        } else {
            return false;
        }

    }

    public synchronized boolean verifyTransaction(Transaction transaction) {
        return Transaction.verifyTransaction(transaction);
    }

    public synchronized boolean transactionExists(Transaction intransaction, int roundno) {
        // Expensive Operation for huge blocksize
        Iterator<Transaction> transaction = this.transactionStorage.get(roundno).iterator();
        while (transaction.hasNext()) {
            if (intransaction.getId().equals(transaction.next().getId())) {
                return true;
            }
        }
        return false;

    }

    public synchronized void clearTransactionPool() {
        this.confirmedTransactionIds = new CopyOnWriteArrayList<String>();

        // Map of Key: Iteration / Round No / Block No | Value: List of Transactions
        this.transactionStorage = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<Transaction>>();

        // Map of Key: Iteration / Round No / Block No | Value: Full or Available Space
        // for adding txs
        this.transactionRoundStatus = new ConcurrentHashMap<Integer, Boolean>();
    }

    // Not needed as we store individual buckets of key in Map
    public synchronized void clearTransactionPoolFromIncomingBlock(int roundno,
            CopyOnWriteArrayList<Transaction> intransactions) {
        CopyOnWriteArrayList<Transaction> removalList = new CopyOnWriteArrayList<>();
        Iterator<Transaction> transaction = this.transactionStorage.get(roundno).iterator();
        while (transaction.hasNext()) {
            Transaction transactionnext = transaction.next();
            // Keep it inside as inner while loop stays at the end after first iteration of
            // outer loop finding one element
            Iterator<Transaction> intransaction = intransactions.iterator();
            while (intransaction.hasNext()) {
                if (intransaction.next().getId().equals(transactionnext.getId())) {
                    removalList.add(transactionnext);
                }
            }
        }
        this.transactionStorage.remove(roundno, removalList);
    }
}
