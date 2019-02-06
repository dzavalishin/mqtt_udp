#!/usr/bin/lua

package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install

--[[

Passive remote config sketch

]]

--local json = require "luajson"
local json = require "json"
local mq = require "mqttudp"

local filename = "lua_rconfig.json"

local MY_ID = "000000000000" -- TODO make me

-- by topic part
local conf_items = 
{
    ["info/soft"]		= { "Lua example" },
    ["info/ver"]		= { "0.0" },
    ["info/uptime"]		= { "?" },

    ["node/name"]		= { "Unnamed" },
    ["node/location"]	= { "Nowhere" },
}


local save_all = function()
	-- todo remove info/* from r/w, recreate
	local j = json.encode(conf_items)
	--print( j )
	
	local f = assert(io.open(filename, "w"))
    f:write(j) -- todo err check
    f:close()
end

local load_all = function()
	-- todo remove info/* from r/w, recreate
	--local j = json.encode(conf_items)
	--print( j )
	
	local f = assert(io.open(filename, "r"))
    --local t = f:read(j)
    local t = f:read()
    f:close()

	local newi = json.decode( t )
	conf_items = newi

end




local full_topic = function( topic )
	return "$SYS/conf/"..MY_ID.."/"..topic
end



local send_one_item = function( k, v )
	mq.send_publish( full_topic(k), v[1] );
end

local recv_one_item = function( k, v, topic, value )
	if full_topic(k) == topic then
		v[1] = value
		print( "Got "..k.." = '"..value.."'" )
		-- TODO call user hook
		save_all() -- TODO TEMP, kill me
	end
end


local send_all_rconf_items = function()
	--print("Will send all items")
	for k, v in pairs( conf_items ) do
		print( "Send "..k, v[1] )
		--print( k, v )
		send_one_item( k, v )
	end
end

local send_asked_rconf_items = function(topic)

	for k, v in pairs( conf_items ) do
		if mq.match( topic, full_topic(k) ) then
			print( "Send "..k, v[1] )
			--print( k, v )
			send_one_item( k, v )
		end
	end
end


-- TODO defs.SYS_CONF_PREFIX

local on_subscribe = function( topic )
	--[[ TODO vice versa?
	if mq.match( "$SYS/conf/#", topic ) then
		send_all_rconf_items()
		return
	end ]]

	--- per topic TODO
	send_asked_rconf_items( topic )

end

local on_publish = function( topic, value )
	for k, v in pairs( conf_items ) do
		--print( k, v[1] )
		--print( k, v )
		recv_one_item( k, v, topic, value )
	end
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
load_all();

mq.listen( listener )

