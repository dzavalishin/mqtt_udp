#.autodepend

FN=mqtt_udp_defs

IN=$(FN).definitions
OUT= conv_version  $(FN).java $(FN).h $(FN).py $(FN).lua

VER_MAJOR=0
VER_MINOR=5
# unused yet
SUB_MINOR=0


default::
	@echo make version

all::
	@echo nothing to do

version:: preprocessors $(OUT) 

CPP=cpp -P -C
SENDTO=cp $@ 

ROOT=../../lang

clean::
	rm $(OUT)

test::
	@echo no test here

build::
	@echo nothing to build here

conv_version::
	echo "" > conv_version 
	echo "id(PACKAGE_VERSION_MAJOR,$(VER_MAJOR))" >> conv_version 
	echo "id(PACKAGE_VERSION_MINOR,$(VER_MINOR))" >> conv_version 
	echo "$(VER_MAJOR).$(VER_MINOR).$(SUB_MINOR)" > version 


$(FN).java: $(IN)  inc_txt2java/* makefile
	$(CPP) -Iinc_txt2java $(IN) $@
	$(SENDTO) $(ROOT)/java/src/main/java/ru/dz/mqtt_udp/util/


$(FN).h: $(IN)  inc_txt2c/* makefile
	$(CPP) -Iinc_txt2c $(IN) $@.1 
	sed 's/const unsigned char/#define/' $@.1 > $@.2
	sed 's/=/ /' $@.2 | sed 's/;/ /' > $@
	-rm $@.1 $@.2
	$(SENDTO) $(ROOT)/c/
	$(SENDTO) $(ROOT)/arduino/MQTT_UDP 

$(FN).py: $(IN)  inc_txt2python/* makefile
	$(CPP) -Iinc_txt2python $(IN) $@.1
	sed 's/----/\#/' $@.1 > $@
	-rm $@.1
	$(SENDTO) $(ROOT)/python/mqttudp/
	$(SENDTO) $(ROOT)/python3/mqttudp/


$(FN).lua: $(IN)  inc_txt2lua/* makefile
	$(CPP) -Iinc_txt2lua $(IN) $@
	$(SENDTO) $(ROOT)/lua/mqttudp/


preprocessors:: 
	cd ../../dox/source          ; sh preprocess.sh "$(VER_MAJOR).$(VER_MINOR)" "$(SUB_MINOR)" 
	cd ../../lang/lua            ; sh preprocess.sh "$(VER_MAJOR).$(VER_MINOR)" "$(SUB_MINOR)" 
	cd ../../lang/python3        ; sh preprocess.sh "$(VER_MAJOR).$(VER_MINOR)" "$(SUB_MINOR)" 
	cd ../../lang/java           ; sh preprocess.sh "$(VER_MAJOR).$(VER_MINOR)" "$(SUB_MINOR)" 
	cd ../../tools/config_server ; sh preprocess.sh "$(VER_MAJOR).$(VER_MINOR)" "$(SUB_MINOR)" 

