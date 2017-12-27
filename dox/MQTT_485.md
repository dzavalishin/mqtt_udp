# MQTT/485
A proposal, no implementation exist.

MQTT/UDP is really lightweit so it can, possibly be used even without UDP,
over an RS485 bus. This way it can be used on even smaller hardware platforms.

All we need to accomplish this is some kind of checksum, and ModBus checksum
looks like the best choice.
