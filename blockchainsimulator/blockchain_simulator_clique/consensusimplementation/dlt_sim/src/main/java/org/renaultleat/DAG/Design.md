https://medium.com/coinmonks/implementing-merkle-tree-and-patricia-trie-b8badd6d9591 

https://www.baeldung.com/jgrapht 

https://jgrapht.org/javadoc-1.4.0/org/jgrapht/graph/DirectedAcyclicGraph.html 

https://stackoverflow.com/questions/24384223/custom-edge-with-jgrapht

https://jgrapht.org/guide/HelloJGraphT

https://medium.com/pandoraboxchain/a-very-brief-engineering-introduction-into-consensus-algorithms-and-distributed-ledger-data-e4d7449278b7

Idea:
======
Leaderless Query for Epoch confirmation
LeaderBased (Randomised or Randomness Beacon Service(https://drand.love/about/#why-decentralized-randomness-is-important)) State Confirmation 

Algorithm:
==========
Initialise Weighted Directed Graph State:
-----------------------------------------
1) Create Epoch N --> add to Graph 
2) Create Transaction and add weighted edge 0 to Epoch N
3) Receive Transaction and add weighted edge 0 to Epoch N
4) Continue until certain Node Pool Size
5) Sort the Transaction by its Timestamp 
6) Calculate the Merkle Tree by the sorted transactions and State Proof: Hash
7) Each Participant broadcast its calculated proof in order to verify the current state
8) Append state proof as weighted edge 1
9) Connect the previous Epoch N to N+1 as weighted edge 2
10) Then proceed to Epoch N+1 


