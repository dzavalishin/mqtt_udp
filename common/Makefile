MAKEFLAGS += --print-directory
#MAKE=make # TODO configure

default::
	@echo doing make all by default
	$(MAKE) all

TOPTARGETS := all clean test build

ALLDIRS := $(wildcard */.)

EXCLUDE_DIRS := bin/.

SUBDIRS := $(filter-out $(EXCLUDE_DIRS), $(ALLDIRS))
#SUBDIRS := $(filter-out $(ALLDIRS), $(EXCLUDE_DIRS))

$(TOPTARGETS): $(SUBDIRS)

$(SUBDIRS):
	echo $(SUBDIRS)
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
	