#!/bin/bash
cd "$(dirname "$0")"
#################################################################################################################
#                       $1           $2          $3         $4         $5           $6         $7             
# ./launchPodTest.sh  totalnodes  nodenetwork blocksize  nodelatency nodebehavior fireboolean notxs
#################################################################################################################
#                    $1  $2  $3  $4  $5   $6   $7
# ./launchPodTest.sh 4  mesh 100  0  0  true 5000
#################################################################################################################
#################################################################################################################
#                       $1         $2        $3           $4         $5           $6          $7         $8  
# ./podScripttest.sh nodeindex totalnodes  nodenetwork blocksize  nodelatency nodebehavior fireboolean notxs
#################################################################################################################
NBNODES=$1

#Start from Index 0

echo "No of Nodes"
echo $NBNODES

value=$((NBNODES - 1))
iter=$value

for (( i = 0; i <=$iter; ++i )); 
#for i in 0..$iter;
do
   bash podScriptTest.sh "$i" $1 $2 $3 $4 $5 $6 $7 > $i.out 2>&1 &
   #nohup bash podScriptTest.sh "$i" $1 $2 $3 $4 $5 $6 $7 > $i.out 2>&1 & disown
   #nohup bash podScriptTest.sh "$i" $1 $2 $3 $4 $5 $6 $7 > $i.out &
   echo "FIRED" "$i"
done