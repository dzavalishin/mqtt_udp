all::
	ant -f makeJar.xml 

clean:
	-rm -r bin/*

build:: all
	cp MqttUdpViewer.jar mqtt_udp_view mqtt_udp_view.cmd  ../../build

test::
	echo -------------------- TODO ---------------------



check: *.c
	gcc -o check test.c sha256.c md5.c hmac.c  
