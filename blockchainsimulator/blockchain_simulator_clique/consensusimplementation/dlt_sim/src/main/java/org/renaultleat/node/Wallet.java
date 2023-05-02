package org.renaultleat.node;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.renaultleat.crypto.CryptoUtil;

public class Wallet {

    public static String nodeproperty;

    public String nodeid;

    public static int nodeindex;

    KeyPair keypair;

    String publicKey;

    public static void setnodeproperty(String input) {
        // Set only once
        if (nodeproperty == null || nodeproperty == "") {
            nodeproperty = input;
        }

    }

    public static String getNodeproperty() {
        return nodeproperty;

    }

    public String getNodeId() {
        return this.nodeid;

    }

    public static int getNodeIndex() {
        return nodeindex;

    }

    public KeyPair getKeyPair() {
        return this.keypair;

    }

    public String getPublicKey() {
        return this.publicKey;

    }

    public String signData(String message) {
        String signature = "";
        try {
            signature = CryptoUtil.getSignature(getNodeIndex(), CryptoUtil.getHash(message));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return signature;
    }

    public Transaction createTransaction(String message) {
        return new Transaction(message, this);

    }

    public Wallet() {
    }

    public void initalise(int index) {
        try {
            this.keypair = CryptoUtil.getKeyPair(index);
            this.publicKey = CryptoUtil.getPublicKeyString(index);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        nodeindex = index;
        this.nodeid = "node" + String.valueOf(index);
    }

}
