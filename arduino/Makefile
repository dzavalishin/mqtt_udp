
# Copy corresponding files from main c code directory

SRC=../c
LIB=MQTT_UDP/
FILES=mqtt_udp.h mqtt_udp_local.h mqtt_udp_send.c

SRCFILES += $(patsubst %,$(SRC)/%,$(FILES))


all:
	cp $(SRCFILES) $(LIB)
