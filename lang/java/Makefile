DEST=../../build

include makedefs 

all: # mqtt_udp.jar 
	mvn

clean:
	-rm -r bin/*

build:: all #mqtt_udp.jar 
#	cp mqtt_udp.jar $(DEST)
	mvn package
	cp target/*.jar $(DEST) 
	cp target/$(JAR_VER) $(DEST)/$(JAR_GEN)

test::
	mvn test


#mqtt_udp.jar::
#	ant -f build.xml 

