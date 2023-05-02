/*
 * package org.renaultleat.network;
 * 
 * import java.io.BufferedReader;
 * import java.io.File;
 * import java.io.FileInputStream;
 * import java.io.FileNotFoundException;
 * import java.io.FileReader;
 * import java.io.IOException;
 * import java.net.Socket;
 * import java.net.URISyntaxException;
 * import java.util.ArrayList;
 * import java.util.List;
 * import java.util.Properties;
 * import java.util.concurrent.CountDownLatch;
 * import java.util.concurrent.ExecutorService;
 * import java.util.concurrent.Executors;
 * 
 * import org.renaultleat.chain.Block;
 * import org.renaultleat.chain.BlockPool;
 * import org.renaultleat.chain.Blockchain;
 * import org.renaultleat.chain.TransactionPool;
 * import org.renaultleat.consensus.Message;
 * import org.renaultleat.consensus.PBFTMessageHandler;
 * import org.renaultleat.consensus.PBFTMessagePool;
 * import org.renaultleat.crypto.CryptoUtil;
 * import org.renaultleat.node.Transaction;
 * import org.renaultleat.node.Validator;
 * import org.renaultleat.node.Wallet;
 * import org.renaultleat.properties.NodeProperty;
 * import org.json.JSONObject;
 * 
 * public class P2PServer {
 * public NodeCommunicator nodeCommunicator;
 * public Blockchain blockchain;
 * public TransactionPool transactionPool;
 * public BlockPool blockPool;
 * public Wallet wallet;
 * public PBFTMessagePool pbftMessagePool;
 * public Validator validator;
 * public String currentuser;
 * public QueueResource queueResource;
 * 
 * public void connect() throws IOException, InterruptedException {
 * // Get index for current Node
 * int index = Integer.valueOf(Wallet.getNodeproperty());
 * int total = NodeProperty.totalnodes;
 * String ip = NodeProperty.ip;
 * String users[] = NodeProperty.users.split(",");
 * String ports[] = NodeProperty.ports.split(",");
 * String currentuser = users[index];
 * this.currentuser = currentuser;
 * String currentport = ports[index];
 * this.nodeCommunicator = new NodeCommunicator(currentport);
 * 
 * nodeCommunicator.start();
 * // System.out.println("send c to initiate coomunication with peers");
 * }
 * 
 * public void connectToPeers()
 * throws FileNotFoundException, IOException {
 * // Form peers List
 * List<String> peerlist = getPeerList();
 * 
 * new P2PServer(this.blockchain, this.transactionPool, this.wallet,
 * this.pbftMessagePool, this.validator,
 * this.blockPool, this.nodeCommunicator, this.queueResource)
 * .listenToPeers(this.currentuser, this.nodeCommunicator, peerlist);
 * }
 * 
 * // Use when deployed in Cloud
 * public List<String> getPeerList() {
 * 
 * int index = Integer.valueOf(Wallet.getNodeproperty());
 * int total = NodeProperty.totalnodes;
 * String networkType = NodeProperty.getnodeNetwork();
 * String ip = NodeProperty.ip;
 * String users[] = NodeProperty.users.split(",");
 * String ports[] = NodeProperty.ports.split(",");
 * String currentuser = users[index];
 * String currentport = ports[index];
 * String basefile = "topology/" + networkType + "/" + String.valueOf(total) +
 * "/" + String.valueOf(index)
 * + ".csv";
 * File file;
 * 
 * List<String> peerlist = new ArrayList<String>();
 * if (networkType.equals("mesh")) {
 * for (int i = 0; i < total; i++) {
 * if (i != index) {
 * peerlist.add(ip + ":" + ports[i]);
 * }
 * 
 * }
 * } else {
 * try {
 * file = CryptoUtil.getFileFromResource(basefile);
 * BufferedReader br = new BufferedReader(new FileReader(file));
 * {
 * String line;
 * while ((line = br.readLine()) != null) {
 * String[] values = line.split(",");
 * System.out.println("valueport" + values[0] + ":" + values[1] + ":" +
 * values[2] + ":");
 * peerlist.add(ip + ":" + ports[Integer.parseInt(values[1])]);
 * }
 * br.close();
 * }
 * } catch (Exception e) {
 * // TODO Auto-generated catch block
 * e.printStackTrace();
 * 
 * }
 * }
 * return peerlist;
 * }
 * 
 * public void listenToPeers(String user, NodeCommunicator nodeCommunicator,
 * List<String> peers)
 * throws IOException {
 * for (int i = 0; i < peers.size(); i++) {
 * String[] address = peers.get(i).split(":");
 * Socket socket = null;
 * try {
 * socket = new Socket(address[0], Integer.valueOf(address[1]));
 * new P2PHandler(socket, this.blockchain, this.transactionPool, this.wallet,
 * this.pbftMessagePool,
 * this.validator,
 * this, this.nodeCommunicator, this.currentuser, this.blockPool,
 * this.queueResource).start();
 * } catch (Exception e) {
 * System.out.println("Connection exception" + e);
 * if (socket != null) {
 * socket.close();
 * 
 * } else {
 * System.out.println("invalid input");
 * }
 * }
 * }
 * }
 * 
 * public void communicate(String message)
 * throws IOException {
 * // Sender Logic
 * JSONObject jsonObject = new JSONObject();
 * jsonObject.put("username", this.wallet.getNodeId());
 * jsonObject.put("message", message);
 * nodeCommunicator.sendMessage(jsonObject.toString());
 * }
 * 
 * public void startSimulationBroadcast(int no_of_transactions, int
 * no_of_threads)
 * throws IOException {
 * 
 * int numberOfThreads = no_of_threads;
 * ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
 * CountDownLatch latch = new CountDownLatch(numberOfThreads);
 * for (int i = 0; i < numberOfThreads; i++) {
 * service.execute(() -> {
 * try {
 * this.nodeCommunicator.broadCastNewTransaction(this.currentuser, this.wallet,
 * no_of_transactions);
 * } catch (IOException e) {
 * // TODO Auto-generated catch block
 * e.printStackTrace();
 * }
 * latch.countDown();
 * });
 * }
 * try {
 * latch.await();
 * } catch (InterruptedException e) {
 * // TODO Auto-generated catch block
 * e.printStackTrace();
 * }
 * }
 * 
 * public void startSimulationBroadcastSingle(int no_of_transactions)
 * throws IOException {
 * // Sender Logic
 * for (int i = 0; i < no_of_transactions; i++) {
 * Transaction inputTransaction = new Transaction("Simulation Test",
 * this.wallet);
 * this.nodeCommunicator.broadCastTransaction(this.currentuser,
 * "Simulation Test", inputTransaction);
 * }
 * }
 * 
 * public P2PServer(Blockchain blockChain, TransactionPool transactionPool,
 * Wallet wallet,
 * PBFTMessagePool pbftMessagePool, Validator validator, BlockPool blockPool,
 * QueueResource queueResource) {
 * this.blockchain = blockChain;
 * this.transactionPool = transactionPool;
 * this.wallet = wallet;
 * this.pbftMessagePool = pbftMessagePool;
 * this.validator = validator;
 * this.blockPool = blockPool;
 * this.queueResource = queueResource;
 * }
 * 
 * public P2PServer(Blockchain blockChain, TransactionPool transactionPool,
 * Wallet wallet,
 * PBFTMessagePool pbftMessagePool, Validator validator, BlockPool blockPool,
 * NodeCommunicator nodeCommunicator, QueueResource queueResource) {
 * this.blockchain = blockChain;
 * this.transactionPool = transactionPool;
 * this.wallet = wallet;
 * this.pbftMessagePool = pbftMessagePool;
 * this.validator = validator;
 * this.blockPool = blockPool;
 * this.nodeCommunicator = nodeCommunicator;
 * this.queueResource = queueResource;
 * }
 * 
 * }
 */