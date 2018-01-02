MAKEFLAGS += --print-directory


default::
	@echo doing make all by default
	$(MAKE) all

all::
	# kernel
	$(MAKE) -C common/defs
	$(MAKE) -C c

clean::
	# kernel
	$(MAKE) -C common/defs clean
	$(MAKE) -C c clean

test::
	#$(MAKE) -C test
