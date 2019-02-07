#!/usr/bin/lua

package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install

--[[

Passive remote config sketch - experimental

]]

local rconf = {}

--local json = require "luajson"
local json = require "json"
rconf.mq = require "mqttudp"

local filename = "lua_rconfig.json"

local MY_ID = "000000000000" -- TODO make me

conf_items = {}


local save_all = function()
	local j = json.encode(conf_items)
	--print( j )
	
	local f = assert(io.open(filename, "w"))
    f:write(j) -- todo err check
    f:close()
end

local load_all = function()
	local f = io.open(filename, "r")
	if f == nil then
		return
	end
    local t = f:read()
    f:close()

	local newi = json.decode( t )
	conf_items = newi

end




local full_topic = function( topic )
	--return "$SYS/conf/"..MY_ID.."/"..topic
	return rconf.mq.defs.SYS_CONF_PREFIX.."/"..MY_ID.."/"..topic
end



local send_one_item = function( k, v )
	rconf.mq.send_publish( full_topic(k), v[1] );
end

local recv_one_item = function( k, v, topic, value )
	if full_topic(k) == topic then
		v[1] = value
		print( "Got "..k.." = '"..value.."'" )
		-- TODO call user hook
		save_all() -- TODO TEMP, kill me
	end
end


local send_asked_rconf_items = function(topic)
	for k, v in pairs( conf_items ) do
		if rconf.mq.match( topic, full_topic(k) ) then
			print( "Send "..k, v[1] )
			--print( k, v )
			send_one_item( k, v )
		end
	end
end



local on_publish = function( topic, value )
	for k, v in pairs( conf_items ) do
		recv_one_item( k, v, topic, value )
	end
end


rconf.listener = function( ptype, topic, value, ip, port )
    if ptype == "publish" then
        --print("pub '"..topic.."' = '"..val.."'".."	from: ", ip, port)
        on_publish( topic, value )
    end

    if ptype == "subscribe" then
        print("sub '"..topic.."'")
        -- on_subscribe( topic )
		send_asked_rconf_items( topic )
    end
end



--send_all_rconf_items()


rconf.init = function( init_items )

	-- Load all, then insert absent and r/o ones from init
	load_all();

	for k, v in pairs( init_items ) do
		print( "Init "..k, v[1] )
		--print( k, v )
		if conf_items[k] == nil then
			conf_items[k] = v
			print( "Set "..k, v[1] )
		else
			--print( "k='"..k.."'" )
			--print( "k="..type(k) )
			if k:sub(1, 4) == "info" then
				conf_items[k] = v
				print( "Set info "..k, v[1] )
			end
		end
	end
end

-- Send message using configurable topic
--
--Get value of "$SYS/conf/{MY_ID}/topic_of_topic" and use it as topic to send data
--
-- @param #string topic_of_topic name of parameter holding topic used to send message
-- @param #string data data to send
--
rconf.publish_for = function( topic_of_topic, data )
	local key = "topic/"..topic_of_topic

	if conf_items[key] == nil then
		print( "no configured value (topic) for topic_of topic() "..k.."'" )
		return
	end

	item = conf_items[key]

	rconf.mq.send_publish( item[1], data )
end

-- true if value for topic_of_topics == topic
-- test incoming message topic to be for this configurable
rconf.is_for = function( topic_of_topic, topic )
	local key = "topic/"..topic_of_topic

	if conf_items[key] == nil then
		print( "no configured value (topic) for topic_of topic() "..k.."'" )
		return false
	end

	item = conf_items[key]

	return topic == item[1]
end


rconf.set_on_config = function( callback )
	rconf.on_config = callback
end

return rconf
