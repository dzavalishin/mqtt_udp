# Wireshark HOWTO

Use filter "udp.port==1883"

Add following lines to wireshark/init.lua (C:\Program Files\Wireshark\init.lua)

'''
    MQTTUDPPROTO_SCRIPT_PATH = "E:\\mqtt_udp\\lang\\lua\\" -- path to wireshark_mqtt_dissector.lua
    dofile(MQTTUDPPROTO_SCRIPT_PATH .. "wireshark_mqtt_dissector.lua")
'''

