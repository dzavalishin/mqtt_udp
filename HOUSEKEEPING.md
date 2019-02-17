## Version

Package ver defined in common/defs/makefile and translated around

cd common/defs ; make version

## Dox

cd dox/source, edit index.rst 
cd dox ; makedox.sh 

Diagrams @ <https://www.websequencediagrams.com/>

## Doxygen

In progress

## JavaDoc

cd lang/java ; mvn package

or

cd lang/java ; make build

## Versions

Python 3.6
Lua 5.1.5


## Ubuntu installs needed

```
apt install gcc
apt install make
apt install default-jdk
apt install maven
apt install python3.6
apt install python3-pip
pip3 install sphinx
apt install lua5.1
apt install luarocks
luarocks install luabitop
luarocks install luasocket
```


## Regress tests

*     test/runner/testmain.py - runs PUBLISH sender and receiver in all 4 languages checking that all pairs send/receive ok


## Work in progress 

### Conan C package

<https://docs.conan.io/en/latest/creating_packages.html>

<https://bintray.com/dzavalishin/MQTT-UDP>

### cloudmqtt.com

<https://api.cloudmqtt.com/console/82596046/settings>
