[![Build Status](https://travis-ci.org/warting/bisecure-gateway.svg?branch=master)](https://travis-ci.org/warting/bisecure-gateway)
[![Build Status](https://travis-ci.org/thomasletsch/bisecure-gateway.svg?branch=master)](https://travis-ci.org/thomasletsch/bisecure-gateway)

# Bisecure Gateway Protocol
Reverse Engineer the App &lt;-> BiSecure Gateway Protocol

This is the attempt to reverse engineer the protocol between the hoermann bisecure gateway and the corresponding app.

The goal is to be able to build an adapter for home automation system to control the garage doors from the automation software. Especially to be able to get the door open when you drive home automatically.

## Example code
See Startup class for flow implementation. 
Currently working:
- Discovery
- Get Name as first request
- Login with setting of resulting token in client
- Get Groups to find out first device
- Get Transition for first device => You should see your door state
- Set State for first device => Be aware that will open / close your door :-)

## Protocol
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

