#!/bin/sh
make ARCH=atmega GLUE=nutos
#make -f Makefile.embedded CC="avr-gcc -mmcu=atmega128 -Os -ffunction-sections -fno-delete-null-pointer-checks -Wall -Wstrict-prototypes " clean install 
