CFLAGS += -g

goal:: all run

all:: mqtt_udp_pub mqtt_udp_sub

run::
	./mqtt_udp_sub

clean::
	rm *.o

mqtt_udp_pub: mqtt_udp.o mqtt_udp_send.o mqtt_udp_pub.o
	$(CC) $(CFLAGS) -o $@ $^

mqtt_udp_sub: mqtt_udp.o mqtt_udp_recv.o mqtt_udp_sub.o
	$(CC) $(CFLAGS) -o $@ $^


