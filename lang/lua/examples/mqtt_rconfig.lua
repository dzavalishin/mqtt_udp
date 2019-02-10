#!/usr/bin/lua

package.path = "../?/init.lua;../?.lua;" .. package.path  -- let us test without lib install

--[[

Passive remote config example

]]

local rconf = require "remote_config"


-- configurable items
-- indexed by topic part
local conf_items = 
{
	-- read only
    ["info/soft"]		= { "Lua example" },
    ["info/ver"]		= { "0.0" },
    ["info/uptime"]		= { "?" },

	-- common instance info
    ["node/name"]		= { "Unnamed" },
    ["node/location"]	= { "Nowhere" },

	-- items we want to send out.
	-- remote configuration must tell
	-- us which topics to use
    ["topic/test"]  	= { "test" },
    ["topic/ai0"]  		= { "unnamed_ai0" },
    ["topic/di0"]	  	= { "unnamed_di0" },

    ["topic/pwm0"]  	= { "unnamed_pwm0" },
}


rconf.init(conf_items)

print("Will listen for remote config");
rconf.mq.listen( rconf.listener )

-- look items above for item with key "topic/test", 
-- get configured topic name and use it to publish
--
-- rconf.publish_for("test","test_data") 

-- look items above for item with key "topic/pwm0", 
-- get configured topic name and chech if incoming_topic
-- is equal
--
-- rconf.is_for ( "pwm0", incoming_topic )

