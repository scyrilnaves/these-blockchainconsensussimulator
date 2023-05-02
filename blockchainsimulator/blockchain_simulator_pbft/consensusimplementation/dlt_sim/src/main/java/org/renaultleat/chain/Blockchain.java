package org.renaultleat.chain;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.renaultleat.consensus.PBFTMessagePool;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class Blockchain {

    public volatile CopyOnWriteArrayList<Integer> blockIds = new CopyOnWriteArrayList<Integer>();

    Validator validator;

    CopyOnWriteArrayList<String> validators;
    volatile CopyOnWriteArrayList<Block> chain;

    public CopyOnWriteArrayList<String> getValidators() {
        return this.validators;
    }

    public void setValidators(CopyOnWriteArrayList<String> validators) {
        this.validators = validators;
    }

    public CopyOnWriteArrayList<Block> getChain() {
        return this.chain;
    }

    public void setChain(CopyOnWriteArrayList<Block> chain) {
        this.chain = chain;
    }

    public Blockchain() {

    }

    public Blockchain(Validator validator) {
        this.chain = new CopyOnWriteArrayList<Block>();
        this.chain.add(Block.generateGenesis());
        this.validators = new CopyOnWriteArrayList<String>();
        try {
            this.validators.addAll(validator.generateAddresses());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Block addBlock(Block block) {
        this.chain.add(block);
        return block;
    }

    public synchronized Block createBlock(CopyOnWriteArrayList<Transaction> transactions, Wallet wallet) {
        Block block = Block.createBlock(this.chain.get(chain.size() - 1), transactions, wallet);
        return block;
    }

    /**
     * Get the latest block hash and get the unicode value at position 2
     * Then get the modulo value with the total nodes
     * 
     * @return
     */
    public String getProposer() {
        int totalnodes = Integer.valueOf(NodeProperty.totalnodes);
        int index = this.chain.get(chain.size() - 1).getBlockHash().codePointAt(2) % totalnodes;
        return validators.get(index);
        /*
         * if (!validators.get(index).equals(this.chain.get(chain.size() -
         * 1).getBlockProposer())) {
         * return validators.get(index);
         * } else {
         * int newindex = this.chain.get(chain.size() - 1).getBlockHash().codePointAt(3)
         * % totalnodes;
         * return validators.get(newindex);
         * }
         */
    }

    /**
     * Get the next block expected as round no
     */
    public synchronized int getCurrentRound() {
        return this.chain.get(chain.size() - 1).getBlockNumber() + 1;
    }

    public boolean isValidBlock(Block inblock) {
        Block nodeLatestBlock = this.chain.get(chain.size() - 1);

        // Block No Sequence Verification
        // Block Hash of previous and current
        // Block Hash verification of current

        if ((Integer.valueOf(nodeLatestBlock.getBlockNumber() + 1)).equals(Integer.valueOf(inblock.getBlockNumber()))
                && nodeLatestBlock.getBlockHash().equals(inblock.getLastBlockHash())
                && inblock.getBlockHash().equals(Block.generateBlockHash(inblock)) && Block.verifyBlock(inblock)
                && Block.verifyProposer(inblock, getProposer())) {
            return true;
        }
        return true;

    }

    // Append Commit and Prepare Messages to a Block
    public void addUpdatedBlock(Block block, BlockPool blockPool, PBFTMessagePool pbftMessagePool) {

        // Block block = blockPool.getBlockforHash(hash);
        // System.out.println("NU" + block.getBlocknumber());
        // if (!this.blockIds.contains(block.getBlocknumber())) {
        if (block.getBlockNumber() > this.chain.get(this.chain.size() - 1).getBlockNumber()) {
            // block.getPrepareMessages().addAll(pbftMessagePool.getPreparePool().get(hash));
            // block.getCommitMessages().addAll(pbftMessagePool.getCommmitPool().get(hash));
            this.addBlock(block);
            this.blockIds.add(block.getBlocknumber());
            // }
        }
    }

}
