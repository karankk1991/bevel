{
 "genesis": {
   "config": {
     "chainId": {{ chain_id }},
     "constantinoplefixblock": 0,
     "contractSizeLimit": 2147483647,
     "qbft": {
       "blockperiodseconds": 2,
       "epochlength": 30000,
       "requesttimeoutseconds": 10
     }
   },
   "nonce": "0x0",
   "timestamp": "0x58ee40ba",
   "gasLimit": "0x1fffffffffffff",
   "difficulty": "0x1"
 },
 "blockchain": {
   "nodes": {
      "generate": true,
      "count": {{ total_peer_count }}
    }
 }
}
