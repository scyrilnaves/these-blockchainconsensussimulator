package org.renaultleat.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NodeCommunicatorThreadSix extends Thread {
    private NodeCommunicatorSix nodeCommunicator;
    private Socket socket;
    private PrintWriter printWriter;

    public NodeCommunicatorThreadSix(Socket socket, NodeCommunicatorSix nodeCommunicator) {
        this.nodeCommunicator = nodeCommunicator;
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                nodeCommunicator.sendMessage(bufferedReader.readLine());
            }
        } catch (Exception e) {
            nodeCommunicator.getnodeCommunicatorThread().remove(this);
        }
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

}
