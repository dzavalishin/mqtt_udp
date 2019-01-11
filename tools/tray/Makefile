BUILD_FILES=MqttUdpTray.jar MqttUdpTray.exe mqttudptray.ini 


all::
	ant -f makeJar.xml 
#	launch4jc launch4j.xml # it uses external JAR, don't need to make

clean:
	-rm -r bin/*

build:: all
	cp $(BUILD_FILES)  ../../build

test::
	echo -------------------- TODO ---------------------



