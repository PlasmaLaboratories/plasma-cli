#!/bin/bash

#### Requirements  Coursier, Plasma CLI, Bash.

# README
# > cd plasma-cli/examples/useCase_001_MintAsset
# > bash ./useCase_001_MintAsset.bash

# rm and create tmp files the outputs from the wallet
rm -rf ./tmp
rm -rf ./bin
mkdir ./tmp
mkdir ./bin

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

# run node tooling
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

printf "\n<<Init Wallet: "
./bin/plasma-cli wallet init -n private -w $password --newwalletdb $walletDb --mnemonicfile $mnemonicFile  -o $keyfile

# wallet address iteration 1
walletAddress_1="$(./bin/plasma-cli wallet current-address --walletdb $walletDb --from-fellowship self --from-template default)"
printf "Wallet address: %s\n" "$walletAddress_1"

printf "<<WalletBalance\n"
((count = 0))
until ./bin/plasma-cli wallet balance --from-fellowship self --from-template default --walletdb $walletDb --host $host --port $port || (( count++ > 1 ));  do sleep 5; done

printf "<<Create a transaction A, transfer 100 lvl from Genesis block with 10 fee:"
./bin/plasma-cli simple-transaction create --from-fellowship nofellowship --from-template genesis --from-interaction 1 --change-fellowship nofellowship --change-template genesis --change-interaction 1 -t "$walletAddress_1" -w $password -o $basePath/tx_A.pbuf -n private --amount 100 --keyfile $keyfile --walletdb $walletDb --fee 10 --transfer-token lvl --host $host --port $port

printf "<<Prove transaction A:"
./bin/plasma-cli tx prove -w $password --keyfile $keyfile -i $basePath/tx_A.pbuf -o $basePath/tx_A_Proved.pbuf --walletdb $walletDb

printf "<<Broadcast transaction A:"
a_utxo_100_lvls="$(./bin/plasma-cli tx broadcast -i $basePath/tx_A_Proved.pbuf --host $host --port $port)"
echo "utxo_a: $a_utxo_100_lvls"

printf "\n<<WalletBalance:"
sleep 5
./bin/plasma-cli wallet balance --from-fellowship self --from-template default --walletdb $walletDb --host $host --port $port

read -r -d '' groupPolicy <<-EOF
label: MyGroupPolicy
registrationUtxo: $a_utxo_100_lvls#0
EOF
echo "$groupPolicy" >> $basePath/groupPolicy.yaml

printf "\n<<Create Group policy:"
./bin/plasma-cli simple-minting create --from-fellowship self --from-template default -n private -o $basePath/tx_group.pbuf -i $basePath/groupPolicy.yaml --mint-amount 1 --mint-token group --fee 10 --walletdb $walletDb --keyfile $keyfile -w $password --host $host --port $port

printf "<<Prove a transaction Group:"
./bin/plasma-cli tx prove -w $password --keyfile $keyfile -i $basePath/tx_group.pbuf -o $basePath/tx_group_Proved.pbuf --walletdb $walletDb

printf "<<Broadcast a transaction Group:"
utxo_group="$(./bin/plasma-cli tx broadcast -i $basePath/tx_group_Proved.pbuf --host $host --port $port)"
echo "utxo_group: $utxo_group"

# wallet address iteration 2
walletAddress_2="$(./bin/plasma-cli wallet current-address --walletdb $walletDb --from-fellowship self --from-template default)"
printf "Wallet address: %s\n" "$walletAddress_2"

printf "<<Create a transaction B, transfer 80 lvl from Genesis block with 10 fee:"
./bin/plasma-cli simple-transaction create --from-fellowship nofellowship --from-template genesis --from-interaction 1 --change-fellowship nofellowship --change-template genesis --change-interaction 1 -t $walletAddress_2 -w $password -o $basePath/tx_B.pbuf -n private --amount 80 --keyfile $keyfile --walletdb $walletDb --fee 10 --transfer-token lvl --host $host --port $port

printf "<<Prove transaction B:"
./bin/plasma-cli tx prove -w $password --keyfile $keyfile -i $basePath/tx_B.pbuf -o $basePath/tx_B_Proved.pbuf --walletdb $walletDb

printf "<<Broadcast transaction B:"
b_utxo_80_lvls="$(./bin/plasma-cli tx broadcast -i $basePath/tx_B_Proved.pbuf --host $host --port $port)"
echo "$b_utxo_80_lvls"

read -r -d '' seriesPolicy <<-EOF
label: MySeriesPolicy
registrationUtxo: $b_utxo_80_lvls#0
fungibility: group-and-series
quantityDescriptor: liquid
EOF
echo "$seriesPolicy" >> $basePath/seriesPolicy.yaml

sync="$(./bin/plasma-cli wallet sync --walletdb $walletDb --keyfile $keyfile -w $password --fellowship-name self --template-name default --host $host --port $port)"
echo "$sync"

printf "\n<<Create Series policy:"
./bin/plasma-cli simple-minting create --from-fellowship self --from-template default -n private -o $basePath/tx_series.pbuf -i $basePath/seriesPolicy.yaml --mint-amount 1 --mint-token series --fee 10 --walletdb $walletDb --keyfile $keyfile -w $password --host $host --port $port

printf "<<Prove a transaction Series:"
./bin/plasma-cli tx prove -w $password --keyfile $keyfile -i $basePath/tx_series.pbuf -o $basePath/tx_series_Proved.pbuf --walletdb $walletDb

printf "<<Broadcast a transaction Series:"
utxo_series="$(./bin/plasma-cli tx broadcast -i $basePath/tx_series_Proved.pbuf --host $host --port $port)"
echo "$utxo_series"

read -r -d '' assetMintingStm <<-EOF
groupTokenUtxo: $utxo_series#0
seriesTokenUtxo: $utxo_series#2
quantity: 1
EOF
echo "$assetMintingStm" >> $basePath/assetMintingStm.yaml

sync="$(./bin/plasma-cli wallet sync --walletdb $walletDb --keyfile $keyfile -w $password --fellowship-name self --template-name default --host $host --port $port)"
echo "$sync"

printf "<<Mint an Asset:"
./bin/plasma-cli simple-minting create --from-fellowship self --from-template default --keyfile $keyfile -w $password -o $basePath/tx_asset.pbuf -i $basePath/assetMintingStm.yaml --fee 10 --walletdb $walletDb --mint-token asset -n private --host localhost --port 9084

printf "<<Prove a transaction Asset:"
./bin/plasma-cli tx prove -w $password --keyfile $keyfile -i $basePath/tx_asset.pbuf -o $basePath/tx_asset_Proved.pbuf --walletdb $walletDb

printf "<<Broadcast a transaction Asset:"
utxo_asset="$(./bin/plasma-cli tx broadcast -i $basePath/tx_asset_Proved.pbuf --host $host --port $port)"
echo "$utxo_asset"

sync="$(./bin/plasma-cli wallet sync --walletdb $walletDb --keyfile $keyfile -w $password --fellowship-name self --template-name default --host $host --port $port)"
echo "$sync"
./bin/plasma-cli wallet balance --from-fellowship self --from-template default --walletdb $walletDb --host $host --port $port

printf "End\n"

# stop remove running container(s), clean folders
rm -rf ./tmp
rm -rf ./bin
docker ps -q --filter "name=node" | xargs -r docker stop
docker ps -aq --filter "name=node" | xargs -r docker rm

: <<'EXPECTED_RESULTS'

Wallet synced
Series(bf9f0d4d312bbed05c4afa3ee8751fb3e16be4c345a318a2a1521a78196fb166): 1
Group(7ccfc7bd4c9cfd82a7a1d6ee7ed8244141aad43bc259cf32c338adf5760f7a87): 1
Asset(7ccfc7bd4c9cfd82a7a1d6ee7ed8244141aad43bc259cf32c338adf5760f7a87, bf9f0d4d312bbed05c4afa3ee8751fb3e16be4c345a318a2a1521a78196fb166): 1
LVL: 150

150 =  + 100 + 80 -10 -10 -10  (+transferA, +transferB, -feeGroup, -feeSeries, -feeAsset)

EXPECTED_RESULTS

