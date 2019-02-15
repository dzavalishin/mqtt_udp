# Global settings for all implementations

Settings are changed in ``mqtt_udp_defs.definitions`` and ``make version`` 
applies changes to files around the repository.

*      PACKAGE_VERSION_{MINOR,MAJOR} is version of this repository, not protocol
*      PKT_PACE_MSEC is min time between packets when we sending
*      DEFAULT_MAX_REPLY_QOS is default setting for maximum level of QoS field for PubAck (or other acks?)
