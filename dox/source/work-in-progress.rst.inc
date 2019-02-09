
Work in progress
----------------

There are parts of protocol or additional components design that 
not finished completely, and are subject of discussion.

QoS Server
^^^^^^^^^^

Nowadays UDP is quite reliable (typical loss rate is < 0.5% even in a busy network),
but not 100% reliable. Things are easy in point to point communications. Send a message,
wait for acknowledgement, resend if none received in a reasonable time. But
MQTT/UDP is broadcast protocol. Do we have to wait for ack from all nodes in a network?
Some nodes? Which ones?

It is makes sense that we can build a map of nodes that listen to us by collecting
their responces. But we want to keep MQTT/UDP implementation simple and this is not
that simple. And not any node needs such high a reliability.

The idea is to add separate server on a network that will build lists of listeners
for each topic, collect low-QoS ack packets and sent one high-QoS ack packet
to topic publisher(s).

.. figure:: diagrams/qos_server.*

    QoS server sequence diagram

Note that such server is not a single point of failure. First of all, there can be more
than one instance of QoS server. Second, even if QoS server fails, nodes continue to
send data. Though, each packet will be resent few times, but it is not a communications
failure. Last, but not least, sending node can stop resending after few acks with lower
QoS. For example, sending node can take for acknowledge one QoS 3 ack message and 2 or 3
QoS 2 ones.
