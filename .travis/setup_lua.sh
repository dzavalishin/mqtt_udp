#! /bin/bash

# A script for setting up environment for travis-ci testing.
# Sets up Lua and Luarocks.

source .travis/platform.sh

curl http://www.lua.org/ftp/lua-5.3.0.tar.gz | tar xz
cd lua-5.3.0;
sudo make $PLATFORM install;
