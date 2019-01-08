#/bin/sh
#
# This is an example file, customize and put to target dir, then add to common/defs/makefile
#
# pass version number as 1st arg, subrelease as arg 2
#gawk -f ../../common/bin/preprocess.awk -v ver=$1 rel=$1-$2 setup.py.in  >setup.py
