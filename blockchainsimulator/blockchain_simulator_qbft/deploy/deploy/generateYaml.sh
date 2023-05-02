#!/bin/bash
# DONT FORGET TO ADD INGRESS ON RANCHER
#http://dltsim-dash-last.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp
#http://dltsim-dash-mid.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp
#http://dltsim-dash.unice.cust.tasfrance.com/distributed_ledger_simulator_framework_consensus_testbench/result.jsp
#ADD in /etc/hosts
#185.52.32.4 rancher.unice.cust.tasfrance.com
#185.52.32.4 grafana.unice.cust.tasfrance.com
#185.52.32.4 substrate-ws.unice.cust.tasfrance.com
#185.52.32.4 dltsim-dash.unice.cust.tasfrance.com
#185.52.32.4 dltsim-dash-mid.unice.cust.tasfrance.com
#185.52.32.4 dltsim-dash-last.unice.cust.tasfrance.com

my_dir="$(dirname "$0")"

NBNODES=$1

cat <<EOF
apiVersion: v1
kind: List

items:

EOF

for ((i = 0; i < $NBNODES; i++)); do
  echo ""
  echo "# --------------------------=== POD DEPLOYMENT $i ===--------------------------"

  ######################

  cat <<EOF
- apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: dltsim-$i
    namespace: dltsim-net
  spec:
    replicas: 1
    selector:
      matchLabels:
        name: dltsim-$i
    template:
      metadata:
        labels:
          name: dltsim-$i
          serviceSelector: dltsim-$i-node
      spec:
        securityContext:
          fsGroup: 101
        containers:
          - name: dltsim-$i-node
            image: cyrilthese/dltsim-qbft:latest
            env:
              - name: JAVA_ARGS
                value: "-Xmx6000M -Xms5096M"
            resources:
              requests:
                memory: "8Gi"
                cpu: "3"
                ephemeral-storage: "2500Mi"
              limits:
                memory: "8Gi"
                cpu: "3"
                ephemeral-storage: "3Gi"
            ports:
              - name: http
                containerPort: 8080
              - name: websocket
                containerPort: 7080
            volumeMounts:
              - name: dltsim-data-$i
                mountPath: /datas/dltsim-$i
             
        volumes:
          - name: dltsim-data-$i
            persistentVolumeClaim:
              claimName: dltsim-data-$i
EOF

  # define service for node
  cat <<EOF

#---------------------------------=NODES SERVICES $i=---------------------------------------
- apiVersion: v1
  kind: Service
  metadata:
    name: dltsim-$i
    namespace: dltsim-net
  spec:
    type: ClusterIP
    selector:
      name: dltsim-$i-node
    ports:
      - name: "8080"
        protocol: TCP
        port: 8080
        targetPort: 8080
      - name: "7080"
        protocol: TCP
        port: 7080
        targetPort: 7080
EOF

  # define volume for node
  cat <<EOF
#---------------------------------=NODES PERSISTANT VOLUME $i=---------------------------------------
- apiVersion: v1
  kind: PersistentVolume
  metadata:
    name: dltsim-$i
    labels:
      type: local
  spec:
    storageClassName: manual
    capacity:
      storage: 50Gi
    accessModes:
      - ReadWriteOnce
    persistentVolumeReclaimPolicy: Recycle
    hostPath:
      path: "/datas/dltsim-$i"
EOF

  # define volume claim for node
  cat <<EOF
#--------------------------=PERSISTENT VOLUME CLAIM $i=------------------------------

- apiVersion: v1
  kind: PersistentVolumeClaim
  metadata:
    labels:
      app: dltsim-data
    name: dltsim-data-$i
    namespace: dltsim-net
  spec:
    storageClassName: manual
    accessModes:
    - ReadWriteOnce
    resources:
     requests:
        storage: 45Gi
EOF

done
# end for loop

cat <<EOF

#--------------------------=HTTP SERVICE FOR FIRST , MID AND LAST NODE=--------------------------------

- apiVersion: v1
  kind: Service
  metadata:
    name: dltsim-http-service
    namespace: dltsim-net
  spec:
    type: ClusterIP
    selector:
      serviceSelector: dltsim-1-node
    ports:
      - name: "8080"
        protocol: TCP
        port: 8080
        targetPort: 8080

- apiVersion: v1
  kind: Service
  metadata:
    name: dltsim-http-mid-service
    namespace: dltsim-net
  spec:
    type: ClusterIP
    selector:
      serviceSelector: dltsim-$(expr $NBNODES - 3)-node
    ports:
      - name: "8080"
        protocol: TCP
        port: 8080
        targetPort: 8080

- apiVersion: v1
  kind: Service
  metadata:
    name: dltsim-http-last-service
    namespace: dltsim-net
  spec:
    type: ClusterIP
    selector:
      serviceSelector: dltsim-$(expr $NBNODES - 1)-node
    ports:
      - name: "8080"
        protocol: TCP
        port: 8080
        targetPort: 8080
EOF

cat <<EOF
####################################### BENCHMARK MACHINE #########################

- apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: ubuntu
    namespace: dltsim-net
  spec:
    replicas: 1
    selector:
        matchLabels:
          name: ubuntu-deployment
    template:
      metadata:
        name: ubuntu-deployment
        labels:
          app: ubuntu
          tier: backend
          name: ubuntu-deployment
      spec:
        containers:
        - name: ubuntu
          image: cyrilthese/masternode:latest
          command:
            - "sleep"
            - "604800"
          resources:
            limits:
              cpu: "8"
              memory: "8Gi"
            requests:
              cpu: "8"
              memory: "8Gi"
          imagePullPolicy: IfNotPresent
        restartPolicy: Always
EOF
