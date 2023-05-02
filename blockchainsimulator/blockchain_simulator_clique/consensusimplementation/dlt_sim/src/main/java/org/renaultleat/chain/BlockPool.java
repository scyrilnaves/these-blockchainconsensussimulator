package org.renaultleat.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockPool {
    volatile CopyOnWriteArrayList<Block> blocks;

    public volatile CopyOnWriteArrayList<Integer> blockIds = new CopyOnWriteArrayList<Integer>();

    public BlockPool() {
        this.blocks = new CopyOnWriteArrayList<Block>();
        this.blocks.add(Block.generateGenesis());
    }

    public synchronized boolean blockExists(Block inblock) {
        Iterator<Block> block = blocks.iterator();
        while (block.hasNext()) {
            if (inblock.getBlockHash() == block.next().getBlockHash()) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addBlock(Block block) {
        if (block.getBlockNumber() > blocks.get(blocks.size() - 1).getBlockNumber()) {
            this.blocks.add(block);
            this.blockIds.add(block.getBlockNumber());
        }

    }

    public synchronized void removeBlock(String inblockhash) {
        CopyOnWriteArrayList<Block> removallist = new CopyOnWriteArrayList<Block>();
        Iterator<Block> block = blocks.iterator();
        while (block.hasNext()) {
            Block currentblock = block.next();
            if (currentblock.getBlockHash().equals(inblockhash)) {
                removallist.add(currentblock);
                this.blockIds.remove(currentblock.getBlockNumber());
            }
        }
        this.blocks.removeAll(removallist);

    }

    public synchronized List<Block> getBlocks() {
        return this.blocks;

    }

    public synchronized Block getBlockforHash(String blockHash) {
        Block block = blocks.stream().filter(bloc -> blockHash.equals(bloc.getBlockHash())).findAny().orElse(null);
        return block;

    }

    public synchronized void clearBlockPool() {
        this.blocks = new CopyOnWriteArrayList<Block>();

    }

}
