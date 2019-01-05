-- wrapper around BitOp module
-- copied (thanx!) from https://github.com/xHasKx/luamqtt/blob/master/mqtt/bit.lua

if _VERSION == "Lua 5.1" or _VERSION == "Lua 5.2" or type(jit) == "table" then
	return require("bit")
else
	return require("mqttudp.bit53")
end

