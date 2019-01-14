
default::
	@echo doing make all by default
	$(MAKE) all


# build - make all build files
build:: all

TOPTARGETS := all clean test build

ALLDIRS := $(wildcard */.)


EXCLUDE_DIRS := 

SUBDIRS := $(filter-out $(EXCLUDE_DIRS), $(ALLDIRS))

$(TOPTARGETS):: $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
	
