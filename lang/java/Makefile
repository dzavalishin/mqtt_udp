all: mqtt_udp.jar 

clean:
	-rm -r bin/*

build:: all mqtt_udp.jar 
	cp mqtt_udp.jar ../../build

test::
	echo -------------------- TODO ---------------------



mqtt_udp.jar::
	ant -f build.xml 

