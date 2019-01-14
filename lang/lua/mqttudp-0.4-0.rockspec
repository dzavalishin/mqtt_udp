-- TODO not ok
-- learn build from https://github.com/xHasKx/luamqtt/blob/master/luamqtt-1.4.2-1.rockspec

package = "mqttudp"


-- version = "0.4"
version = "0.4-0"

source = {
   file = "lua_mqttudp-0.4-0.tar.gz",
   dir = "mqttudp",
   url = "https://github.com/dzavalishin/mqtt_udp",
}

description = {
   summary = "MQTT/UDP implementation in Lua.",
   detailed = [[
      MQTT/UDP is simplest possible UDP broadcast based IoT protocol.
      Lua implementtaion is very basic and is intended to be uses mostly in
      IoT devices. See homepage for more info and other languages.
   ]],
   homepage = "https://github.com/dzavalishin/mqtt_udp", -- All the MQTT/UDP project
   license = "MIT/X11",
}

dependencies = {
   "lua >= 5.1, < 5.4"
   -- If you depend on other rocks, add them here
}

build = {
    type = "builtin",
    modules = {
        proto   = "mqttudp/mqtt_proto_lib.lua",
        udp     = "mqttudp/mqtt_udp_lib.lua",
        defs    = "mqttudp/mqtt_udp_defs.lua",
        node    = "mqttudp/mqtt_udp_lib_NodeMCU.lua",
        bit53   = "mqttudp/bit53.lua",
        mybit   = "mqttudp/mybit.lua",
    }
}
