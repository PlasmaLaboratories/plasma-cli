#!/bin/bash

#### Requirements  Coursier, Plasma CLI, Bash.

# README
# > cd plasma-cli/examples/useCase_bn1877
# > bash ./useCase_bn1877_BroadcastPolicyAndSeries.bash

# rm and create tmp files the outputs from the wallet
rm -rf ./tmp
rm -rf ./bin # un comment
mkdir ./tmp
mkdir ./bin

# # 0 Set up the plasma-cli

curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > ./bin/cs
chmod +x ./bin/cs
./bin/cs setup --yes
echo "#!/bin/bash" >> ./bin/plasma-cli
echo 'cs launch --java-opt -Dslf4j.internal.verbosity=WARN -r https://s01.oss.sonatype.org/content/repositories/releases org.plasmalabs:plasma-cli_2.13:0.1.3 -- $@' >> ./bin/plasma-cli
sudo chmod +x ./bin/plasma-cli

#### END Requirements Coursier, Plasma CLI

# stop running container(s)
docker ps -q --filter "name=node" | xargs -r docker stop
# remove existing container(s)
docker ps -aq --filter "name=node" | xargs -r docker rm

# 1. Run a local plasma-node
containerId="$(docker run --rm -d --name node -p 9085:9085 -p 9084:9084 ghcr.io/plasmalaboratories/plasma-node:0.1.4 -- )"
export containerId
# If you want inspect containerId
printf "CONTAINER_ID: $containerId \n"

# inspect container ip
containerIp="$(docker network inspect bridge | jq  ".[0].Containers.\"$containerId\".IPv4Address" | sed  's:"::g' | sed -n 's:\(.*\)/.*:\1:p')"
export containerIp
# If you want inspect containerIP
printf "CONTAINER_IP: $containerIp"


# constants
password=password
basePath=./tmp
walletDb=$basePath/wallet.db
mnemonicFile=$basePath/mnemonic.txt
keyfile=$basePath/keyfile.json
host=localhost
port=9084
plasmaWalletTx=lvlsTransferTx.pbuf
plasmaWalletTxProved=lvlsTransferTxProved.pbuf

# 2. Create a wallet
printf "\n<<Init Wallet: "
./bin/plasma-cli wallet init -n private -w $password --newwalletdb $walletDb --mnemonicfile $mnemonicFile  -o $keyfile

# Get the wallet address
walletAddress_1="$(./bin/plasma-cli wallet current-address --walletdb $walletDb --from-fellowship self --from-template default)"
printf "Wallet address: %s\n" "$walletAddress_1"

# wait for the balance
printf "<<WalletBalance\n"
((count = 0))
until ./bin/plasma-cli wallet balance --from-fellowship self --from-template default --walletdb $walletDb --host $host --port $port || (( count++ > 1 ));  do sleep 5; done

# 4. Create a transaction to transfer tokens to wallet
printf "<<Create a transaction"
./bin/plasma-cli simple-transaction create --from-fellowship nofellowship --from-template genesis --from-interaction 1 --change-fellowship nofellowship --change-template genesis --change-interaction 1 -t "$walletAddress_1" -w $password -o $basePath/$plasmaWalletTx -n private --amount 1000 --keyfile $keyfile --walletdb $walletDb --fee 10 --transfer-token lvl --host $host --port $port

# 5. Prove the transaction is semantically and syntactically correct
printf "<<Prove transaction"
./bin/plasma-cli tx prove -w $password --keyfile $keyfile -i $basePath/$plasmaWalletTx -o $basePath/$plasmaWalletTxProved --walletdb $walletDb

# 6. Broadcast the transaction
printf "<<Broadcast transaction"
a_utxo_100_lvls="$(./bin/plasma-cli tx broadcast -i $basePath/$plasmaWalletTxProved --host $host --port $port)"
echo "utxo_a: $a_utxo_100_lvls"


# 7 8. Wait for the transaction to be included in a block, and check the balance
sync="$(./bin/plasma-cli wallet sync --walletdb $walletDb --keyfile $keyfile -w $password --fellowship-name self --template-name default --host $host --port $port)"
echo "$sync"
printf "<<WalletBalance\n"
((count = 0))
until ./bin/plasma-cli wallet balance --from-fellowship self --from-template default --walletdb $walletDb --host $host --port $port || (( count++ > 1 ));  do sleep 5; done

read -r -d '' groupPolicy <<-EOF
label: MyGroupPolicy
registrationUtxo: $a_utxo_100_lvls#0
EOF
echo "$groupPolicy" >> $basePath/groupPolicy.yaml

# 9  Create policy transaction
printf "\n<<Create Group policy:"
./bin/plasma-cli simple-minting create --from-fellowship self --from-template default -n private -o $basePath/tx_group.pbuf -i $basePath/groupPolicy.yaml --mint-amount 1 --mint-token group --fee 10 --walletdb $walletDb --keyfile $keyfile -w $password --host $host --port $port

# 10 prove the group policy
printf "<<Prove a transaction Group:"
./bin/plasma-cli tx prove -w $password --keyfile $keyfile -i $basePath/tx_group.pbuf -o $basePath/tx_group_Proved.pbuf --walletdb $walletDb

# 11 Broadcast a group policy
printf "<<Broadcast a transaction Group:"
utxo_group="$(./bin/plasma-cli tx broadcast -i $basePath/tx_group_Proved.pbuf --host $host --port $port)"
echo "utxo_group: $utxo_group"

# Wait for the transaction to be included in a block, and check the balance
sync="$(./bin/plasma-cli wallet sync --walletdb $walletDb --keyfile $keyfile -w $password --fellowship-name self --template-name default --host $host --port $port)"
echo "$sync"
printf "<<WalletBalance\n"
((count = 0))
until ./bin/plasma-cli wallet balance --from-fellowship self --from-template default --walletdb $walletDb --host $host --port $port || (( count++ > 1 ));  do sleep 5; done

printf "End\n"

# stop remove running container(s), clean folders
rm -rf ./tmp
rm -rf ./bin  # commented to no download the cli each time
docker ps -q --filter "name=node" | xargs -r docker stop
docker ps -aq --filter "name=node" | xargs -r docker rm

: <<'EXPECTED_RESULTS'

Group(227d2e09c86a0077b83dc61b8c3fab010c8e0c2a84e9933b0f483a86423879d5): 1
LVL: 990  <<< 1000 -10

EXPECTED_RESULTS

