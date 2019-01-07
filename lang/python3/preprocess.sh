#/bin/sh
#gawk -f preprocess.awk -v ver=0.55 conf.py.in  |grep version
# pass version number as 1st arg, subrelease as arg 2
gawk -f ../../common/bin/preprocess.awk -v ver=$1 rel=$1-$2 setup.py.in  >setup.py
