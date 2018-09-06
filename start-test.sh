#!/bin/bash

# Launch Arachni
if [ "$LOG_ARACHNI" = "true" ]
then
	/arachni/bin/arachni_rest_server --address 0.0.0.0 &
else
	/arachni/bin/arachni_rest_server --address 0.0.0.0 >/dev/null &
fi

# Launch W3af
if [ "$LOG_W3AF" = "true" ]
then
	/home/w3af/w3af/w3af_api 0.0.0.0:5000 &
else
	/home/w3af/w3af/w3af_api 0.0.0.0:5000 >/dev/null &
fi

# Launch ZAP
if [ "$LOG_ZAP" = "true" ]
then
	/zap/zap.sh -daemon -host 0.0.0.0 -port 8081 -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -config api.disablekey=true &
else
	/zap/zap.sh -daemon -host 0.0.0.0 -port 8081 -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -config api.disablekey=true -nostdout &
fi

echo Waiting 5 seconds for tools to get ready...
sleep 5

# Compile and test application 
cd /code
mvn package -Dspring.profiles.active=docker 
