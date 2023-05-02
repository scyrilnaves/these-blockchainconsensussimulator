package org.renaultleat.api;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

// DataStructure for storing collatting results
public class Simulator_result {

    // Time Series, Timestamp, Confirmed Tx Count
    public volatile SortedMap<Timestamp, Integer> inputTpsStorage = new TreeMap<Timestamp, Integer>();

    // Time Series, Timestamp, Confirmed Block Count (extrapolate with block size to
    // get tx finalised)
    public volatile Map<Timestamp, Integer> finalisedTpsStorage = new LinkedHashMap<Timestamp, Integer>();

    // Call the counter for Prepare in consensus Message Handler after updated block
    public volatile Map<Timestamp, Integer> prepareCounter = new LinkedHashMap<>();

    // Call the counter for Commit in consensus Message Handler after updated block
    public volatile Map<Timestamp, Integer> commitCounter = new LinkedHashMap<>();

    // Call the counter for RoundChange in consensus Message Handler after updated
    // block
    public volatile Map<Timestamp, Integer> roundChangeCounter = new LinkedHashMap<>();

}
