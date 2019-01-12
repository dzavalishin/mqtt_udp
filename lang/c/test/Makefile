
SRC=regress_test.c
EXE_FILE=regress_test

all:: $(EXE_FILE)

test:: all
	./$(EXE_FILE)

build:: all

clean::
	-rm $(EXE_FILE) *.o


$(EXE_FILE): $(SRC) ../libmqttudp.a
	gcc -o $(EXE_FILE) $(SRC)  ../libmqttudp.a
