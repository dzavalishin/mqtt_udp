MAKEFLAGS += --print-directory
MAKE=make # TODO configure

default::
	@echo doing make all by default
	$(MAKE) all

#travis_ci:: all test
#	@echo 
#	@echo --------
#	@echo All done



# version - propagate version number into all the files than need it
version::
	$(MAKE) -C common/defs $@

# build - make all build files
build:: all

TOPTARGETS := all clean test build

ALLDIRS := $(wildcard */.)

EXCLUDE_DIRS := build/. docs/.

SUBDIRS := $(filter-out $(EXCLUDE_DIRS), $(ALLDIRS))

$(TOPTARGETS):: $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)

test/.: lang/.

tools/.: lang/.
	