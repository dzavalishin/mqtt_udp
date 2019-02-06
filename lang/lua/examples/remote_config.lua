#!/usr/bin/lua

package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install

--[[

Passive remote config sketch

]]

local mq = require "mqttudp"



local MY_ID = "000000000000" -- TODO make me

-- by topic part
local conf_items = 
{
    ["info/soft"]		= { "Lua example" },
    ["info/ver"]		= { "0.0" },
    ["info/uptime"]		= { "?" },

    ["node/name"]		= { "Unnamed" },
    ["node/locarion"]	= { "Nowhere" },
}


local full_topic = function( topic )
	return "$SYS/conf/"..MY_ID.."/"..topic
end



local send_one_item = function( k, v )
	mq.send_publish( full_topic(k), v[1] );
end


local send_all_rconf_items = function()
	print("Will send all items")
	for k, v in pairs( conf_items ) do
		print( k, v[1] )
		--print( k, v )
		send_one_item( k, v )
	end
end

-- TODO defs.SYS_CONF_PREFIX

local on_subscribe = function( topic )
	if mq.match( "$SYS/conf/#", topic ) then
		send_all_rconf_items()
	end
end

local on_publish = function( topic, value )
end


local listener = function( ptype, topic, value, ip, port )
    if ptype == "publish" then
        print("pub '"..topic.."' = '"..val.."'".."	from: ", ip, port)
        on_publish( topic, value )
    end

    if ptype == "subscribe" then
        print("sub '"..topic.."'")
        on_subscribe( topic )
    end
end


print("Will listen for remote config");

--send_all_rconf_items()

mq.listen( listener )

