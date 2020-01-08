#include <mqtt_udp.h>
#include <UIPEthernet.h>



EthernetUDP udp;


void setup() {
  delay(500);
  Serial.begin(9600);
  Serial.println("Init Ethernet");

  uint8_t mac[6] = {0x02, 0x01, 0x02, 0x03, 0x04, 0x05};

  //Ethernet.begin(mac, IPAddress(192, 168, 2, 177));
#if 1
  Ethernet.begin(mac);
  Serial.print("My IP address: ");
  Serial.println(Ethernet.localIP());  
#else  
  Ethernet.begin(mac,
                 IPAddress(192, 168, 2, 177),
                 IPAddress(192, 168, 88, 1), // DNS
                 IPAddress(192, 168, 88, 1), // GW
                 IPAddress(0xFF, 0xFF, 0, 0)); // Subnet
  Serial.println(Ethernet.localIP());  
#endif
  int success = udp.begin( MQTT_PORT );

  Serial.println( success ? "success" : "failed");
}

void loop() {
  Serial.print("Sending... ");
  //int rc = 
  mqtt_udp_send( 0, "From", "Arduino" );
  //udp.stop();
  delay(1000);
  return;


  //check for new udp-packet:
  //Serial.println("Wait 4 UDP pkt");
  int size = udp.parsePacket();
  if (size > 0) {
    do
    {
      char* msg = (char*)malloc(size + 1);
      int len = udp.read(msg, size + 1);
      msg[len] = 0;

      Serial.print("received: ");
      int i;
      for ( i = 0; i < len; i++ )
      {
        Serial.print(msg[i], HEX);
        Serial.print(" ");
      }

      free(msg);
    }
    while ((size = udp.available()) > 0);
    //finish reading this packet:
    udp.flush();
    Serial.println(" --");
    udp.stop();
  }
}

int mqtt_udp_send_pkt( int fd, char *data, size_t len )
{
  Serial.print(" pkt.. ");
  int success;
  success = udp.beginPacket(IPAddress(255, 255, 255, 255), MQTT_PORT);
  success = udp.write( data, len );
  success = udp.endPacket();
  //udp.stop();
  Serial.println("sent");
  return 0;
}
