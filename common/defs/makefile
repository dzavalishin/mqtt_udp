#.autodepend

FN=mqtt_udp_defs

OUT=$(FN).java $(FN).h $(FN).py

goal:: $(OUT)

CPP=cpp -P -C
#CPP=cpp32 -q 

clean:
	rm $(OUT)

$(FN).java: $(FN).definitions  inc_txt2java/*
	$(CPP) -Iinc_txt2java $(FN).definitions $@
#	cp $@ $(realpath $(PHANTOM_HOME)/tools/pdb/src/phantom/code/opcode_ids.java)
#	cp $@ $(realpath $(PHANTOM_HOME)/tools/plc/src/phantom/code/opcode_ids.java)
#	cp $@ $(realpath $(PHANTOM_HOME)/plc/src/phantom/code/opcode_ids.java)


$(FN).h: $(FN).definitions  inc_txt2c/*
	$(CPP) -Iinc_txt2c $(FN).definitions $@.1 
	sed 's/const unsigned char/#define/' $@.1 > $@.2
	sed 's/=/ /' $@.2 | sed 's/;/ /' > $@
	-rm $@.1 $@.2


$(FN).py: $(FN).definitions  inc_txt2python/*
	$(CPP) -Iinc_txt2python $(FN).definitions $@.1
	sed 's/----/\#/' $@.1 > $@
	-rm $@.1

