package org.renaultleat.network;

import java.io.IOException;

import org.renaultleat.node.Transaction;
import org.renaultleat.node.Wallet;
import org.renaultleat.properties.NodeProperty;

public class TransactionBroadcasterTer extends Thread {

    public NodeCommunicatorTer nodeCommunicator;
    public Wallet wallet;
    public String currentuser;
    int no_of_transactions;

    public void setNoofTransactions(int inTransaction) {
        this.no_of_transactions = inTransaction;
    }

    @Override
    public void run() {

        while (true) {
            for (int i = 0; i < no_of_transactions; i++) {
                Transaction inputTransaction = new Transaction("Simulation Test", this.wallet);
                try {

                    this.nodeCommunicator.broadCastTransaction(this.currentuser,
                            "Simulation Test", inputTransaction);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public TransactionBroadcasterTer(NodeCommunicatorTer nodeCommunicator,
            Wallet wallet, int noTransactions) {
        int index = Integer.valueOf(Wallet.getNodeproperty());
        String currentuser = NodeProperty.user + String.valueOf(index);
        this.currentuser = currentuser;
        this.nodeCommunicator = nodeCommunicator;
        this.wallet = wallet;
        this.no_of_transactions = noTransactions;
    }

}
