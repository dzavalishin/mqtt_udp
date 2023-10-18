# MQTT UDP .net

This is a implementation of the [MQTT UDP](https://github.com/dzavalishin/mqtt_udp) specification as defined by Dmitry Zavalishin.

## Motivation for the port

I was searching for a lightweight way to do pubsub between a few processes. I started out by just using UDP broadcast but then thought that maybe there already would be a library and I stumbled upon this specification.

I'm aware of things like ZeroMQ that support UDP but it felt like a simple little project to make so I spend a few long evenings on it.

Supported TTR's:

- 'n' - Number
- 's' - MD5 Signature
- 'm' - Measurement timestamp
- 'r' - ReplyTo
- 'p' - Publish timestamp

# Publish/Subscribe

It works similarly as regular MQTT which is topic based.

There is a publisher and a subscriber but because it uses UDP broadcast/multicast the publisher will not track a list of subscribers. Each publish will always be a UDP broadcast/multicast.

## Retained messages

A client can subscribe to a set of topics. These subscription requests are broadcasted or multicasted. Any publisher that has matching retained messages will at that moment publish that topic.

I'm not sure if subscribe should work according to the spec but it is convenient for my environment.
