package org.renaultleat.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NodeCommunicatorThreadPenta extends Thread {
    private NodeCommunicatorPenta nodeCommunicator;
    private Socket socket;
    private PrintWriter printWriter;

    public NodeCommunicatorThreadPenta(Socket socket, NodeCommunicatorPenta nodeCommunicator) {
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
