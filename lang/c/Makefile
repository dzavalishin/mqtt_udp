# 
# Don't forget to run ./configure or see README.md for ARCH/GLUE parameters
#

#.SILENT:

#
# ARCH defines target architecture (actually programs to compile)
# GLUE defines integration code to connect to tagret OS/monitor
#
# For example: make ARCH=atmega GLUE=nutos
#
# See `arch` and `glue` subdirs for variants
#
# Modify glue/$(GLUE)/Makefile.glue.local for your local paths to includes or whatever
#
ARCH=default
GLUE=unix

#ifeq ($(findstring Makefile.arch,$(wildcard arch/$(ARCH)/*)), )
#($error no arch/$(ARCH)/Makefile.arch exist, need to run ./configure?)
#endif

include arch/$(ARCH)/Makefile.arch
include glue/$(GLUE)/Makefile.glue
-include glue/$(GLUE)/Makefile.glue.local

vpath %.c glue/$(GLUE)

# autoconf: 
# .exe - executable file extension


# Package-related substitution variables

#package        = MQTT-UDP-Library
#version        = 0.1
#tarname        = mqtt-udp-library
#distdir        = $(tarname)-$(version)
#prefix         = /usr/local

version        = $(file < ../../common/defs/version)

LIB=libmqttudp.a
LIBV=libmqttudp.$(ARCH).$(GLUE).$(version).a


CFLAGS += -Iarch/$(ARCH)


include makedefs

goal:: all

all:: library 

ifeq ($(GLUE),unix)
BUILD_PROGRAMS := true
endif

ifeq ($(GLUE),mingw)
BUILD_PROGRAMS := true
endif


ifeq ($(BUILD_PROGRAMS),true)

all:: programs

programs:: mqtt_udp_pub mqtt_udp_sub mqtt_udp_listen mqtt_udp_subscribe

run::
	./mqtt_udp_sub

endif

build:: all
	cp $(LIB) $(LIBV) ../../build

clean::
	-rm *.o *.d *.exe *~

cleanall:: clean
	-rm autom4te.cache/* config.h config.status config.log $(LIB)
	-rmdir autom4te.cache

test:: all
	$(MAKE) -C test test

library:: $(LIB) $(LIBV)

$(LIBV): $(LIB)
	cp $(LIB) $(LIBV)

$(LIB): $(OBJS)
	$(AR) rc $@ $(OBJS)
	$(RANLIB) $@

ifeq ($(BUILD_PROGRAMS),true)

# GLUE_LIBS can be defined in glue

mqtt_udp_pub: mqtt_udp_pub.o $(LIB)
	$(CC) $(CFLAGS) -o $@ $^ $(GLUE_LIBS)

mqtt_udp_sub: mqtt_udp_sub.o $(LIB)
	$(CC) $(CFLAGS) -o $@ $^ $(GLUE_LIBS)

mqtt_udp_listen: mqtt_udp_listen.o $(LIB)
	$(CC) $(CFLAGS) -o $@ $^ $(GLUE_LIBS)

mqtt_udp_subscribe: mqtt_udp_subscribe.o $(LIB)
	$(CC) $(CFLAGS) -o $@ $^ $(GLUE_LIBS)

endif

-include $(exclude README.md, $(wildcard *.md))
-include $(wildcard *.d)

