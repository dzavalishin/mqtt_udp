
.. _java-lang-api:

Java Language API Reference
---------------------------


There is a native MQTT/UDP implementation in Java. You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/java repository.


Again, here are simplest examples.

.. index:: single: send

Send data::


    PublishPacket pkt = new PublishPacket(topic, value);
    pkt.send();


.. index:: single: listen

Listen for data::


    PacketSourceServer ss = new PacketSourceServer();
    ss.setSink( pkt -> { 
        System.out.println("Got packet: "+pkt);
    
        if (p instanceof PublishPacket) {
            PublishPacket pp = (PublishPacket) p;			
        }
    
    });




Listen for packets
^^^^^^^^^^^^^^^^^^

See `Example Java code <https://github.com/dzavalishin/mqtt_udp/blob/master/lang/java/src/ru/dz/mqtt_udp/util/Sub.java>`_.


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


Now what we are doung here. Our class ``Sub`` is based on ``SubServer``, which is doing all the reception job, and calls ``processPacket``
when it got some data for you. There are many possible types of packets, but for now we need just one, which is
``PublishPacket``. Hence we check for type, and convert::

    if (p instanceof PublishPacket) {
        PublishPacket pp = (PublishPacket) p;

Now we can do what we wish with data we got using ``pp.getTopic()`` and ``pp.getValueString()``.


Listen code we've seen in a first example is slightly different::


    PacketSourceServer ss = new PacketSourceServer();
    ss.setSink( pkt -> { 
        System.out.println("Got packet: "+pkt);
    
        if (p instanceof PublishPacket) {
            PublishPacket pp = (PublishPacket) p;			
        }
    
    });

Used here ``PacketSourceServer``, first of all, starts automatically, and uses ``Sink`` you pass to ``setSink``
to pass packets received to you. The rest of the story is the same.


There is another, more complex listen server class, ```PacketSourceMultiServer```. 
Instance of it can provide incoming packets to more than one listener::

	PacketSourceMultiServer ms = new PacketSourceMultiServer();
        ms.addPacketSink( first_listener );
        ms.addPacketSink( next_listener );
        ms.start(); // Does not start automatically



.. index:: single: send

Packet classes
^^^^^^^^^^^^^^

There are ``PublishPacket``, ``SubscribePacket``, ``PingReqPacket`` and ``PingRespPacket``. Usage is extremely simple::

   new PingReqPacket().send();




.. index:: single: match

Service
^^^^^^^

Match topic name against a pattern, processing `+` and `#` wildcards, returns true on match::

   TopicFilter tf = new TopicFilter("aaa/+/bbb");
   boolean matches = tf.test("aaa/ccc/bbb") );

TopicFlter is a Predicate (functional interface implementation).

.. rem TODO example of incoming packets filtering



Control
^^^^^^^

**setMuted( boolean mute )**
   PacketSourceServer and PacketSourceMultiServer can be swiched to not to 
   reply to any incoming packet (such as PING) automatically.

.. index:: single: throttle

**Engine.setThrottle(int msec)**
   Set average time in milliseconds between packets sent. Set to 0 to turn throttling off.


Error handling
^^^^^^^^^^^^^^

There is a global error handler which is called from library code on
IO, protocol and some other errors. Default handler just prints error message
on ``stderr``. You can install your own error handler with call to
``GlobalErrorHandler.setHandler(BiConsumer<ErrorType, String> h)``.


Digital Signature
^^^^^^^^^^^^^^^^^

.. NOTE::

   Development is in progress, not a final implementation.

Java ``ru.dz.mqtt_udp.Engine`` class has preliminary controls for message digital
signature. It is implemented with HMAC MD5 technology.

Set signature secret key to sign and check signature::

   void setSignatureKey(String key)

Set requirement for all incoming packets to be signed::

   void setSignatureRequired(boolean req)

At this moment other language implementations ignore and do not generate signature at all.




