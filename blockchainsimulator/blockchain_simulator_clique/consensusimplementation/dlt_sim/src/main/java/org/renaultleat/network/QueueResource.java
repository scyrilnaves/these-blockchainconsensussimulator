package org.renaultleat.network;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

// Class for Storing all Messages in Queue
public class QueueResource {
    // Queue for all transaction messages
    volatile BlockingQueue<JSONObject> transactionBlockingQueue = new LinkedBlockingQueue<JSONObject>();
    // Queue for all consensus messages
    volatile BlockingQueue<JSONObject> messageBlockingQueue = new LinkedBlockingQueue<JSONObject>();

    volatile BlockingQueue<JSONObject> transactionBlockingQueueSec = new LinkedBlockingQueue<JSONObject>();
    // Queue for all consensus messages
    volatile BlockingQueue<JSONObject> messageBlockingQueueSec = new LinkedBlockingQueue<JSONObject>();

    volatile BlockingQueue<JSONObject> transactionBlockingQueueTer = new LinkedBlockingQueue<JSONObject>();

    volatile BlockingQueue<JSONObject> transactionBlockingQueueQuar = new LinkedBlockingQueue<JSONObject>();

    volatile BlockingQueue<JSONObject> transactionBlockingQueuePenta = new LinkedBlockingQueue<JSONObject>();

    public synchronized BlockingQueue<JSONObject> getTransactionBlockingQueue() {
        return this.transactionBlockingQueue;
    }

    public synchronized void setTransactionBlockingQueue(BlockingQueue<JSONObject> transactionBlockingQueue) {
        this.transactionBlockingQueue = transactionBlockingQueue;
    }

    public synchronized BlockingQueue<JSONObject> getMessageBlockingQueue() {
        return this.messageBlockingQueue;
    }

    public synchronized void setMessageBlockingQueue(BlockingQueue<JSONObject> messageBlockingQueue) {
        this.messageBlockingQueue = messageBlockingQueue;
    }

    public synchronized BlockingQueue<JSONObject> getTransactionBlockingQueueSec() {
        return this.transactionBlockingQueueSec;
    }

    public synchronized BlockingQueue<JSONObject> getTransactionBlockingQueueTer() {
        return this.transactionBlockingQueueTer;
    }

    public synchronized BlockingQueue<JSONObject> getTransactionBlockingQueueQuar() {
        return this.transactionBlockingQueueQuar;
    }

    public synchronized BlockingQueue<JSONObject> getTransactionBlockingQueuePenta() {
        return this.transactionBlockingQueuePenta;
    }

    public synchronized BlockingQueue<JSONObject> getMessageBlockingQueueSec() {
        return this.messageBlockingQueueSec;
    }

}