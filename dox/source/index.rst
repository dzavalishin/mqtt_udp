.. MQTT/UDP documentation master file, created by
   sphinx-quickstart on Sun Jan  6 20:00:08 2019.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to MQTT/UDP
===================

.. toctree::
   :maxdepth: 2
   :caption: Contents:


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

MQTT/UDP is a simplest possible protocol for IoT, smart home applications and robotics. As you can guess from its name, it is based on MQTT (which is quite simple too), but based on UDP.

Fast track for impatient readers: MQTT/UDP native implementations exist in Java, Python, C, Lua and PLC specific ST language. See corresponding references:

* :ref:`c-lang-api`
* :ref:`java-lang-api`
* :ref:`python-lang-api`
* :ref:`lua-lang-api`








.. _c-lang-api:

MQTT/UDP C Language API Reference
=================================


There is a native MQTT/UDP implementation in C. You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/c repository.


Lets begin with a simplest examples.

Send data::


    int rc = mqtt_udp_send_publish( topic, value );



Listen for data::


    int main(int argc, char *argv[])
    {
        ...
    
        int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );
    
        ...
    }
    
    int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o )
    {
    
        printf( "pkt %x flags %x, id %d",
                o->ptype, o->pflags, o->pkt_id
              );
    
        if( o->topic_len > 0 )
            printf(" topic '%s'", o->topic );
    
        if( o->value_len > 0 )
            printf(" = '%s'", o->value );
    
        printf( "\n");
    }


Now lets get through the packet structure definition::

    struct mqtt_udp_pkt
    {
        int         from_ip;
    
        int         ptype;          // upper 4 bits, not shifted
        int         pflags;         // lower 4 bits
    
        size_t      total;          // length of the rest of pkt down from here
    
        int         pkt_id;
    
        size_t      topic_len;
        char *      topic;
    
        size_t      value_len;
        char *      value;
    };


Listen for packets
------------------

See `Example code <https://github.com/dzavalishin/mqtt_udp/blob/master/lang/c/mqtt_udp_listen.c>`_.

For listening for data from the network you need just some of fields. First, you have to check
that packet is transferring item data::

    struct mqtt_udp_pkt p;
    
    if( p->ptype == PTYPE_PUBLISH )
    {
    // Got data message
    }

For the first implementation just ignore all other packets. Frankly, there's not much for you to ignore.

Now get topic and data from packet you got::


    strlcpy( my_value_buf, p->value, sizeof(my_data_buf) );
    strlcpy( my_topic_buf, p->topic, sizeof(my_topic_buf) );


And you're done, now ypou have topic and value received.


Includes
--------

There's just one::

    #include "mqtt_udp.h"


Functions
---------

Send PUBLISH packet::

    int mqtt_udp_send_publish( char *topic, char *data );

Send SUBSCRIBE packet::

    int mqtt_udp_send_subscribe( char *topic );

Send PINGREQ packet, ask others to respond::

    int mqtt_udp_send_ping_request( void );

Send PINGREST packet, tell that you're alive::

    int mqtt_udp_send_ping_responce( void );


Start loop for packet reception, providing callback to be called 
when packet arrives::

    typedef int (*process_pkt)( struct mqtt_udp_pkt *pkt );

    int mqtt_udp_recv_loop( process_pkt callback );

Dump packet structure. Handy to debug things::

    int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o );



UDP IO interface
----------------

Default implementation uses POSIX API to communicate with network, but for 
embedded use you can redefine corresponding functions.

Receive UDP packet. Must return sender's address in `src_ip_addr`::

    int mqtt_udp_recv_pkt( int fd, char *buf, size_t buflen, int *src_ip_addr );

Broadcast UDP packet::

    int mqtt_udp_send_pkt( int fd, char *data, size_t len );

Send UDP packet (actually not used now, but can be later)::

    int mqtt_udp_send_pkt_addr( int fd, char *data, size_t len, int ip_addr );

Create UDP socket which can be used to send or broadcast::

    int mqtt_udp_socket(void);

Prepare socket for reception on MQTT_PORT::

    int mqtt_udp_bind( int fd )

Close UDP socket::

    int mqtt_udp_close_fd( int fd ) 



.. _java-lang-api:

MQTT/UDP Java Language API Reference
=================================


There is a native MQTT/UDP implementation in Java. You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/java repository.


Again, here are simplest examples.

Send data::


    PublishPacket pkt = new PublishPacket(topic, value);
    pkt.send();



Listen for data::


    PacketSourceServer ss = new PacketSourceServer();
    ss.setSink( pkt -> { 
        System.out.println("Got packet: "+pkt);
    
        if (p instanceof PublishPacket) {
            PublishPacket pp = (PublishPacket) p;			
        }
    
    });




Listen for packets
------------------

See `Example code <https://github.com/dzavalishin/mqtt_udp/blob/master/lang/java/src/ru/dz/mqtt_udp/util/Sub.java>`_.


Here it is::

    package ru.dz.mqtt_udp.util;
    
    import java.io.IOException;
    import java.net.SocketException;
    
    import ru.dz.mqtt_udp.IPacket;
    import ru.dz.mqtt_udp.MqttProtocolException;
    import ru.dz.mqtt_udp.SubServer;
    
    public class Sub extends SubServer 
    {
    
        public static void main(String[] args) throws SocketException, IOException, MqttProtocolException 
        {
            Sub srv = new Sub();
            srv.start();
        }

        @Override
        protected void processPacket(IPacket p) {
            System.out.println(p);
                
            if (p instanceof PublishPacket) {
                PublishPacket pp = (PublishPacket) p;

                // now use pp.getTopic() and pp.getValueString() or pp.getValueRaw()
            }
        }
    }


Now what we are doung here. Our class `Sub` is based on `SubServer`, which is doing all the reception job, and calls `processPacket`
when it got some data for you. There are many possible types of packets, but for now we need just one, which is
`PublishPacket`. Hence we check for type, and convert::

    if (p instanceof PublishPacket) {
        PublishPacket pp = (PublishPacket) p;

Now we can do what we wish with data we got using `pp.getTopic()` and `pp.getValueString()`.


Listen code we've seen in a first example is slightly different::


    PacketSourceServer ss = new PacketSourceServer();
    ss.setSink( pkt -> { 
        System.out.println("Got packet: "+pkt);
    
        if (p instanceof PublishPacket) {
            PublishPacket pp = (PublishPacket) p;			
        }
    
    });

Used here `PacketSourceServer`, first of all, starts automatically, and uses `Sink` you pass to `setSink`
to pass packets received to you. The rest of the story is the same.





















.. _python-lang-api:

MQTT/UDP Python Language API Reference
======================================

As you already guessed, python implementation is native too. You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/python3 repository.
There is also lang/python directory, which is for older 2.x python environment, but it is outdated. Sorry, can't afford to support it. You can backport some
python3 code, it should be quite easy.


Let's begin with examples, as usual.

Send data::


    mqttudp.engine.send_publish_packet( "test_topic", "Hello, world!" )



Listen for data::


    def recv_packet(ptype,topic,value,pflags,addr):
        if ptype != "publish":
            print( ptype + ", " + topic + "\t\t" + str(addr) )
            return
        print( topic+"="+value+ "\t\t" + str(addr) )
       
    mqttudp.engine.listen(recv_packet)


All functions
-------------

* `send_ping()` - send PINGREQ packet.
* `send_ping_responce()` - send PINGRESP packet. It is sent automatically, you don't have to.
* `listen(callback)` - listen for incoming packets.
* `send_publish_packet( topic, payload)` - this what is mostly used.
* `send_subscribe(topic)` - ask other party to send corresponding item again. This is optional.




























.. _lua-lang-api:

MQTT/UDP Lua Language API Reference
===================================


**NB! Lua API is not final, there will be some methods rename.**

You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/lua repository.


Basic examples in Lua.

Send data::


    local mq = require "mqtt_udp_lib"
    mq.publish( topic, val );



Listen for data::


    local mq = require "mqtt_udp_lib"
    
    local listener = function( ptype, topic, value, ip, port )
        print("'"..topic.."' = '"..val.."'".."	from: ", ip, port)
    end
    
    mq.listen( listener )
















