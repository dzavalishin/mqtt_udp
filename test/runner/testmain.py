#!/usr/bin/env python3

'''


'''

import subprocess
import threading
import time
import sys

PY_PATH = "../../lang/python3/test/"
C_PATH = "../../lang/c"
LUA_PATH = "../../lang/lua/test"
#JAVA_PATH = "../../lang/java"
JAVA_PATH = "../../build"

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
    #args=[ "java", "-cp", "target/mqtt_udp-0.5.0.jar", prog, a1, a2 ]
    args=[ "java", "-cp", "mqtt_udp.jar", prog, a1, a2 ]
    return run_wait( JAVA_PATH, args, timeout )

# exec func, topic, msg, pub prog, wait prog
defs = {
    "py"    :   [ run_py,   "regress/from/python", "test_message1", "test_pub.py",             "test_wait.py" ],
    "c"     :   [ run_c,    "regress/from/c",      "test_message2", "mqtt_udp_pub",            "mqtt_udp_waitmsg" ],
    "lua"   :   [ run_lua,  "regress/from/lua",    "test_message3", "test_pub.lua",            "test_wait.lua" ],
    "java"  :   [ run_java, "regress/from/java",   "test_message4", "ru.dz.mqtt_udp.util.Pub", "ru.dz.mqtt_udp.util.Wait" ],
}


class Waiter(object):
    def __init__(self, send_lang, recv_lang, cmd_parameters ):
        sender = defs[send_lang]
        recvr = defs[recv_lang]
        print("\n---- From "+send_lang+" to "+recv_lang+"")
        self.send_func = sender[0]
        self.topic = sender[1]
        self.value = sender[2]
        self.send_exe = sender[3]

        self.recv_func = recvr[0]
        self.recv_exe = recvr[4]
        self.result = "__ERROR__"

    def start(self):
        #print("recv with "+str(self.recv_func)+" exe "+self.recv_exe)
        self.th = threading.Thread(target=self.__run, args=(self.recv_func,self.recv_exe))
        self.th.start()
        #print("thread started")

    def send(self):
        #print("send with "+str(self.send_func)+" exe "+self.send_exe)
        return self.send_func( self.send_exe, self.topic, self.value )

    def wait(self):
        self.th.join()
        return self.result

    def test(self):
        self.start()
        time.sleep(1.5) # give thread some time to start program. TODO: Need better way to sync!
        print(self.send())
        rc = self.wait()
        print(rc)
        if rc == "__ERROR__":
            print("\nFAILED\n")
            sys.exit(1)


    def __run(self,runner,prog):
        self.result = runner( prog, self.topic, self.value, timeout=100 )



def runAll(param):
    # Runs waiter which listens for given topic/value, then
    # runs talker which PUBLISHes same topic/value, then waits
    # for waiter to complete
    Waiter("py",   "lua", 	param).test()
    Waiter("c",    "lua", 	param).test()
    Waiter("lua",  "lua", 	param).test()
    Waiter("java", "lua", 	param).test()

    Waiter("py",   "py", 	param).test()
    Waiter("c",    "py", 	param).test()
    Waiter("lua",  "py", 	param).test()
    Waiter("java", "py", 	param).test()

    Waiter("py",   "c", 	param).test()
    Waiter("c",    "c", 	param).test()
    Waiter("lua",  "c", 	param).test()
    Waiter("java", "c", 	param).test()

    Waiter("py",   "java", 	param).test()
    Waiter("c",    "java", 	param).test()
    Waiter("lua",  "java", 	param).test()
    Waiter("java", "java", 	param).test()


if __name__ == "__main__":
    print( "Will do MQTT/UDP program run tests" )
    #print(run_py( "test_pub.py", "regress/from/python", "test_message1" ))

    runAll("")
    #runAll("-s signPassword")

    print("\n ------ All tests PASSED!")

    # CI checks our exit code
    sys.exit(0)

