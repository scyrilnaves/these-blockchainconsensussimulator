package org.renaultleat.consensus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.renaultleat.api.Simulator_result;
import org.renaultleat.chain.Block;
import org.renaultleat.chain.BlockPool;
import org.renaultleat.chain.Blockchain;
import org.renaultleat.chain.TransactionPool;
import org.renaultleat.network.NodeCommunicator;
import org.renaultleat.node.NonValidator;
import org.renaultleat.node.Validator;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

/**
 * This Handler is used in case of Multi threaded call from P2PHandler or single
 * threaded we call PBFTMessageHandler
 * 
 */
public class IBFTConsensusRoundChangeHandler extends AbstractScheduledService {

    public volatile AtomicInteger roundCounter = new AtomicInteger(1);

    public NodeCommunicator nodeCommunicator;

    public String currentuser;

    public Synchronizer synchronizer;

    // To increment Round Counter
    public Blockchain blockChain;

    public IBFTMessagePool ibftMessagePool;

    public Wallet wallet;

    public Timer timer;

    public BlockPool blockPool;

    // Be careful as this is thread and need to be concurrent and synchronized
    // Thread Safe and Read from Main Memory
    // public volatile Map<String, Boolean> consensusReached = new HashMap<String,
    // Boolean>();

    public void broadCastRoundChange(Message data, String message, String user, String blockhash)
            throws IOException {
        if (NodeProperty.isValidator()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.currentuser);
            jsonObject.put("message", message);
            jsonObject.put("blockhash", blockhash);
            jsonObject.put("type", "ROUNDCHANGE");
            Gson gson = new Gson();
            String datajson = gson.toJson(data);
            jsonObject.put("data", datajson);
            this.nodeCommunicator.sendMessage(jsonObject.toString());
        }
    }

    public void scheduleTimer() {
        try {
            this.startAsync();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void rescheduleTimer() {
        try {
            this.shutDown();
            this.startUp();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void runOneIteration() throws Exception {
        // Run only if the counter has not progressed
        if (this.roundCounter.get() == this.blockChain.roundCounter.get()) {
            task();
        } else {
            this.roundCounter.set(this.blockChain.roundCounter.get());
        }

    }

    public void task() {
        // Only if validator
       // System.out.println("called task");
        if (NodeProperty.isValidator()) {
            // Get the latest block unconfirmed in block pool
            Block pendingblock = this.blockPool.getBlocks().get(this.blockChain.getChain().size());
            if (pendingblock != null) {
                // Send Round Change Message
                Message roundchangemessage = this.ibftMessagePool.message("ROUNDCHANGE",
                        pendingblock,
                        this.wallet, this.blockChain.getRoundCounter());
                try {
                    broadCastRoundChange(roundchangemessage, "ROUNDCHANGE INITIATE",
                            this.wallet.getPublicKey(), roundchangemessage.getBlockHash());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // rescheduleTimer();
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, NodeProperty.getRoundChange(), TimeUnit.MILLISECONDS);
    }

    public IBFTConsensusRoundChangeHandler(
            NodeCommunicator nodeCommunicator, String currentuser, Synchronizer synchronizer, Blockchain blockChain,
            IBFTMessagePool ibftMessagePool, Wallet wallet, BlockPool blockPool) {
        this.nodeCommunicator = nodeCommunicator;
        this.currentuser = currentuser;
        this.synchronizer = synchronizer;
        this.blockChain = blockChain;
        this.ibftMessagePool = ibftMessagePool;
        this.wallet = wallet;
        this.blockPool = blockPool;
    }

}