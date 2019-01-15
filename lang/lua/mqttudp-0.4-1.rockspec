package = "mqttudp"


version = "0.4-1"

source = {
   file = "lua_mqttudp-0.4-1.tar.gz",
   ---dir = "mqttudp",
   dir = ".",
   url = "https://github.com/dzavalishin/mqtt_udp/blob/master/lang/lua/lua_mqttudp-0.4-1.tar.gz?raw=true",
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
        mqttudp = "mqttudp/init.lua",
        ["mqttudp.mqtt_proto_lib"]       = "mqttudp/mqtt_proto_lib.lua",
        ["mqttudp.mqtt_udp_lib"]         = "mqttudp/mqtt_udp_lib.lua",
        ["mqttudp.mqtt_udp_defs"]        = "mqttudp/mqtt_udp_defs.lua",
        ["mqttudp.mqtt_udp_lib_NodeMCU"] = "mqttudp/mqtt_udp_lib_NodeMCU.lua",
        ["mqttudp.bit53"]                = "mqttudp/bit53.lua",
        ["mqttudp.mybit"]                = "mqttudp/mybit.lua",
    }
}
