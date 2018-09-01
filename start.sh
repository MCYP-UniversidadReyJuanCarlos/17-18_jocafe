#!/bin/bash

# Launch ZAP
/zap/zap.sh -daemon -host 0.0.0.0 -port 8081 -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -config api.disablekey=true &

# Launch Arachni
/arachni/bin/arachni_rest_server --address 0.0.0.0 &

# Launch W3af
/home/w3af/w3af/w3af_api 0.0.0.0:5000 &


echo Waiting 5 seconds for tools to get ready...
sleep 5

# Run application 
cd /code/target
java -jar -Dspring.profiles.active=docker WebSecTester.jar 
