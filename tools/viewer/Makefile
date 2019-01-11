

BUILD_FILES = MqttUdpViewer.jar mqtt_udp_view mqtt_udp_view.cmd MqttUdpViewer.exe

all::
	ant -f makeJar.xml 
#	launch4jc launch4j.xml # it uses external JAR, don't need to make

clean:
	-rm -r bin/*

build:: all
	cp $(BUILD_FILES)  ../../build

test::
	echo -------------------- TODO ---------------------



