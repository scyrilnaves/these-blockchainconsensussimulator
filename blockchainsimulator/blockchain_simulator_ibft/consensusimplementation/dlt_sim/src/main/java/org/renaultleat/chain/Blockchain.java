package org.renaultleat.chain;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.renaultleat.consensus.IBFTMessagePool;
import org.renaultleat.crypto.CryptoUtil;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class Blockchain {

    public volatile AtomicInteger roundCounter = new AtomicInteger(1);

    public volatile CopyOnWriteArrayList<Integer> blockIds = new CopyOnWriteArrayList<Integer>();

    Validator validator;

    NonValidator nonValidator;

    CopyOnWriteArrayList<String> validators;

    CopyOnWriteArrayList<String> nonValidators;

    volatile CopyOnWriteArrayList<Block> chain;

    public CopyOnWriteArrayList<String> getValidators() {
        return this.validators;
    }

    public void setNonValidators(CopyOnWriteArrayList<String> nonValidators) {
        this.nonValidators = nonValidators;
    }

    public CopyOnWriteArrayList<String> getNonValidators() {
        return this.nonValidators;
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

    // Update Round After each block finalisation
    public void setRoundCounter(int newRoundCounter) {
        this.roundCounter.set(newRoundCounter);
    }

    // Update Round After each block finalisation
    public void incrementRoundCounter() {
        this.roundCounter.incrementAndGet();
    }

    // Update Round After each block finalisation
    public int getRoundCounter() {
        return this.roundCounter.get();
    }

    public Blockchain() {

    }

    public Blockchain(Validator validator, NonValidator nonValidator) {
        this.validator = validator;
        this.nonValidator = nonValidator;
        this.chain = new CopyOnWriteArrayList<Block>();
        this.chain.add(Block.generateGenesis());
        this.validators = new CopyOnWriteArrayList<String>();
        this.nonValidators = new CopyOnWriteArrayList<String>();
        try {
            this.validators.addAll(validator.generateAddresses());
            this.nonValidators.addAll(nonValidator.generateAddresses());
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
        // Increment Round Counter Auto
        // this.incrementRoundCounter();
        // this.addBlock(block);
        return block;
    }

    /**
     * Get the latest block hash + Round as IBFT is dependent on Rounds and get the
     * unicode value at position 2
     * Then get the modulo value with the total nodes
     * 
     * @return
     */
    // IBFT Changed to the validators and not total nodes
    public String getProposer() {
        int totalvalidators = Integer.valueOf(NodeProperty.validators);
        int index = CryptoUtil.getHash(this.chain.get(chain.size() - 1).getBlockHash())
                .codePointAt(2) % totalvalidators;
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

    // Incase of Round Change we revert to the earliest block producer
    // IBFT Changed to the validators and not total nodes
    public String getRoundChangeProposer() {
        int totalvalidators = Integer.valueOf(NodeProperty.validators);
        int index = CryptoUtil.getHash(this.chain.get(chain.size() - 2).getBlockHash())
                .codePointAt(2) % totalvalidators;
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

    public boolean isValidBlock(Block inblock) {
        Block nodeLatestBlock = this.chain.get(chain.size() - 1);

        // Round No Verification
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
    public void addUpdatedBlock(Block block, BlockPool blockPool, IBFTMessagePool pbftMessagePool) {

        // Block block = blockPool.getBlockforHash(hash);
        // if (block != null) {
        // if (!this.blockIds.contains(block.getBlocknumber())) {
        // if (this.chain.get(this.chain.size() - 1).getBlockNumber() + 1 ==
        // block.getBlockNumber()) {
        // block.getPrepareMessages().addAll(pbftMessagePool.getPreparePool().get(hash));
        // block.getCommitMessages().addAll(pbftMessagePool.getCommmitPool().get(hash));
        if (block.getBlockNumber() > this.chain.get(this.chain.size() - 1).getBlockNumber()) {
            // block.getPrepareMessages().addAll(pbftMessagePool.getPreparePool().get(hash));
            // block.getCommitMessages().addAll(pbftMessagePool.getCommmitPool().get(hash));
            this.addBlock(block);
            this.blockIds.add(block.getBlocknumber());
            // }
        }

        // }
        // }
        // }
    }

}
