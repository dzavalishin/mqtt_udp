.. _c-lang-api:

C Language API Reference
------------------------


There is a native MQTT/UDP implementation in C. You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/c repository.


Lets begin with a simplest examples.

.. index:: single: send

Send data::

    int rc = mqtt_udp_send_publish( topic, value );


.. index:: single: listen

Listen for data::

    int main(int argc, char *argv[])
    {
        ...   
        int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );
        ...
    }
    
    int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o )
    {    
        printf( "pkt %x flags %x, id %d", o->ptype, o->pflags, o->pkt_id );
    
        if( o->topic_len > 0 )
            printf(" topic '%s'", o->topic );
    
        if( o->value_len > 0 )
            printf(" = '%s'", o->value );
    
        printf( "\n");
    }


Now lets look at the packet structure definition::

    struct mqtt_udp_pkt
    {
        int         from_ip;
    
        int         ptype;          // upper 4 bits, not shifted
        int         pflags;         // lower 4 bits
    
        size_t      total;
    
        int         pkt_id;
    
        size_t      topic_len;
        char *      topic;
    
        size_t      value_len;
        char *      value;

        char        is_signed;
    };



**from_ip**
   Ip address of message sender. Usually ignored.
   
**ptype**
   Packet type. You will be interested in ```PTYPE_PUBLISH``` most of time. 
   See ```mqtt_udp_defs.h``` for more.

**pflags**
   Flags specific for each type. Ignore. Current version of MQTT/UDP 
   does not use them at all, and in any case everything critical will be
   processed by library.
   
**total**
   This field is internal for library.

**pkt_id**
   Packet id. Leave zero for outgoing packets, and sequential number
   will be provided. In incoming packets it will be filled if sender 
   supports TTRs (extended packet structure).
   
**topic** and **topic_len**
   Message topic, NULL terminated. Length of topic in bytes.
   
**value** and **value_len**
   Message value, also NULL terminated. Length of value in bytes.
   
**is_signed**
   This packet has correct digital signature. 

.. index:: single: listen

Listen for packets
^^^^^^^^^^^^^^^^^^

See `Example C code <https://github.com/dzavalishin/mqtt_udp/blob/master/lang/c/mqtt_udp_listen.c>`_.

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
^^^^^^^^

There's just one::

    #include "mqtt_udp.h"


.. index:: single: send

Functions
^^^^^^^^^

Send PUBLISH packet::

    int mqtt_udp_send_publish( char *topic, char *data );

Send SUBSCRIBE packet::

    int mqtt_udp_send_subscribe( char *topic );

Send PINGREQ packet, ask others to respond::

    int mqtt_udp_send_ping_request( void );

Send PINGRESP packet, tell that you're alive::

    int mqtt_udp_send_ping_responce( void );


Start loop for packet reception, providing callback to be called 
when packet arrives::

    typedef int (*process_pkt)( struct mqtt_udp_pkt *pkt );

    int mqtt_udp_recv_loop( process_pkt callback );

Dump packet structure. Handy to debug things::

    int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o );


.. index:: single: throttle

Set minimal time between outgoing packets (msec), control maximum send speed::

    void mqtt_udp_set_throttle(int msec);

.. index:: single: error handler

Set callback to handle internal errors such as net IO error::

   typedef enum {
       MQ_Err_Other,
       MQ_Err_Establish,   // open socket
       MQ_Err_IO,          // net io
       MQ_Err_Proto,       // broken pkt
       MQ_Err_Timeout,
   } mqtt_udp_err_t;
   
   typedef int err_func_t( mqtt_udp_err_t type, int err_no , char * msg, char * arg );
   
   void mqtt_udp_set_error_handler( err_func_t *handler );

User error handler can:

* Return zero: caller must attempt to ignore error, if possible.
* Return err_no: caller must return with error in turn, if possible.
* Exit (or restart application completely) if error is supposed to be fatal.

Handler can also be used for logging.

.. index:: single: signature

Digital signature
^^^^^^^^^^^^^^^^^

There is implementation of digital signature implemented. To use it call ``mqtt_udp_enable_signature``
passing encryption key. Same key must be used on all nodes that use signature. Nodes that have no
signature turned on will not sign outgoing messages and will ignore incoming signatures.

Start using signature::

   int mqtt_udp_enable_signature( const char *key, size_t key_len );

**key**
   Key used to sign outgoing messages and check signature on incomnig ones.
   Usually just an ASCII string, but can be any binary data.

**key_len**
   Number of valid bytes in key.

If signature is turned on and incoming packet is correctly signed, it
will have nonzero ``is_signed`` field.



.. index:: single: match

Service
^^^^^^^

Match topic name against a pattern, processing `+` and `#` wildcards, returns 1 on match::

   int mqtt_udp_match( const char *wildcard, const char *topic_name );


Remote configuration
^^^^^^^^^^^^^^^^^^^^

This part of API lets user to configure program/device by network. There is a detailed
description in :ref:`passive-rconfig` and in the Python part of this book, 
see :ref:`python-rconfig-api`. Here is description of C implementation.

Set up remote config subsystem::

   #include "runtime_cfg.h"

   mqtt_udp_rconfig_item_t rconfig_list[] =
   {
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Switch 1 topic",	"topic/sw1", { .s = 0 } },
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Switch 2 topic",	"topic/sw2", { .s = 0 } },
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Di 0 topic",           "topic/di0", { .s = 0 } },
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Di 1 topic",           "topic/di1", { .s = 0 } },
   
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_OTHER, "MAC address",          "net/mac",   { .s = 0 } },
   
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_INFO, "Switch 4 topic",        "info/soft",   { .s = DEVICE_NAME } },
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_INFO, "Switch 4 topic",        "info/ver",    { .s = 0 } },
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_INFO, "Switch 4 topic",        "info/uptime", { .s = 0 } },
   
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_OTHER, "Name", 		"node/name",     { .s = 0 } },
       { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_OTHER, "Location", 	        "node/location", { .s = 0 } },
   };   
   
   int rconfig_list_size = sizeof(rconfig_list) / sizeof(mqtt_udp_rconfig_item_t);

   int rc = mqtt_udp_rconfig_client_init( mac_string, rconfig_rw_callback, rconfig_list, rconfig_list_size );
   if( rc ) printf("rconfig init failed, %d\n", rc );
   
Each array item is one parameter to be set up remotely. The only type supported now is ``MQ_CFG_TYPE_STRING``. Kinds:

MQ_CFG_KIND_INFO
   Read-only information about this instance (program or device)
   
MQ_CFG_KIND_TOPIC
   Is a configurable topic name, used to publish or receive information.

MQ_CFG_KIND_OTHER
   Any other parameter type. (R/W and not topic)

Third item field is human-readable item description, currently it is not used, but will be translated to
configuration tool. Fourth item is identification of configurable item, both for local and remote side.
For remote side it is sent as part of configuration message topic and is shown to user as configuration
iem description. Last field is current parameter value. For read-only parameters you can just put any string
pointer here. For R/W string must be malloc'ed (or set with ``mqtt_udp_rconfig_set_string()``).

To be precise::

   /// Definition of configuration parameter
   typedef struct
   {
       mqtt_udp_rconfig_item_type_t        type;   ///< Item (.value field) data type (string, bool, number, other)
       mqtt_udp_rconfig_inetm_kind_t       kind;   ///< Item kind, not processed by network code
       const char *                        name;   ///< Human readable name for this config parameter
       const char *                        topic;  ///< MQTT/UDP topic name for this config parameter
       mqtt_udp_rconfig_item_value_t       value;  ///< Current value
       mqtt_udp_rconfig_item_value_t       opaque; ///< user data item, not processed by MQTT/UDP code at all
   } mqtt_udp_rconfig_item_t;

type
   Data type for ``.value``, must be MQ_CFG_TYPE_STRING as for now.
   
kind
   Kind of item, see above. If kind is MQ_CFG_KIND_TOPIC, ``.topic`` field must begin with "topic/".
   
name
   Human-readable description, unused now.
   
value
   Current value. You will be using ``.value.s`` union field.

opaque
   Not used or interpreted, use as pointer to external storage for this item, 
   internal item index or function pointer to read/set item as you wish.


Now lets look at available functions.

Init subsystem::

   int rc = mqtt_udp_rconfig_client_init( mac_string, rconfig_rw_callback, rconfig_list, rconfig_list_size );

mac_string
   Id string (12 bytes) used as unique id of this configurable instance. MAC address of device is
   a good candidate.

rconfig_rw_callback
   Callback called by subsystem to ask you provide current value for item or get new setting
   after instance item was remotely set up. Prototype is ``int rconfig_rw_callback( int pos, int write )``,
   where ``pos`` is item position (index) in array and ``write`` is nonzero if callback shall
   get new setting from instance array and save it somewhare for next run. If zero, callback
   must read saved instance value and call ``mqtt_udp_rconfig_set_string()`` for it.


Set item value::

   int mqtt_udp_rconfig_set_string( int pos, char *string );

pos
   Item position (index) in array
   
string
   New value

Get item value checking kind::

   const char * rconfig_get_string_by_item_index( int pos, mqtt_udp_rconfig_inetm_kind_t kind );

pos
   Item position (index) in array

kind
   Expected kind for item. If not, global error callback is called and ``NULL`` is returned.
   This function is supposed to be used to get configurable topic for outgoing message
   so usually this parameter is ``MQ_CFG_KIND_TOPIC``.


Find item by ``.value`` string::

   int rconfig_find_by_string_value( const char *search, mqtt_udp_rconfig_inetm_kind_t kind );
   
search
   String value to be found.

kind
   Only lines of this kind will match. This function is supposed to look up
   incoming items topics to find if some of configurable topics match. So
   this parameter usually is ``MQ_CFG_KIND_TOPIC``.


Please study this API use example in `sample remote config C application`_.

.. _sample remote config C application: https://github.com/dzavalishin/smart-home-devices/blob/master/mmnet_mqt_udp_server/main/rconfig_client.c



.. index:: single: UDP

UDP IO interface
^^^^^^^^^^^^^^^^

Default implementation uses POSIX API to communicate with network, but for 
embedded use you can redefine corresponding functions. Here are things to 
reimplement.

Receive UDP packet. Returning value is number of bytes in packet received or
negative error code. Must return sender's address in ``src_ip_addr``::

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
