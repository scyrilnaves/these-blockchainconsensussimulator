package org.renaultleat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.renaultleat.node.Transaction;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class NodeCommunicatorSec extends Thread {

    private ServerSocket serverSocket;
    private Set<NodeCommunicatorThreadSec> nodeCommunicatorThreads = new HashSet<NodeCommunicatorThreadSec>();

    public NodeCommunicatorSec(String port) throws NumberFormatException, IOException {
        serverSocket = new ServerSocket(Integer.valueOf(port) + 10, 500);
    }

    public void run() {
        try {
            while (true) {
                if (nodeCommunicatorThreads.size() < (NodeProperty.gettotalNodes() - 1)) {
                    NodeCommunicatorThreadSec nodeCommunicatorThread = new NodeCommunicatorThreadSec(
                            serverSocket.accept(),
                            this);
                    nodeCommunicatorThreads.add(nodeCommunicatorThread);
                    nodeCommunicatorThread.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadCastTransaction(String currentuser, String message, Transaction inTransaction)
            throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", currentuser);
        jsonObject.put("message", message);
        jsonObject.put("type", "TRANSACTION");
        Gson gson = new Gson();
        String datajson = gson.toJson(inTransaction);
        jsonObject.put("data", datajson);
        sendMessage(jsonObject.toString());
    }

    public void broadCastNewTransaction(String currentuser, Wallet wallet, int no_of_transactions)
            throws IOException {
        for (int i = 0; i < no_of_transactions; i++) {
            Transaction inputTransaction = new Transaction("Simulation Test", wallet);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", currentuser);
            jsonObject.put("message", "Simulation Test");
            jsonObject.put("type", "TRANSACTION");
            Gson gson = new Gson();
            String datajson = gson.toJson(inputTransaction);
            jsonObject.put("data", datajson);
            sendMessage(jsonObject.toString());
        }
    }

    public void sendMessage(String message) {
        try {
            TimeUnit.MILLISECONDS.sleep(NodeProperty.latency);
            if (NodeProperty.getnodeBehavior() == 1) {
                TimeUnit.MILLISECONDS.sleep(NodeProperty.latency + 60000);
            }
            nodeCommunicatorThreads.forEach(t -> t.getPrintWriter().println("YYY" + message + "ZZZ"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<NodeCommunicatorThreadSec> getnodeCommunicatorThread() {
        return nodeCommunicatorThreads;
    }
}
