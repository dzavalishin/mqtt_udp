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
vpath %.c examples
vpath %.c hmac

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


CFLAGS += -Wall -Iarch/$(ARCH)


goal:: all

#todo kill me
#ifeq ($(GLUE),unix)
#BUILD_PROGRAMS := true
#endif

ifeq ($(GLUE),mingw)
BUILD_PROGRAMS := true
endif


include makedefs
include makerules







build:: all
	cp $(LIB) $(LIBV) ../../build


test:: all
	$(MAKE) -C test test

library:: $(LIB) $(LIBV)

$(LIBV): $(LIB)
	cp $(LIB) $(LIBV)



-include $(exclude README.md, $(wildcard *.md))
-include $(wildcard *.d)

