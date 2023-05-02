package org.renaultleat.network;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.random.RandomDataGenerator;

public class NetworkTopologyGenerator {

    public static void generateRingLatticeTopologyCSV(int totalNodeCount, int meanDegree) {

        // MeanDegreeHalf
        int meanDegreeHalf = meanDegree / 2;
        // [TotalNodecount | [ Node index source, [Node Index Destination,
        // ConnectionBoolean(yes or no)]]]
        Map<Integer, Map<Integer, Map<Integer, Boolean>>> totalConnectionMap = new HashMap<Integer, Map<Integer, Map<Integer, Boolean>>>();
        for (int i = 2; i <= totalNodeCount; i++) {
            Map<Integer, Map<Integer, Boolean>> nodeConnectionMap = new HashMap<Integer, Map<Integer, Boolean>>();
            for (int j = 0; j < i; j++) {
                Map<Integer, Boolean> peerConnectionMap = new HashMap<Integer, Boolean>();
                // Peers
                for (int k = j + 1; k < j + meanDegreeHalf; k++) {
                    peerConnectionMap.put((k % i), true);
                }
                nodeConnectionMap.put(j, peerConnectionMap);
            }
            totalConnectionMap.put(i, nodeConnectionMap);
        }

        try {
            writeToCSV(totalConnectionMap, "ringLattice");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // Minimum 3 nodes necessary !!!!!
    public static void generateWattsStrogatzTopologyCSV(int totalNodeCount, int meanDegree) {

        float randomness = 1;
        // MeanDegreeHalf
        int meanDegreeHalf = meanDegree / 2;
        // [TotalNodecount | [ Node index source, [Node Index Destination,
        // ConnectionBoolean(yes or no)]]]

        // Base Ring Lattice
        Map<Integer, Map<Integer, Map<Integer, Boolean>>> ringLatticeTotalConnectionMap = new HashMap<Integer, Map<Integer, Map<Integer, Boolean>>>();
        for (int i = 3; i <= totalNodeCount; i++) {
            Map<Integer, Map<Integer, Boolean>> nodeConnectionMap = new HashMap<Integer, Map<Integer, Boolean>>();
            for (int j = 0; j < i; j++) {
                Map<Integer, Boolean> peerConnectionMap = new HashMap<Integer, Boolean>();
                // Peers
                for (int k = j + 1; k < j + meanDegreeHalf; k++) {
                    peerConnectionMap.put((k % i), true);
                }
                nodeConnectionMap.put(j, peerConnectionMap);
            }
            ringLatticeTotalConnectionMap.put(i, nodeConnectionMap);
        }
        //////////////////////////////////////////////////////////
        // Watts Stroagtz
        Map<Integer, Map<Integer, Map<Integer, Boolean>>> wSTotalConnectionMap = new HashMap<Integer, Map<Integer, Map<Integer, Boolean>>>();
        for (Integer currentTotalNodeCount : ringLatticeTotalConnectionMap.keySet()) {
            // For each total Node count

            Map<Integer, Map<Integer, Boolean>> nodeConnectionMap = ringLatticeTotalConnectionMap
                    .get(currentTotalNodeCount);
            Map<Integer, Map<Integer, Boolean>> wsnodeConnectionMap = new HashMap<>();
            for (Integer currentSourceNodeIndex : nodeConnectionMap.keySet()) {
                Map<Integer, Boolean> destinationconnectionMap = nodeConnectionMap.get(currentSourceNodeIndex);
                Map<Integer, Boolean> wsDestinationconnectionMap = new HashMap<>();
                for (Integer destinationNode : destinationconnectionMap.keySet()) {
                    if (new Random().nextFloat() < randomness) {
                        int leftLimit = 0;
                        int rightLimit = currentTotalNodeCount - 1;
                        // Generate a random peer with the limit of total nodecount -1
                        Integer newPeer = new RandomDataGenerator().nextInt(leftLimit, rightLimit);
                        // Check if the new peer is not:
                        // 1) old peer 2) already the peer exists 3) or the new peer has a connection to
                        // the source index (reverse direction)
                        while (newPeer == destinationNode || (destinationconnectionMap.containsKey(newPeer)
                                && destinationconnectionMap.get(newPeer)) || (nodeConnectionMap.containsKey(newPeer)
                                        && nodeConnectionMap.get(newPeer).containsKey(destinationNode)
                                        && nodeConnectionMap.get(newPeer).get(destinationNode))) {
                            newPeer = new RandomDataGenerator().nextInt(leftLimit, rightLimit);
                        }
                        wsDestinationconnectionMap.put(newPeer, true);
                    }
                }
                wsnodeConnectionMap.put(currentSourceNodeIndex, wsDestinationconnectionMap);
            }
            wSTotalConnectionMap.put(currentTotalNodeCount, wsnodeConnectionMap);
        }

        try {
            writeToCSV(wSTotalConnectionMap, "wattsStrogatz");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void writeToCSV(Map<Integer, Map<Integer, Map<Integer, Boolean>>> totalConnectionMap, String topology)
            throws IOException {
        String baseTopologyfile = "/home/renault/Documents/code/dltsimulator/dlt_sim/src/main/resources/topology/"
                + topology
                + "/";
        for (Integer currentTotalNodeCount : totalConnectionMap.keySet()) {
            // Make file for each total Node count

            Map<Integer, Map<Integer, Boolean>> nodeConnectionMap = totalConnectionMap.get(currentTotalNodeCount);
            File baseDirectory = new File(baseTopologyfile + String.valueOf(currentTotalNodeCount));
            if (!baseDirectory.exists()) {
                baseDirectory.mkdirs();
            }
            for (Integer currentSourceNodeIndex : nodeConnectionMap.keySet()) {

                File file = new File(baseTopologyfile + String.valueOf(currentTotalNodeCount) + "/"
                        + String.valueOf(currentSourceNodeIndex) + ".csv");
                file.createNewFile();
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                for (Map.Entry<Integer, Boolean> connectionValue : nodeConnectionMap.get(currentSourceNodeIndex)
                        .entrySet()) {
                    bufferedWriter.write(
                            currentSourceNodeIndex + "," + connectionValue.getKey() + "," + connectionValue.getValue());
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
                bufferedWriter.close();
            }

        }
    }

    public static void main(String args[]) {
        // Depends on our test
        int totalNodeCount = 30;
        // For Ring Lattice Start from 2 Node Configuration until total Node
        // For Watts Stroagtz Start from 3 Node Configuration until Total Node
        // Should be even
        int meanDegree = 4;
        //////////////////
        generateRingLatticeTopologyCSV(totalNodeCount, meanDegree);
        generateWattsStrogatzTopologyCSV(totalNodeCount, meanDegree);
    }

}
