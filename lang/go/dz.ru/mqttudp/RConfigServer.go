/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2023 Dmitry Zavalishin, dz@dz.ru
 *
 * Remote configuration data provider
 *
 * Will reply to requests (subscribe packets) for given topics
 * sending back publish packets with data.
 *
 * @author dz
 *
 */

package mqttudp

import (
	"log"
)

var configSource map[string]string

func StartConfigServer() {
	AddPacketListener(configServer)
}

func AddConfigServerItem(topicName, topicValue string) {
	configSource[topicName] = topicValue
}

func configServer(pkt MqttPacket) error {
	if pkt.GetType() == SUBSCRIBE {
		topic := string(pkt.topic)
		log.Printf("configServer got request for %s\n", topic)

		value, ok := configSource[topic]
		if ok {
			Publish(topic, value)
		}
	}

	return nil
}

// Send all the config info we have -
// actually configure all the actual listeners
func ConfigServerSendAll() {
	for topic, value := range configSource {
		//fmt.Println("Topic:", topic, "Value:", value)
		Publish(topic, value)
	}
}
