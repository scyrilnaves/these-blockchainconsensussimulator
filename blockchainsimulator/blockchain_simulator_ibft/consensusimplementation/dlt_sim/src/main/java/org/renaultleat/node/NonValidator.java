package org.renaultleat.node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.renaultleat.crypto.CryptoUtil;
import org.renaultleat.properties.NodeProperty;

public class NonValidator {
    List<String> allNodePublicKeys = new ArrayList<String>();

    public NonValidator() {
        try {
            this.allNodePublicKeys.addAll(generateAddresses());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public List<String> generateAddresses() throws FileNotFoundException, IOException {
        List<String> nodePublicKeys = new ArrayList<String>();
        int totalkeys = Integer.valueOf(NodeProperty.totalkeys);
        for (int i = 0; i < totalkeys; i++) {
            try {
                nodePublicKeys.add(CryptoUtil.getPublicKeyString(i));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return nodePublicKeys;
    }

    public boolean isValidNonValidator(String nodePublicKey) {
        return allNodePublicKeys.contains(nodePublicKey);

    }

}
