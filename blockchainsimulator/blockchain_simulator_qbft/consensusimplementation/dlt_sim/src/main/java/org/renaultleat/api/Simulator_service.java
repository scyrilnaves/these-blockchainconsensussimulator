package org.renaultleat.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.json.JSONObject;
import org.renaultleat.chain.Block;
import org.renaultleat.network.P2PServer;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class Simulator_service {

    P2PServer p2pServer;

    Wallet wallet;

    Simulator_result simulator_result;

    public Simulator_service(P2PServer p2pServer, Wallet wallet, Simulator_result simulator_result) {
        this.p2pServer = p2pServer;
        this.wallet = wallet;
        this.simulator_result = simulator_result;
    }

    public Simulator_service(Wallet wallet) {
        this.wallet = wallet;
    }

    public void storeNodeProperty(String value) {
        Wallet.setnodeproperty(value);
        wallet.initalise(Integer.parseInt(value));
    }

    public void storeNodeLatency(String value) {
        NodeProperty.setnodeLatency(Long.valueOf(value));

    }

    public long getNodeLatency() {
        return NodeProperty.getnodeLatency();

    }

    public void setRoundChange(String value) {
        NodeProperty.setRoundChange(Long.valueOf(value));

    }

    public long getRoundChange() {
        return NodeProperty.getRoundChange();

    }

    public void storeAsValidator(String value) {
        NodeProperty.setIsValidator(Boolean.valueOf(value));

    }

    public boolean getAsValidator() {
        return NodeProperty.isValidator();

    }

    public void storeNodeBehavior(String value) {
        NodeProperty.setnodeBehavior(Integer.valueOf(value));

    }

    public String getNodeBehavior() {
        if (NodeProperty.getnodeBehavior() == 0) {
            return "Honest :)";
        } else {
            return "DisHonest!!!";
        }

    }

    public void storeNodeNetwork(String value) {
        NodeProperty.setnodeNetwork(value);

    }

    public String getNodeNetwork() {
        return NodeProperty.getnodeNetwork();

    }

    public void storeBlockSize(String value) {
        NodeProperty.setBlockSize(Integer.valueOf(value));

    }

    public void storeIPS(String value) {
        NodeProperty.setIPS(value);

    }

    public String getIPS() {
        return NodeProperty.getIPS();

    }

    public String getIP() {
        return NodeProperty.getIP();

    }

    public int getBlockSize() {
        return NodeProperty.getBlockSize();

    }

    public void storeTotalNodes(String value) {
        NodeProperty.settotalNodes(Integer.valueOf(value));

    }

    public int getTotalNodes() {
        return NodeProperty.gettotalNodes();

    }

    public void storeTotalValidators(String value) {
        NodeProperty.setValidators(Integer.valueOf(value));

    }

    public int getTotalValidators() {
        return NodeProperty.getValidators();

    }

    public String getVersion() {
        String version = NodeProperty.deployed;
        return version;

    }

    public int getNodeProperty() {
        int index = Integer.valueOf(Wallet.getNodeproperty());
        return index;

    }

    public String getBlocks() {
        String value = "";
        List<Block> chain = p2pServer.blockchain.getChain();
        for (Block block : chain) {
            value += "blockno: " + block.getBlockNumber() + "; " + "proposer: " + block.getBlockProposer();
        }
        return value;

    }

    public String getBlocksinBlockPool() {
        String value = "";
        if (p2pServer != null) {
            List<Block> chain = p2pServer.blockPool.getBlocks();
            for (Block block : chain) {
                value += "blockno: " + block.getBlockNumber() + "; " + "proposer: " + block.getBlockProposer();
            }
        }
        return value;

    }

    public String getTotalTransactionsValidatedNo() {
        if (p2pServer != null) {
            List<Block> chain = p2pServer.blockchain.getChain();
            Integer total = 0;
            // for (Block block : chain) {
            // total += block.getBlockData().size();
            // }
            total = chain.size() * NodeProperty.getBlockSize();
            return String.valueOf(total);
        }
        return "0";
    }

    public String getTransactionsinPoolNo(String roundno) {
        int roundnumber = Integer.valueOf(roundno);
        if (p2pServer != null) {
            int size = p2pServer.transactionPool.transactionStorage.get(roundnumber).size();
            String value = String.valueOf(size);
            return value;
        }
        return "0";

    }

    public int getNoofBlocks() {
        if (p2pServer != null) {
            int noofblocks = p2pServer.blockchain.getChain().size();
            return noofblocks;
        }
        return 0;

    }

    public void initiateConnection() {
        if (p2pServer != null) {
            try {
                p2pServer.connect();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void listentoPeers() {
        if (p2pServer != null) {
            try {
                p2pServer.connectToPeers();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void sendMessageToPeer(String message) {
        if (p2pServer != null) {
            try {
                p2pServer.communicate(message);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void startSimulation(String notxs, String nothreads) {
        if (p2pServer != null) {
            int no_of_txs = Integer.valueOf(notxs);
            int no_of_threads = Integer.valueOf(nothreads);
            try {
                p2pServer.startSimulationBroadcast(no_of_txs, no_of_threads);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void startSimulationSingle(String notxs) {
        if (p2pServer != null) {
            int no_of_txs = Integer.valueOf(notxs);
            try {
                p2pServer.startSimulationBroadcastSingle(no_of_txs);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Map<Timestamp, Integer> getInputTPS() {
        Map<Timestamp, Integer> inputTps = new LinkedHashMap<Timestamp, Integer>();
        Map<Timestamp, Integer> inputTpsStorage = this.simulator_result.inputTpsStorage;
        List<Timestamp> keys = new ArrayList<>(inputTpsStorage.keySet());
        for (int i = 0; i < keys.size() - 1; i++) {
            // Millisecond
            long milliseconddifference = keys.get(i + 1).getTime() - keys.get(i).getTime();
            int second = (int) milliseconddifference / 1000;
            int txdifference = inputTpsStorage.get(keys.get(i + 1)) - inputTpsStorage.get(keys.get(i));
            int tps = txdifference / second;
            inputTps.put(keys.get(i + 1), tps);

        }
        return inputTps;
    }

    public String getProcessedTPS() {
        SortedMap<Timestamp, Integer> resultMap = this.simulator_result.inputTpsStorage;
        List<Timestamp> keys = new ArrayList<>(resultMap.keySet());
        int tps = 0;
        for (int i = 0; i < keys.size() - 1; i++) {
            // Millisecond
            tps += resultMap.get(keys.get(i));
        }
        return String.valueOf(tps);
    }

    public Map<Timestamp, Integer> getFinalisedTPS() {

        Map<Timestamp, Integer> finalisedTps = new LinkedHashMap<Timestamp, Integer>();
        Map<Timestamp, Integer> finalisedTpsStorage = this.simulator_result.finalisedTpsStorage;
        List<Timestamp> keys = new ArrayList<>(finalisedTpsStorage.keySet());
        for (int i = 0; i < keys.size() - 1; i++) {
            // Millisecond
            long milliseconddifference = keys.get(i + 1).getTime() - keys.get(i).getTime();
            int second = (int) milliseconddifference / 1000;
            int txdifference = finalisedTpsStorage.get(keys.get(i + 1)) * NodeProperty.blocksize
                    - finalisedTpsStorage.get(keys.get(i)) * NodeProperty.blocksize;
            int tps = txdifference / second;
            finalisedTps.put(keys.get(i + 1), tps);

        }
        return finalisedTps;

    }

    public List<JSONObject> getFinalisedTPSJSON() {

        return getJSONData(getFinalisedTPS());

    }

    public List<JSONObject> getInputTPSJSON() {

        return getJSONData(getInputTPS());

    }

    public List<JSONObject> getPrepareTPSJSON() {

        return getJSONData(getPrepareMessageRate());

    }

    public List<JSONObject> getCommitTPSJSON() {

        return getJSONData(getCommitMessageRate());

    }

    public List<JSONObject> getRoundChangeTPSJSON() {

        return getJSONData(getRoundChangeMessageRate());

    }

    public Map<Timestamp, Integer> getPrepareMessageRate() {

        Map<Timestamp, Integer> prepareTpsStorage = this.simulator_result.prepareCounter;

        return prepareTpsStorage;

    }

    public Map<Timestamp, Integer> getCommitMessageRate() {
        Map<Timestamp, Integer> commitTpsStorage = this.simulator_result.commitCounter;

        return commitTpsStorage;

    }

    public Map<Timestamp, Integer> getRoundChangeMessageRate() {
        Map<Timestamp, Integer> roundChangeTpsStorage = this.simulator_result.roundChangeCounter;

        return roundChangeTpsStorage;

    }

    public List<JSONObject> getJSONData(Map<Timestamp, Integer> objData) {
        List<JSONObject> jsonResult = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
        for (Map.Entry<Timestamp, Integer> entryObj : objData.entrySet()) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("time", timeFormatter.format(entryObj.getKey().toLocalDateTime()));
            jsonObj.put("tps", entryObj.getValue());
            jsonResult.add(jsonObj);
        }
        return jsonResult;
    }

}
