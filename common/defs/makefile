#.autodepend

FN=mqtt_udp_defs

OUT=$(FN).java $(FN).h $(FN).py $(FN).lua

goal:: $(OUT)

CPP=cpp -P -C
SENDTO=cp $@ 

clean:
	rm $(OUT)


$(FN).java: $(FN).definitions  inc_txt2java/*
	$(CPP) -Iinc_txt2java $(FN).definitions $@
	$(SENDTO) ../../java/src/ru/dz/mqtt_udp/util/


$(FN).h: $(FN).definitions  inc_txt2c/*
	$(CPP) -Iinc_txt2c $(FN).definitions $@.1 
	sed 's/const unsigned char/#define/' $@.1 > $@.2
	sed 's/=/ /' $@.2 | sed 's/;/ /' > $@
	-rm $@.1 $@.2
	$(SENDTO) ../../c/


$(FN).py: $(FN).definitions  inc_txt2python/*
	$(CPP) -Iinc_txt2python $(FN).definitions $@.1
	sed 's/----/\#/' $@.1 > $@
	-rm $@.1
	$(SENDTO) ../../python/mqttudp/


$(FN).lua: $(FN).definitions  inc_txt2lua/*
	$(CPP) -Iinc_txt2lua $(FN).definitions $@
	$(SENDTO) ../../lua/

