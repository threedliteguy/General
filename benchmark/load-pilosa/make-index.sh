#!/bin/bash
curl -XDELETE localhost:10101/index/test
#curl -XPOST localhost:10101/index/test -d '{"options":{"keys":true}}'
curl -XPOST localhost:10101/index/test 
for i in {0..50}
do
  curl -XPOST localhost:10101/index/test/field/c$i -d '{"options":{"keys":true}}'
  #curl -XPOST localhost:10101/index/test/field/c$i -d '{"options": {"type": "int", "min": 0, "max":100}}'
done
curl -XGET localhost:10101/index/test

