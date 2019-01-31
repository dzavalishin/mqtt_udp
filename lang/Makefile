
default::
	@echo doing make all by default
	$(MAKE) all


# build - make all build files
build:: all

TOPTARGETS := all clean test build

ALLDIRS := $(wildcard */.)

# TODO arduino

# TODO wrong! we have to exec build there!

AC_MAKEFILE=c/arch/default/Makefile.arch
AC_CONFIG=c/arch/default/config.h


#EXCLUDE_DIRS := codesys/. kotlin/. arduino/. c/. lua/. python/. python3/.
EXCLUDE_DIRS := codesys/. kotlin/. arduino/. c/. 

SUBDIRS := $(filter-out $(EXCLUDE_DIRS), $(ALLDIRS)) 
#c/test

$(TOPTARGETS):: $(SUBDIRS) c/. 

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
	
c/.:: c/arch/default/Makefile.arch c/arch/default/config.h
	$(MAKE) -C $@ $(MAKECMDGOALS)
	

c/arch/default/Makefile.arch c/arch/default/config.h: c/arch/default/Makefile.arch.in
	cd c ; sh configure
 