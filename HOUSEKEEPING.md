## Version

Package ver defined in common/defs/makefile and translated around

cd common/defs ; make version

## Dox

cd dox/source, edit index.rst 
cd dox ; makedox.sh 

Diagrams @ https://www.websequencediagrams.com/

## Doxygen

In progress

## JavaDoc

cd lang/java ; mvn package

or

cd lang/java ; make build


## Regress tests

*     test/runner/testmain.py - runs PUBLISH sender and receiver in all 4 languages checking that all pairs send/receive ok
