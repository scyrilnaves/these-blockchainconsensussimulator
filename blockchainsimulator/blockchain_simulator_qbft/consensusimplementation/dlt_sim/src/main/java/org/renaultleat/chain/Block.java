package org.renaultleat.chain;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.renaultleat.consensus.Message;
import org.renaultleat.crypto.CryptoUtil;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Wallet;

public class Block {

    Timestamp blocktime;
    String previousblockhash;
    String hash;
    String proposer;
    String signature;
    int blocknumber;
    // QBFT Change
    int roundno;

    CopyOnWriteArrayList<Transaction> transactions = new CopyOnWriteArrayList<Transaction>();

    CopyOnWriteArrayList<String> prepareMessageValidators = new CopyOnWriteArrayList<String>();

    CopyOnWriteArrayList<String> commitMessageValidators = new CopyOnWriteArrayList<String>();

    public Timestamp getBlocktime() {
        return this.blocktime;
    }

    public void setBlocktime(Timestamp blocktime) {
        this.blocktime = blocktime;
    }

    public int getRoundNo() {
        return this.roundno;
    }

    public void setRoundNo(int roundno) {
        this.roundno = roundno;
    }

    public String getPreviousblockhash() {
        return this.previousblockhash;
    }

    public void setPreviousblockhash(String previousblockhash) {
        this.previousblockhash = previousblockhash;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public CopyOnWriteArrayList<Transaction> getTransactions() {
        return this.transactions;
    }

    public void setTransactions(CopyOnWriteArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public CopyOnWriteArrayList<String> getPrepareMessages() {
        return this.prepareMessageValidators;
    }

    public void setPrepareMessages(CopyOnWriteArrayList<String> prepareMessages) {
        this.prepareMessageValidators = prepareMessages;
    }

    public CopyOnWriteArrayList<String> getCommitMessages() {
        return this.commitMessageValidators;
    }

    public void setCommitMessages(CopyOnWriteArrayList<String> commitMessages) {
        this.commitMessageValidators = commitMessages;
    }

    public String getProposer() {
        return this.proposer;
    }

    public void setProposer(String proposer) {
        this.proposer = proposer;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getBlocknumber() {
        return this.blocknumber;
    }

    public void setBlocknumber(int blocknumber) {
        this.blocknumber = blocknumber;
    }

    public String getBlockDetails() {
        return "blocktime:" + this.blocktime.toString() + ";" + "previousblockhash:" + this.previousblockhash + ";"
                + "hash:" + this.hash + ";" + ";" + "transactionsize:" + String.valueOf(transactions.size()) + ";"
                + "proposer" + this.proposer + ";" + "signature:" + this.signature + "blocknumber:"
                + String.valueOf(this.blocknumber);
    }

    public String getLastBlockHash() {
        return this.previousblockhash;
    }

    public String getBlockHash() {
        return this.hash;
    }

    public String getBlockSignature() {
        return this.signature;
    }

    public int getBlockNumber() {
        return this.blocknumber;
    }

    public String getBlockProposer() {
        return this.proposer;
    }

    public Timestamp getBlockTimestamp() {
        return this.blocktime;
    }

    public CopyOnWriteArrayList<Transaction> getBlockData() {
        return this.transactions;
    }

    public Block() {

    }

    public Block(Timestamp timestamp, String previousblockhash, String currentblockhash,
            CopyOnWriteArrayList<Transaction> data,
            String blockproposer, String signature, int blocknum) {
        this.blocktime = timestamp;
        this.previousblockhash = previousblockhash;
        this.hash = currentblockhash;
        this.transactions = data;
        this.proposer = blockproposer;
        this.signature = signature;
        this.blocknumber = blocknum;
    }

    public static Block generateGenesis() {
        // should be uniform
        return new Block(null, "genesishash", "genesisblockhash", new CopyOnWriteArrayList<Transaction>(), "genesis",
                null,
                0);
    }

    public static Block createBlock(Block lastBlock, CopyOnWriteArrayList<Transaction> data, Wallet wallet) {

        String lastblockHash = lastBlock.getBlockHash();
        Timestamp timeStampcurrent = new Timestamp(System.currentTimeMillis());
        String proposer = wallet.getPublicKey();
        String blockhash = generateBlockHash(lastblockHash, data, proposer);

        // String proposer =
        // Base64.getEncoder().encodeToString(proposerkey.getEncoded());
        String signature = signBlockHash(blockhash, wallet);
        return new Block(timeStampcurrent, lastblockHash, blockhash, data, proposer, signature,
                lastBlock.getBlockNumber() + 1);
    }

    public static String generateBlockHash(String lastblockHash,
            List<Transaction> transactions, String proposer) {
        return CryptoUtil.getHash(lastblockHash +
                transactions.get(0).getId()
                + transactions.get(transactions.size() - 1).getId() + proposer);

    }

    public static String generateBlockHash(Block block) {
        return CryptoUtil.getHash(block.getLastBlockHash() +
                block.getBlockData().get(0).getId()
                + block.getBlockData().get(block.getBlockData().size() - 1).getId() + block.getBlockProposer());

    }

    public static String signBlockHash(String blockHash, Wallet wallet) {
        return wallet.signData(blockHash);

    }

    public static boolean verifyBlock(Block block) {
        try {
            return CryptoUtil.verify(block.getBlockProposer(), block.getBlockSignature(),
                    Block.generateBlockHash(block.getLastBlockHash(), block.getBlockData(), block.getBlockProposer()));
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException
                | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyProposer(Block block, String proposer) {
        return block.getBlockProposer().equals(proposer);
    }

}
