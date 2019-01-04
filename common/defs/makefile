#.autodepend

FN=mqtt_udp_defs

IN=$(FN).definitions
OUT=$(FN).java $(FN).h $(FN).py $(FN).lua

goal:: $(OUT)

CPP=cpp -P -C
SENDTO=cp $@ 

ROOT=../../lang

clean:
	rm $(OUT)


$(FN).java: $(IN)  inc_txt2java/*
	$(CPP) -Iinc_txt2java $(IN) $@
	$(SENDTO) $(ROOT)/java/src/ru/dz/mqtt_udp/util/


$(FN).h: $(IN)  inc_txt2c/*
	$(CPP) -Iinc_txt2c $(IN) $@.1 
	sed 's/const unsigned char/#define/' $@.1 > $@.2
	sed 's/=/ /' $@.2 | sed 's/;/ /' > $@
	-rm $@.1 $@.2
	$(SENDTO) $(ROOT)/c/
	$(SENDTO) $(ROOT)/arduino/MQTT_UDP 

$(FN).py: $(IN)  inc_txt2python/*
	$(CPP) -Iinc_txt2python $(IN) $@.1
	sed 's/----/\#/' $@.1 > $@
	-rm $@.1
	$(SENDTO) $(ROOT)/python/mqttudp/
	$(SENDTO) $(ROOT)/python3/mqttudp/


$(FN).lua: $(IN)  inc_txt2lua/*
	$(CPP) -Iinc_txt2lua $(IN) $@
	$(SENDTO) $(ROOT)/lua/mqttudp/

