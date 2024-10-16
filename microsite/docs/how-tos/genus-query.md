---
sidebar_position: 8
---

# Query The Indexer Node

The Indexer node provides a query mode to query the UXTOs of a given address. 

## Query UXTO by address

To query UXTOs by address run the following command:

```bash
brambl-cli indexer-query utxo-by-address --from-fellowship $FELLOWSHIP --from-template $LOCK_TEMPLATE -h $HOST --port $PORT --walletdb $WALLET
```

This will query the UXTOs for the address in the indexer node. It uses the wallet to derive the right address to query.