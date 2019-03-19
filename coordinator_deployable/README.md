#Prerequisites
  
1. underlying openflow network need to consist of ofcontroller, ofswitch and host. you can choose floodlight or ryu this like open source ofcontroller to do this work. as for ofswitch and host, you can simply choose mininet to substitute for them, of course, can also use physical ofsiwitch and host.

2. you need to install jdk1.8 64bit version.

3. you need a tool for sending httpRequest to Coordinator, such as Postman.

#Startup

1. configure underlying openflow network. here, we use the example of floodlight and mininet.

        $cd floodlight_0.9
        $ant
        $sed 's/FloodlightProvider.openflowport = 6666/FloodlightProvider.openflowport = 6667/g' src/main/resources/floodlightdefault.properties | sed 's/RestApiServer.port = 8081/RestApiServer.port = 10001/g' > floodlightdefault.properties.tmp1
        $sed 's/FloodlightProvider.openflowport = 6666/FloodlightProvider.openflowport = 6668/g' src/main/resources/floodlightdefault.properties | sed 's/RestApiServer.port = 8081/RestApiServer.port = 20001/g' > floodlightdefault.properties.tmp2
        $sed 's/FloodlightProvider.openflowport = 6666/FloodlightProvider.openflowport = 6669/g' src/main/resources/floodlightdefault.properties | sed 's/RestApiServer.port = 8081/RestApiServer.port = 30001/g' > floodlightdefault.properties.tmp3
        $cd target
        $java -jar floodlight.jar -cf floodlightdefault.properties.tmp1 &
        $java -jar floodlight.jar -cf floodlightdefault.properties.tmp2 &
        $java -jar floodlight.jar -cf floodlightdefault.properties.tmp3 &

        $mn -c
        $mn --controller remote,ip={Coordinator-ip},port={Coordinator-port} --topo linear,2
        
2. look up coordinator help file.

        $cd Coordinator
        $java -jar Coordinator.jar --help

3. directly run coordinator jar file.

        $cd Coordinator
        $java -jar Coordinator.jar --of-host={Coordinator-ip} --of-port={Coordinator-port}

4. run coordinator via script.

        $cd Coordinator
        $cd script
        $sh Coordinator.sh

5. configure controller replica.
        
        via Postman send this like http post request to add controller replica for Coordinator:
        http://{Coordinator-rpcip}:{Coordinator-rpcport}/admin?JSONRPCString={
                "jsonrpc": "2.0",
                "method": "addController",
                "params": {
                        "controllerIp": {controller-ip},
                        "controllerPort": {controller-port}
                },
                "id": "Coordinator"
        }

        via Postman send this like http post request to delete controller replica for Coordinator:
        http://{Coordinator-rpcip}:{Coordinator-rpcport}/admin?JSONRPCString={
                "jsonrpc": "2.0",
                "method": "deleteController",
                "params": {
                        "controllerIp": {controller-ip},
                        "controllerPort": {controller-port}
                },
                "id": "Coordinator"
        }

        enjoy !

#Credits

1. mininet 2. floodlight 3. Postman