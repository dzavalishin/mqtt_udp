
default::
	@echo doing make all by default
	$(MAKE) all


# build - make all build files
build:: all

TOPTARGETS := all clean test build

ALLDIRS := $(wildcard */.)

# TODO arduino

# TODO wrong! we have to exec build there!

#EXCLUDE_DIRS := codesys/. kotlin/. arduino/. c/. lua/. python/. python3/.
EXCLUDE_DIRS := codesys/. kotlin/. arduino/. c/. 

SUBDIRS := $(filter-out $(EXCLUDE_DIRS), $(ALLDIRS))

$(TOPTARGETS):: $(SUBDIRS) c/.

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
	
c/.:: c/Makefile
	$(MAKE) -C $@ $(MAKECMDGOALS)
	

c/Makefile: c/Makefile.in
	cd c ; sh configure
