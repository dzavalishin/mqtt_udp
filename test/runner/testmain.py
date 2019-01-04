#!/bin/python


'''
must be configured, each suite is set of programs to be run in given order and assumed output for them

test suite 1 is pub/sub for all leanguages we can run
test suite 2 is conf_test.py 

'''

import subprocess

python_path = "../../lang/python3/examples/"

#args=["sh", "-c", "python3.6", python_path+"random_to_udp.py"]
#args=["python3.6", python_path+"random_to_udp.py"]
#args=["python3.6", "random_to_udp.py"]
args=[ "python3.6", "pub.py", "aaa", "bbb" ]

proc = subprocess.Popen(args, stdout=subprocess.PIPE, cwd=python_path )

try:
    (out, _) = proc.communicate(timeout=10)
#    out = out.strip()
    print( "Done, out = "+str(out) );
except subprocess.TimeoutExpired as e:
    print( "Died, err = " + str( e ) );
finally:
    proc.terminate()

