#/bin/sh
#gawk -f preprocess.awk -v ver=0.55 conf.py.in  |grep version
# pass version number as 1st arg, subrelease as arg 2
gawk -f ../../common/bin/preprocess.awk -v ver=$1 rel=$1-$2 mqttudp-V.V-V.rockspec.in  >mqttudp-$1-$2.rockspec 
gawk -f ../../common/bin/preprocess.awk -v ver=$1 rel=$1-$2 make_dist.sh.in >make_dist.sh 
