CFLAGS += -g

goal:: all run

all:: mqtt_udp_pub mqtt_udp_sub

run::
	./mqtt_udp_sub

clean::
	-rm *.o *.exe

OBJS=udp_send_pkt.o mqtt_udp.o 

mqtt_udp_pub: $(OBJS) mqtt_udp_send.o mqtt_udp_pub.o
	$(CC) $(CFLAGS) -o $@ $^

mqtt_udp_sub: $(OBJS) mqtt_udp_recv.o mqtt_udp_sub.o
	$(CC) $(CFLAGS) -o $@ $^


