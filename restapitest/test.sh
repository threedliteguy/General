#!/bin/bash

for i in {1..1000}
do
curl -s   http://127.0.0.1:$1/ra -X POST -H 'Content-Type: application/json' -d '["1","2"]' > /dev/null
done
