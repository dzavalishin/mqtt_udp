#!/bin/sh
make ARCH=atmega GLUE=nutos
#make -f Makefile.embedded CC="avr-gcc -mmcu=atmega128 -Os -ffunction-sections -fno-delete-null-pointer-checks -Wall -Wstrict-prototypes " clean install 
cp libmqttudp.a mqtt_udp.h mqtt_udp_defs.h  P:/smart-home-devices/mmnet_mqt_udp_server/mqtt_udp 
