#/bin/sh
# pass version number as 1st arg, subrelease as arg 2
gawk -f ../../common/bin/preprocess.awk -v ver=$1 rel=$1.$2   makeJava.xml.in   >makeJava.xml
