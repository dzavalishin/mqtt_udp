# Remote configuration server example

This is a simplest program which can serve remote configuration requests
with MQTT/UDP.

An ``remote_config.items`` file contains ``topic=value`` lines containing 
answers for remote configuration requests.

Requesting parties must send **SUBSCRIBE** message with corresponding 
topics and server will reply with **PUBLISH** messages with value.

Server will not send any information without request.

Java library contains ``ru.dz.mqtt_udp.config.Requester`` class which is
counterpart to this server and is able to request needed information.

For other languages there is no lib yet, but implementation is trivial.
