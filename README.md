# Bisecure Gateway Protocol
Reverse Engineer the App &lt;-> BiSecure Gateway Protocol

This is the attempt to reverse engineer the protocol between the hoermann bisecure gateway and the corresponding app.

The goal is to be able to build an adapter for home automation system to control the garage doors from the automation software. Especially to be able to get the door open when you drive home automatically.

## Example code
See Startup class for flow implementation. 
It makes a discovery request first and then tries to execute a get name request. 

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

#### Message Exchange
The messages that are exchanged over the TCP connection have the following format:
Byte 0..5: Sender Address 
Byte 6..11: Receiver Address
The sender / Receiver address of the Gateway seems to be the mac address, the address of the app seems to be `000000000000` or `00000000000` + last byte 1..9
Byte 12..13: body length
Byte 14: Tag
Byte 15..18: Session Id
Byte 19: Command
PAYLOAD
The last two bytes seem to be some kind of checksum. 

#### Authentication
The request fromthe app to the gateway for authentication seems to have the following format:
`{appId: 6byte}{gatewayId: 6byte}00{bodyLength:2 byte}000000000010{userNameLength: 1byte}{userNameHex}{passwordHex}{checksum: 2byte}`

Example (I x-ed out my MAC :-):
`000000000000XXXXXXXXXXXX00190000000000100674686F6D61736161616262626363632DF0`
Username is `thomas`and password is `aaabbbccc`

The gateway response with some kind of session:
`000000000006XXXXXXXXXXXX000900F4443564300A78`where `F4443564`seems to be the session id, because it is part of every sub sequent request. The App Address is now set to `000000000006`.
