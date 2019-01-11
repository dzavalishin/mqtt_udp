all: test

clean:
	-rm *.o

build::
	echo nothing to do

test: *.c
	gcc -o test test.c sha256.c md5.c hmac.c  
