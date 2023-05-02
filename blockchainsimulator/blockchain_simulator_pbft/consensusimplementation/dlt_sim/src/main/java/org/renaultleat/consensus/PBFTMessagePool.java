package org.renaultleat.consensus;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.renaultleat.chain.Block;
import org.renaultleat.crypto.CryptoUtil;
import org.renaultleat.node.Wallet;

public class PBFTMessagePool {
    // We verify the message before for each validator so no need to store the
    // entire message
    // Store only the ACK of the message instead of the entire message as it will be
    // discarded
    // Blockhash, Message Sender
    public volatile Map<String, CopyOnWriteArrayList<String>> preparePool;
    // Blockhash,Message Sender
    public volatile Map<String, CopyOnWriteArrayList<String>> commitPool;
    // Blockhash,Message Sender
    public volatile Map<String, CopyOnWriteArrayList<String>> roundChangePool;

    public Map<String, CopyOnWriteArrayList<String>> getPreparePool() {
        return this.preparePool;
    }

    public void setPreparePool(Map<String, CopyOnWriteArrayList<String>> preparePool) {
        this.preparePool = preparePool;
    }

    public Map<String, CopyOnWriteArrayList<String>> getCommmitPool() {
        return this.commitPool;
    }

    public void setCommmitPool(Map<String, CopyOnWriteArrayList<String>> commmitPool) {
        this.commitPool = commmitPool;
    }

    public Map<String, CopyOnWriteArrayList<String>> getRoundChangePool() {
        return this.roundChangePool;
    }

    public void setRoundChangePool(Map<String, CopyOnWriteArrayList<String>> roundChangePool) {
        this.roundChangePool = roundChangePool;
    }

    public PBFTMessagePool() {
        this.preparePool = new HashMap<String, CopyOnWriteArrayList<String>>();
        this.commitPool = new HashMap<String, CopyOnWriteArrayList<String>>();
        this.roundChangePool = new HashMap<String, CopyOnWriteArrayList<String>>();
    }

    public synchronized Message message(String type, Block block, Wallet wallet) {
        Message message = new Message(type, block.getBlockHash(),
                wallet.signData(block.getBlockHash() + CryptoUtil.getHash(type)),
                wallet.getPublicKey(), type);
        if (message.type.equals("PREPARE")) {
            if (this.preparePool.get(message.blockHash) != null) {
                this.preparePool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> preparePoolMessages = new CopyOnWriteArrayList<String>();
                preparePoolMessages.add(message.getMessageSender());
                this.preparePool.put(message.blockHash, preparePoolMessages);
            }

        } else if (message.type.equals("COMMIT")) {
            if (this.commitPool.get(message.blockHash) != null) {
                this.commitPool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> commitPoolMessages = new CopyOnWriteArrayList<String>();
                commitPoolMessages.add(message.getMessageSender());
                this.commitPool.put(message.blockHash, commitPoolMessages);
            }
        } else if (message.type.equals("ROUNDCHANGE")) {
            if (this.roundChangePool.get(message.blockHash) != null) {
                this.roundChangePool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> roundChangePoolMessages = new CopyOnWriteArrayList<String>();
                roundChangePoolMessages.add(message.getMessageSender());
                this.roundChangePool.put(message.blockHash, roundChangePoolMessages);
            }
        }
        return message;
    }

    public synchronized Message message(String type, Message inmessage, Wallet wallet) {
        Message message = new Message(type, inmessage.getBlockHash(),
                wallet.signData(inmessage.getBlockHash() + CryptoUtil.getHash(type)),
                wallet.getPublicKey(), type);
        if (message.type.equals("PREPARE")) {
            if (this.preparePool.get(message.blockHash) != null) {
                this.preparePool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> preparePoolMessages = new CopyOnWriteArrayList<String>();
                preparePoolMessages.add(message.getMessageSender());
                this.preparePool.put(message.blockHash, preparePoolMessages);
            }

        } else if (message.type.equals("COMMIT")) {
            if (this.commitPool.get(message.blockHash) != null) {
                this.commitPool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> commitPoolMessages = new CopyOnWriteArrayList<String>();
                commitPoolMessages.add(message.getMessageSender());
                this.commitPool.put(message.blockHash, commitPoolMessages);
            }
        } else if (message.type.equals("ROUNDCHANGE")) {
            if (this.roundChangePool.get(message.blockHash) != null) {
                this.roundChangePool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> roundChangePoolMessages = new CopyOnWriteArrayList<String>();
                roundChangePoolMessages.add(message.getMessageSender());
                this.roundChangePool.put(message.blockHash, roundChangePoolMessages);
            }
        }
        return message;
    }

    public synchronized boolean addMessage(Message message) {
        if (message.type.equals("PREPARE")) {
            if (this.preparePool.get(message.blockHash) != null) {
                this.preparePool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> preparePoolMessages = new CopyOnWriteArrayList<String>();
                preparePoolMessages.add(message.getMessageSender());
                this.preparePool.put(message.blockHash, preparePoolMessages);
            }
            return true;
        } else if (message.type.equals("COMMIT")) {
            if (this.commitPool.get(message.blockHash) != null) {
                this.commitPool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> commitPoolMessages = new CopyOnWriteArrayList<String>();
                commitPoolMessages.add(message.getMessageSender());
                this.commitPool.put(message.blockHash, commitPoolMessages);
            }
            return true;
        } else if (message.type.equals("ROUNDCHANGE")) {
            if (this.roundChangePool.get(message.blockHash) != null) {
                this.roundChangePool.get(message.blockHash).add(message.getMessageSender());
            } else {
                CopyOnWriteArrayList<String> roundChangePoolMessages = new CopyOnWriteArrayList<String>();
                roundChangePoolMessages.add(message.getMessageSender());
                this.roundChangePool.put(message.blockHash, roundChangePoolMessages);
            }
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean existingMessage(Message message) {
        if (message.type.equals("PREPARE")) {
            return this.preparePool.containsKey(message.getBlockHash())
                    && this.preparePool.get(message.getBlockHash()).contains(message.getMessageSender());
        } else if (message.type.equals("COMMIT")) {
            return this.commitPool.containsKey(message.getBlockHash())
                    && this.commitPool.get(message.getBlockHash()).contains(message.getMessageSender());
        } else if (message.type.equals("ROUNDCHANGE")) {
            return this.roundChangePool.containsKey(message.getBlockHash())
                    && this.roundChangePool.get(message.getBlockHash()).contains(message.getMessageSender());
        } else {
            return false;
        }
    }

    public synchronized boolean existingPrepareMessage(Message message) {

        return this.preparePool.containsKey(message.getBlockHash())
                && this.preparePool.get(message.getBlockHash()).contains(message.getMessageSender());

    }

    public synchronized boolean existingCommitMessage(Message message) {

        return this.commitPool.containsKey(message.getBlockHash())
                && this.commitPool.get(message.getBlockHash()).contains(message.getMessageSender());

    }

    public synchronized boolean existingRoundChangeMessage(Message message) {

        return this.roundChangePool.containsKey(message.getBlockHash())
                && this.roundChangePool.get(message.getBlockHash()).contains(message.getMessageSender());

    }

    public synchronized void clearAllMessagePool() {
        this.preparePool = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();
        this.commitPool = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();
        this.roundChangePool = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();
    }

    public synchronized int getSizeofPrepareMessagePoolForBlockHash(String blockhash) {
        if (this.preparePool.containsKey(blockhash)) {
            return this.preparePool.get(blockhash).size();
        }
        return 0;
    }

    public synchronized int getSizeofCommitMessagePoolForBlockHash(String blockhash) {
        if (this.commitPool.containsKey(blockhash)) {
            return this.commitPool.get(blockhash).size();
        }
        return 0;
    }

    public synchronized int getSizeofRoundChangeMessagePoolForBlockHash(String blockhash) {
        if (this.roundChangePool.containsKey(blockhash)) {
            return this.roundChangePool.get(blockhash).size();
        }
        return 0;

    }

    public synchronized void clearAllMessagePoolForBlockHash(String blockhash) {
        this.preparePool.remove(blockhash);
        this.commitPool.remove(blockhash);
        this.roundChangePool.remove(blockhash);
    }

    public synchronized void clearPrepareMessagePoolForBlockHash(String blockhash) {
        this.preparePool.remove(blockhash);

    }

    public synchronized void clearCommitMessagePoolForBlockHash(String blockhash) {
        this.commitPool.remove(blockhash);

    }

    public synchronized void clearRoundChangeMessagePoolForBlockHash(String blockhash) {
        this.roundChangePool.remove(blockhash);

    }

    public synchronized boolean isValidMessage(Message message) {
        try {
            return CryptoUtil.verify(message.getMessageSender(), message.getMessagesignature(),
                    message.getBlockHash() + CryptoUtil.getHash(message.getType()));
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException
                | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}
