---
sidebar_position: 4
---

# Node Query Mode

``` 
Command: node-query [block-by-height|block-by-id|transaction-by-id]
Node query mode
Command: node-query block-by-height [options]
Get the block at a given height
  -h, --host <value>       The host of the node. (mandatory)
  --port <value>           Port Node node. (mandatory)
  -s, --secure <value>     Enables the secure connection to the node. (optional)
  --height <value>         The height of the block. (mandatory)
Command: node-query block-by-id [options]
Get the block with a given id
  -h, --host <value>       The host of the node. (mandatory)
  --port <value>           Port Node node. (mandatory)
  -s, --secure <value>     Enables the secure connection to the node. (optional)
  --block-id <value>       The id of the block in base 58. (mandatory)
Command: node-query transaction-by-id [options]
Get the transaction with a given id
  -h, --host <value>       The host of the node. (mandatory)
  --port <value>           Port Node node. (mandatory)
  -s, --secure <value>     Enables the secure connection to the node. (optional)
  --transaction-id <value>
                           The id of the transaction in base 58. (mandatory)                  
```