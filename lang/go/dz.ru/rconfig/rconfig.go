/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2023 Dmitry Zavalishin, dz@dz.ru
 *
 * Example program: Remote configuration
 *
**/

package main

import (
	"fmt"
	"time"

	"dz.ru/mqttudp"
)

type myServer struct {
}

func main() {

	fmt.Println("Demo of MQTT/UDP passive remote configuration")

	init_rconfig()

	var s myServer

	mqttudp.SubServer(s)

	for {
		//fmt.Println("Send MQTT/UDP packet")
		//mqttudp.Publish("fromGoLang", "hello world")
		time.Sleep(2 * time.Second)
	}

	time.Sleep(2 * time.Hour)

}

func (s myServer) Accept(packet mqttudp.MqttPacket) {
	//fmt.Println("got pkt ", packet)
	packet.Dump()
}

/*int main(int argc, char *argv[])
{
    printf("\n\n");

    //setvbuf( stdout, 0, _IONBF, 0 );

    init_rconfig();

    while(1)
    {
        // We need to start listen loop: remote config takes input from it
        int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );
        if( rc ) {
            mqtt_udp_global_error_handler( MQ_Err_Other, rc, "recv_loop error", 0 );
        }
    }

    return 0;
} */

// Actual remotely configurable items
// We use .opaque to keep initial value
var rconfig_list = []mqttudp.RConfigItem{
	mqttudp.NewTopicRConfigItem("Switch 1 topic", "topic/sw1", "", "sw1"),
	mqttudp.NewTopicRConfigItem("Switch 2 topic", "topic/sw2", "", "sw2"),
	mqttudp.NewTopicRConfigItem("Switch 3 topic", "topic/sw3", "", "sw3"),
	mqttudp.NewTopicRConfigItem("Switch 4 topic", "topic/sw4", "", "sw4"),
	mqttudp.NewTopicRConfigItem("Di 0 topic", "topic/di0", "", "di0"),

	mqttudp.NewTopicRConfigItem("Di 1 topic", "topic/di1", "", "di1"),

	mqttudp.NewOtherRConfigItem("MAC address", "net/mac", "", "020000000000"),

	mqttudp.NewInfoRConfigItem("Switch 4 topic", "info/soft", "", "C RConfig Demo"),
	mqttudp.NewInfoRConfigItem("Switch 4 topic", "info/ver", "", "0.0.1"),
	mqttudp.NewInfoRConfigItem("Switch 4 topic", "info/uptime", "", "0d 00:00:00"), // DO NON MOVE OR ADD LINES ABOVE, inited by array index below

	mqttudp.NewOtherRConfigItem("Name", "node/name", "", "C Test Node"),  // TODO R/W
	mqttudp.NewOtherRConfigItem("Location", "node/location", "", "None"), // TODO R/W

}

func init_rconfig() {

	rconfig_list[8].SetStringValue("00:00:00")
	rconfig_list[9].SetStringValue("?")

	rc := mqttudp.RConfigClientInit(rconfig_rw_callback, rconfig_list)
	if rc != nil {
		fmt.Printf("rconfig init failed, %s\n", rc.Error())
	}
}

// -----------------------------------------------------------------------
//
// Callback to connect to host code
//
// -----------------------------------------------------------------------

// Must read from local storage or write to local storage
// config item at pos
func rconfig_rw_callback(pos int, write bool) {

	var op string = "load"
	if write {
		op = "save"
	}
	fmt.Printf("asked to %s item %d\n", op, pos)

	if (pos < 0) || (pos >= cap(rconfig_list)) {
		return // -1
	} // TODO error

	i := rconfig_list[pos]

	if i.GetType() != mqttudp.MQ_CFG_TYPE_STRING {
		return // -2
	}

	if i.GetKind() == mqttudp.MQ_CFG_KIND_INFO {
		return // -3
	}

	if write {
		// RConfig got new setting from outside. Store and use it.
		fmt.Printf("Got new settings for %s = '%s'\n",
			i.GetTopic(),
			i.GetStringValue())
	} else {
		// RConfig asks saved or default setting for item
		if len(i.Opaque) != 0 {
			fmt.Printf("RCONF will set item %d to %s\n", pos, i.Opaque)
			mqttudp.RConfigSetString(pos, i.Opaque)
		}
	}

}
