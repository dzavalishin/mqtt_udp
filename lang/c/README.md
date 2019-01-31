# MQTT/UDP library for C language


  mqtt_udp.h			- General MQTT/UDP library header, to be included by clien code
  mqtt_udp_local.h		- Library local header, do not include elsewhere
  mqtt_udp_defs.h		- Generated protocol constants definitions file, see ../../common/defs

  mqtt_udp_build.c		- Build MQTT/UDP packet
  mqtt_udp_parse.c		- Parse MQTT/UDP packet

  mqtt_udp_recv.c		- Incoming packets processing
  mqtt_udp_send.c		- Outgoing packets processing

  mqtt_udp_util.c		- Misc code (dump)

  mqtt_udp.c			- OS specific UDP IO code, can be rewritten to port to other OS or embedded environment
  udp_recv_pkt.c		- OS specific UDP reception code
  udp_send_pkt.c		- OS specific UDP transmission code



## Examples based on MQTT/UDP library


  mqtt_udp_listen.c		- dump all the MQTT/UDP packets that come along

  mqtt_udp_sub.c		- print next coming MQTT/UDP packet (or all packets with -f flag)

  mqtt_udp_pub.c		- publish MQTT/UDP message to all the listeners

  mqtt_udp_subscribe.c  - send subscribe message


## Build instructions


On most systems do './configure && make'
If you have no autoconf tools try 'make -f Makefile.cygwin' or tailor it to your needs


There are target architecture/os dependent subdirs (arch and glue), if you want to 
support any other CPU or OS binding, please create arch/{name-of-your-architecture}
and glue/{name-of-your-os} subdirs and add corresponding makefile and glue code
sources.

To build for non-default arch and not for Unix, type ```make ARCH=your-arch GLUE=your-os```
command. For example, ```make ARCH=atmega GLUE=nutos```.

## Usage

**Send data:**

```c
    int rc = mqtt_udp_send_publish( topic, value );

```

**Listen for data:**

```c

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


```
