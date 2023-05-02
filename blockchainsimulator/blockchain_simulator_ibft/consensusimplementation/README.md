# README #
// Mozilla uses port 80, so dont use Geckomain
Change server configuration to 9005, 9080 and 9443
https://www.baeldung.com/java-testing-multithreaded 
# ###########################
PLANT UML:
===============
https://marketplace.visualstudio.com/items?itemName=Mebrahtom.plantumlpreviewer

sudo apt install graphviz
# ###########################
# DOCKER PART
docker build -f dltdocker -t dltsim .

docker images

docker run -p 8080:8080 dltsim

docker login --username=cyrilthese

docker images

docker tag b82dfcaf5e96 cyrilthese/dltsim

docker push cyrilthese/dltsim
// Delete all images, containers
docker system prune -a
# DOCKER END
//RUN 
mvn clean install -DskipTests -Dhttps.protocols=TLSv1.2

# BUILD : mvn clean package -DskipTests -Dhttps.protocols=TLSv1.2

mvn exec:java -Dexec.mainClass="org.renaultleat.dev.SimulatorServiceDev" -Dexec.args="0"

mvn exec:java -Dexec.mainClass="org.renaultleat.dev.SimulatorServiceDev" -Dexec.args="1"

https://linuxhint.com/install_apache_tomcat_server_ubuntu/

Spring:

./mvnw spring-boot:run

./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8080

./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

mvn archetype:generate -DgroupId=com.renaultleat -DartifactId=distributed_ledger_simulator_framework_consensus_testbench -DarchetypeArtifactId=maven-archetype-webapp -DinteractiveMode=false -U

https://java2blog.com/restful-web-services-jaxrs-crud-example/


Run
======
Ref: https://stackoverflow.com/questions/1089285/maven-run-project

mvn exec:java -Dexec.mainClass="what to put here?" -Dexec.args="arg0 arg1 arg2"

mvn exec:java -Dexec.mainClass="com.renault.consensus_simulator.Node" -Dexec.args="0"

mvn exec:java -Dexec.mainClass="com.renault.consensus_simulator.Node" -Dexec.args="1"

=============================
Generate Topology
=============================
mvn exec:java -Dexec.mainClass="org.renaultleat.network.NetworkTopologyGenerator"

=======================
GENERATE NODE KEY
=======================
mvn exec:java -Dexec.mainClass="com.renault.consensus_simulator.NodeKeyGenerator"

Peer To Peer Module:
======================
Node
NodeThread
NodeReceiver
NodeReceiverSecondarys

CryptoModule:
===============
NodeKeyGenerator: To Generate Edwards Public and Private Key apriori
CryptoUtil: To get a Node's KeyPair, Public, Private, Signature, Hash.. etc

DASHBOARD
=================
http://localhost:8080/distributed_ledger_simulator_framework_consensus_testbench/result.jsp

Plan:
======
WattsStrogatz Model of connecting peers; virtual voting; 
Network Model
Latency, Payload
No Need Merkle or Patricia for Blockchain for DAG
Docker Deployment
Database not needed for now
D3.js (front-end) dashboard
Fix Block Validation | Verify Block signature | 

https://husqy.medium.com/a-new-consensus-the-tangle-multiverse-part-1-da4cb2a69772
Simulator --> Merkle --> Small World
=================================================
Good Link:
https://github.com/sleepokay/watts-strogatz
https://www.geeksforgeeks.org/small-world-model-using-python-networkx/
https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwiy0fSR__L1AhWuy4UKHYhABp04ChC3AnoECBkQAw&url=https%3A%2F%2Fwww.coursera.org%2Flecture%2Fnetworks-illustrated%2Fwatts-strogatz-model-part-a-zUnMU&usg=AOvVaw3KHk2tQW2jdy6wT0R2ZE4m - Coursera
PBFT / IBFT / QBFT
https://simgrid.org/doc/latest/Platform_examples.html --simulation tool

D3js integration for chart
https://medium.com/thinkspecial/play-time-with-d3js-springboot-and-mongodb-35bdf1d2faef
https://www.d3-graph-gallery.com/line
OK:
https://medium.com/@ecb2198/multiple-time-series-in-d3-5562b981236c
===================================================
For H2:
https://o7planning.org/11893/integrating-spring-boot-jpa-and-h2-database

# Read Me First
Wrapper problem:
====================
mvn -N io.takari:maven:wrapper 

# Development : UNCOMMENT
================================
 P2PServer.java == P2PServer-Dev.jav
 result.jsp     == result-dev.jsp

 Comment shortcut:
 Ctrl+/
 Uncomment: Same

