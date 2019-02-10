#!/usr/bin/env python3

'''
must be configured, each suite is set of programs to be run in given order and assumed output for them

test suite 1 is pub/sub for all leanguages we can run
test suite 2 is conf_test.py 

'''

import subprocess
import threading

PY_PATH = "../../lang/python3/test/"
C_PATH = "../../lang/c"
LUA_PATH = "../../lang/lua/test"
JAVA_PATH = "../../lang/java"

#args=[ "python3.6", "test_pub.py", "aaa", "bbb" ]
##done = subprocess.run( args, capture_output=True, cwd=python_path, timeout=100, check=True )
#done = subprocess.run( args, cwd=python_path, stdout=subprocess.PIPE, timeout=100, check=True )
#print( "Done, out = "+str(done.stdout) );



def run_wait( wd, args, timeout=100 ):
    done = subprocess.run( args, cwd=wd, stdout=subprocess.PIPE, timeout=100, check=True )
    return str(done.stdout)

def run_py( prog, a1, a2, timeout=100 ):
    args=[ "python3.6", prog, a1, a2 ]
    return run_wait( PY_PATH, args, timeout )

def run_c( prog, a1, a2, timeout=100 ):
    #args=[ "cmd", "/c", prog, a1, a2 ]
    args=[ "./"+prog, a1, a2 ]
    return run_wait( C_PATH, args, timeout )

def run_lua( prog, a1, a2, timeout=100 ):
    #args=[ "cmd", "/c", prog, a1, a2 ]
    args=[ "lua", prog, a1, a2 ]
    return run_wait( LUA_PATH, args, timeout )

def run_java( prog, a1, a2, timeout=100 ):
    #args=[ "cmd", "/c", "java", "-cp", "target/mqtt_udp-0.4.1.jar", prog, a1, a2 ]
    args=[ "java", "-cp", "target/mqtt_udp-0.4.1.jar", prog, a1, a2 ]
    return run_wait( JAVA_PATH, args, timeout )


class Waiter(object):
    def __init__(self, topic, value):
        self.topic = topic
        self.value = value
        self.result = "__ERROR__"

    def start(self):
        self.th = threading.Thread(target=self.__run, args=(run_c,"mqtt_udp_waitmsg"))
        self.th.start()

    def wait(self):
        self.th.join()
        return self.result

    def __run(self,runner,prog):
        self.result = runner( prog, self.topic, self.value, timeout=100 )



if __name__ == "__main__":
    print( "Will do MQTT/UDP program run tests" )
    print(run_py( "test_pub.py", "regress/from/python", "test_message1" ))
    print(run_c( "mqtt_udp_pub", "regress/from/c", "test_message2" ))
    print(run_lua( "test_pub.lua", "regress/from/lua", "test_message3" ))
    print(run_java( "ru.dz.mqtt_udp.util.Pub", "regress/from/java", "test_message4" ))

    # Runs waiter which listens for given topic/value, then
    # runs talker which PUBLISHes same topic/value, then waits
    # for waiter to complete
    w = Waiter("regress/from/python", "test_message1")
    w.start()
    print(run_py( "test_pub.py", "regress/from/python", "test_message1" ))
    print(w.wait())

    w = Waiter("regress/from/c", "test_message2")
    w.start()
    print(run_c( "mqtt_udp_pub", "regress/from/c", "test_message2" ))
    print(w.wait())

    w = Waiter("regress/from/lua", "test_message3")
    w.start()
    print(run_lua( "test_pub.lua", "regress/from/lua", "test_message3" ))
    print(w.wait())

    w = Waiter("regress/from/java", "test_message4")
    w.start()
    print(run_java( "ru.dz.mqtt_udp.util.Pub", "regress/from/java", "test_message4" ))
    print(w.wait())

