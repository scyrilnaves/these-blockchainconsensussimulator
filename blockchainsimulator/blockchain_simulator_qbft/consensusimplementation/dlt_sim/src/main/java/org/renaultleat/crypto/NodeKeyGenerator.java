package org.renaultleat.crypto;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;

import org.renaultleat.properties.NodeProperty;

public class NodeKeyGenerator {

    public static void generateRSAKeyPair(int totalkeys)
            throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        KeyPairGenerator keypairGenerator = KeyPairGenerator.getInstance("RSA");
        String pubkeyfile = "/home/renault/Documents/code/qbft/qbftdltsimulatorfinal/dlt_sim/src/main/resources/nodekeys/public";
        String privatekeyfile = "/home/renault/Documents/code/qbft/qbftdltsimulatorfinal/dlt_sim/src/main/resources/nodekeys/private";

        for (int i = 0; i < totalkeys; i++) {
            KeyPair keyPair = keypairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Store Public Key
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
            FileOutputStream fos_pub = new FileOutputStream(pubkeyfile + "/node" + i);
            fos_pub.write(x509EncodedKeySpec.getEncoded());
            fos_pub.close();

            // Store Private Key
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            FileOutputStream fos_priv = new FileOutputStream(privatekeyfile + "/node" + i);
            fos_priv.write(pkcs8EncodedKeySpec.getEncoded());
            fos_priv.close();
        }
    }

    public static void main(String args[]) {
        try {
            int totalkeys = Integer.valueOf(100);
            generateRSAKeyPair(totalkeys);
        } catch (NoSuchAlgorithmException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
