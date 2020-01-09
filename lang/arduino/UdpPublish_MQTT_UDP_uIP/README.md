This one uses uIP UDP stack and ENC28j60 hardware

## Connection

RST -> RESET
GND -> GND
3V3 -> VCC (some boards need +5v, see board spec.)
D2  -> INT (not really used)
D10 -> CS
D11 -> SI
D12 -> SD
D13 -> SCK

D9  -> 18b20 temperature sensor, also used to generate MAC address

