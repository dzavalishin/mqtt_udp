

BUILD_FILES = ConfigServer.jar config_server.sh

all::
	ant -f makeJava.xml 
	ant -f makeJar.xml 
#	launch4jc launch4j.xml # it uses external JAR, don't need to make

clean:
	echo -------------------- TODO ---------------------
#	-rm -r bin/*

build:: all
	cp $(BUILD_FILES)  ../../build

test:: 
	echo -------------------- TODO ---------------------



