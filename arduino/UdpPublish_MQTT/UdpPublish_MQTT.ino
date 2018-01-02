#include <mqtt_udp.h>
#include <UIPEthernet.h>

EthernetUDP udp;


void setup() {
  delay(1000);
  Serial.begin(9600);
  Serial.println("Init Ethernet");

  uint8_t mac[6] = {0x02, 0x01, 0x02, 0x03, 0x04, 0x05};

  //Ethernet.begin(mac, IPAddress(192, 168, 2, 177));

  Ethernet.begin(mac,
                 IPAddress(192, 168, 2, 177),
                 IPAddress(192, 168, 88, 1), // DNS
                 IPAddress(192, 168, 88, 1), // GW
                 IPAddress(0xFF, 0xFF, 0, 0)); // Subnet

  int success = udp.begin( MQTT_PORT );

  Serial.println( success ? "success" : "failed");
}

void loop() {

  //int rc = 
  mqtt_udp_send( 0, "From", "Arduino2" );
  delay(1000);
  return;
}

int mqtt_udp_send_pkt( int fd, char *data, size_t len )
{
  int success;
  success = udp.beginPacket(IPAddress(255, 255, 255, 255), MQTT_PORT);
  success = udp.write( data, len );
  success = udp.endPacket();
  udp.stop();
  Serial.println("sent!");
  return 0;
}




