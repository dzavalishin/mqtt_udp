#!/usr/bin/lua

package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install

--[[

Passive remote config example

]]

local rconf = require "remote_config"



-- indexed by topic part
local conf_items = 
{
    ["info/soft"]		= { "Lua example" },
    ["info/ver"]		= { "0.0" },
    ["info/uptime"]		= { "?" },

    ["node/name"]		= { "Unnamed" },
    ["node/location"]	= { "Nowhere" },

    ["topic/test"]  	= { "test" },
}


rconf.init(conf_items)

print("Will listen for remote config");
rconf.mq.listen( rconf.listener )

--publish_for("test","test")
