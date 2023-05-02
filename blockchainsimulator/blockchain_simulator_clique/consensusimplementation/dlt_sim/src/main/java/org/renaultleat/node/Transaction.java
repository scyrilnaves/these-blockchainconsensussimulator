package org.renaultleat.node;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;

import org.renaultleat.crypto.CryptoUtil;

public class Transaction {

    public String id;

    public String from;

    public String nodeid;

    public int nodeindex;

    public String input;

    public Timestamp timestamp;

    public String hash;

    public String signature;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getNodeid() {
        return this.nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public int getNodeindex() {
        return this.nodeindex;
    }

    public void setNodeindex(int nodeindex) {
        this.nodeindex = nodeindex;
    }

    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Transaction(String id, String from, String nodeId, int nodeIndex, String input, Timestamp inputTimestamp,
            String hash, String signature) {
        this.id = id;
        this.nodeid = nodeId;
        this.nodeindex = nodeIndex;
        this.from = from;
        this.input = input;
        this.timestamp = inputTimestamp;
        this.hash = hash;
        this.signature = signature;

    }

    public Transaction(String data, Wallet wallet) {
        this.id = CryptoUtil.getUniqueIdentifier();
        this.nodeid = wallet.getNodeId();
        this.nodeindex = wallet.nodeindex;
        this.from = wallet.getPublicKey();
        this.input = data;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.hash = CryptoUtil.getHash(data);
        this.signature = wallet.signData(data);

    }

    public static boolean verifyTransaction(Transaction transaction) {
        try {
            return CryptoUtil.verify(transaction.nodeindex, transaction.signature,
                    CryptoUtil.getHash(transaction.input));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
}