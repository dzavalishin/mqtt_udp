all: # mqtt_udp.jar 
	mvn

clean:
	-rm -r bin/*

build:: all mqtt_udp.jar 
	cp mqtt_udp.jar ../../build

test::
	mvn test


mqtt_udp.jar::
	ant -f build.xml 

