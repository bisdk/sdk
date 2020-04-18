[![Build Status](https://travis-ci.org/bisdk/sdk.svg?branch=master)](https://travis-ci.org/bisdk/sdk)

# Bisecure SDK

The BiSecure Gateway SDK is a library that can be used to speak to the Hoermann BiSecure Gateway.
It is a small device that can speak the BiSecure wireless protocol with your Hoermann devices (e.g. garage door).

Be aware that this is unofficial / experimental code that can potentially damage your devices! We cannot give you any guarantee! 
Use at your own risk!!!!  

## Example code
See RealGatewayTest class for flow implementation. 
Currently working:
- Discovery
- Get Name as first request
- Login with setting of resulting token in client
- Get Groups to find out first device
- Get Transition for first device => You should see your door state
- Set State for first device => Be aware that will open / close your door :-)

        // Create discovery object
        val discovery = Discovery()
        // Start UDP Server on Port 4002 to listen to responses from GW
        val future = discovery.startServer()
        // Send out the discovery request to the local network
        discovery.sendDiscoveryRequest()
        // Wait for GW response
        val discoveryData = future.join()
        // Create a gatway connection from the data from the GW response 
        val client = GatewayConnection(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        // Initialize the API for sending requests to the BiSecure GW
        val clientAPI = ClientAPI(client)
        println("Name: " + clientAPI.getName())
        println("Ping: " + clientAPI.ping())
        println("Login in...")
        clientAPI.login("thomas", "aaabbbccc")
        val state = clientAPI.getState()
        println("State: $state")
        val groups = clientAPI.getGroups()
        println("Groups: $groups")
        // Get information about door states
        val transition = clientAPI.getTransition(groups[0].ports[0])
        // Send out an impuls to open / close door
        // clientAPI.setState(groups[0].ports[0])
        // Logout again
        clientAPI.logout()

## Things to take care

- You should use a different username / password for each client that connects to the gateway. If you for example use the BiSecure Smartphone App, you should not use the same credentials here in the lib.
- The gateway stops responding after some time, about after 2 or 3 min. Therefore the ClientAPI will reconnect to the gateway again and login again if a timeout occurs. That leads to some requests take much more time than others.

## Local Development
### Install as Maven Dependency
    ./gradlew build
    mvn install:install-file -Dfile=build/libs/sdk-jvm-0.0.2-SNAPSHOT.jar -DpomFile=build/publications/jvm/pom-default.xml 

## Protocol
This is the result of the Reverse Engineering of the App &lt;-> BiSecure Gateway Protocol
It is the attempt to reverse engineer the protocol between the hoermann bisecure gateway and the corresponding app.

The goal is to be able to build an adapter for home automation system to control the garage doors from the automation software. Especially to be able to get the door open when you drive home automatically.

I'm not an expert in reverse engineering nor IP protocols, so my findings could be sometimes wrong :-)

### Discovery
See file Discovery.kt

The app looks for a gateway in the local network by sending out an UDP message to Port 4001 (Destination= 255.255.255.255) with the following payload `<Discover target="LogicBox" />`

The gateway returns the following UDP package to port 4002 at the App's IP:
`<LogicBox swVersion="2.5.0" hwVersion="1.0.0" mac="XX:XX:XX:XX:XX:XX" protocol="MCP V3.0"/>`

After that they apparently create a TCP connection:
```
546	14.294589	192.168.178.45	192.168.178.58	TCP	74	32922 → 4000 [SYN] Seq=0 Win=65535 Len=0 MSS=1360 SACK_PERM=1 TSval=70242312 TSecr=0 WS=64
547	14.295620	192.168.178.58	192.168.178.45	TCP	60	4000 → 32922 [SYN, ACK] Seq=0 Ack=1 Win=512 Len=0 MSS=536
548	14.298392	192.168.178.45	192.168.178.58	TCP	60	32922 → 4000 [ACK] Seq=1 Ack=1 Win=65535 Len=0
```
After that the exchange request response pair over the TCP/IP connection. 

### Message Exchange
The messages that are exchanged over the TCP connection have the following format:
* Byte 0..5: Sender Address 
* Byte 6..11: Receiver Address
    - The sender / Receiver address of the Gateway seems to be the mac address, the address of the app seems to be `000000000000` or `00000000000` + last byte 1..9
* Byte 12..13: body length
* Byte 14: Tag
* Byte 15..18: Session Id
* Byte 19: Command
* PAYLOAD
* The second last byte is the checksum of the inner part (body with payload) and the last byte is the checksum of the whole message. 

The whole hex value array is converted to a string (as human readable hex string) and this is converted to bytes and sent through th TCP connection.

#### Get Name
The get name request is the first request made from APP to GW.

#### Authentication
Send a LOGIN command and receive the token (session id) as response payload.

It seems that only one client can be logged in into the GW. So if your app is also logged in it will be logged out. Or the App will log out your session.

#### Get Transition
Command GET_TRANSITION => Results in door state

#### Set State
To open / close the door send a SET_STATE Command

### JCMP Protocol

JCMP protocol is JSON over CMP. The commands are probably the same as to the cloud service.
The Sequence is:

1. {"cmd":"GET_USERS"}
    Result:
    
        [{"ID":0,"NAME":"ADMIN","ISADMIN":TRUE,"GROUPS":[]}]
2. {"CMD":"GET_GROUPS", "FORUSER":1}
    Result:
    
        [{"ID":0,"NAME":"GARAGE ","PORTS":[{"ID":0,"TYPE":1}]}]
3. {"cmd":"GET_VALUES"}
    
    Result: 
    
        {"00":1,"01":0,"02":0,"03":0,"04":0,"05":0,"06":0,"07":0,"08":0,"09":0,"10":0,"11":0,"12":0,"13":0,"14":0,"15":0,"16":0,"17":0,"18":0,"19":0,"20":0,"21":0,"22":0,"23":0,"24":0,"25":0,"26":0,"27":0,"28":0,"29":0,"30":0,"31":0,"32":0,"33":0,"34":0,"35":0,"36":0,"37":0,"38":0,"39":0,"40":0,"41":0,"42":0,"43":0,"44":0,"45":0,"46":0,"47":0,"48":0,"49":0,"50":0,"51":0,"52":0,"53":0,"54":0,"55":0,"56":0,"57":0,"58":0,"59":0,"60":0,"61":0,"62":0,"63":0}

### Time out of Responses

The gateway responds quite quickly to a request and there are only a few exceptions.
Sometime it does not respond at all, only a reconnect and retry works then.

Example (199 x GetTransition):
2020-01-20T19:23:37.266 INFO: Times (ms): [655, 654, 654, 654, 654, 604, 654, 705, 605, 655, 660, 705, 654, 654, 704, 654, 653, 704, 2917, 652, 703, 653, 653, 653, 704, 704, 653, 652, 703, 653, 653, 653, 653, 652, 653, 652, 653, 652, 653, 652, 652, 655, 652, 652, 653, 652, 652, 655, 603, 652, 653, 602, 652, 652, 653, 652, 653, 652, 602, 653, 652, 653, 652, 656, 602, 652, 653, 602, 652, 603, 652, 602, 602, 602, 653, 653, 602, 602, 703, 652, 652, 702, 703, 652, 653, 702, 653, 702, 703, 652, 652, 702, 703, 652, 702, 703, 652, 652, 703, 652, 702, 652, 652, 653, 652, 651, 653, 652, 702, 652, 651, 651, 652, 702, 652, 652, 652, 657, 702, 652, 702, 702, 652, 702, 657, 1103, 653, 652, 5947, 652, 652, 651, 602, 654, 658, 652, 702, 652, 652, 706, 652, 652, 702, 652, 652, 652, 703, 652, 652, 703, 652, 652, 652, 652, 652, 652, 653, 1102, 652, 657, 652, 652, 653, 601, 652, 602, 652, 652, 602, 652, 652, 652, 652, 652, 652, 652, 652, 653, 652, 652, 652, 652, 602, 652, 652, 652, 653, 652, 652, 602, 652, 652, 602, 652, 652, 652, 602, 652, 652, 602]
2020-01-20T19:23:37.266 INFO: Under 1s: 196, Under 2s: 198, Under 5s: 199 

Example 2 (199 x GetGroups):
[857, 280, 288, 288, 277, 280, 225, 272, 274, 325, 279, 272, 269, 268, 272, 216, 213, 265, 266, 212, 324, 212, 267, 372, 259, 310, 254, 255, 260, 203, 254, 204, 255, 204, 254, 253, 255, 254, 253, 254, 255, 259, 258, 254, 202, 254, 256, 255, 258, 257, 303, 262, 203, 254, 203, 253, 255, 254, 203, 303, 253, 255, 253, 253, 257, 254, 252, 254, 253, 258, 254, 254, 203, 202, 253, 306, 304, 254, 258, 255, 306, 203, 252, 253, 253, 253, 255, 252, 206, 206, 303, 303, 253, 253, 256, 304, 252, 253, 255, 203, 252, 254, 253, 257, 254, 202, 252, 306, 304, 252, 303, 303, 306, 202, 253, 256, 253, 253, 252, 255, 256, 253, 253, 202, 202, 309, 308, 308, 303, 303, 253, 252, 256, 257, 217, 256, 253, 252, 353, 308, 253, 256, 258, 252, 254, 302, 204, 202, 254, 257, 5544, 305, 255, 253, 254, 302, 255, 252, 253, 254, 253, 202, 257, 252, 256, 255, 204, 305, 252, 252, 306, 306, 252, 252, 256, 257, 255, 253, 202, 203, 256, 252, 253, 202, 302, 305, 305, 253, 252, 253, 256, 255, 252, 253, 302, 254, 252, 253, 254, 254]
2020-01-21T16:41:42.539 INFO: Under 1s: 199, Under 2s: 199, Under 5s: 199 

As you can see the most of the get transition commands return after <1s (96%), 2 need little more than 1s and 1 had to be retried after timeout of 2s and needed almost 3s then.

As a result I set the timeout for waiting for a response to 2s, that should be enough for all responses that come and we don't wait too long if we won't get any response at all. 

## Local Development

### Build

     ./gradlew build
     
### Deployment to local Maven Repo

    mvn install:install-file -Dfile=build/libs/sdk-jvm-0.0.2-SNAPSHOT.jar -DpomFile=build/publications/jvm/pom-default.xml
