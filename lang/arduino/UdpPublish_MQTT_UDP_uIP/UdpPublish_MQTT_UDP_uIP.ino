#include <mqtt_udp.h>

#if 1
#include <UIPEthernet.h>
#else
#include <Ethernet.h>
#include <EthernetUdp.h>
#endif

#include <OneWire.h>

EthernetUDP udp;

OneWire  ds(9);  // on pin 9
#define MAX_DS1820_SENSORS 2
byte addr[MAX_DS1820_SENSORS][8];

int have_1w = 0;
int got_1w_temp = 0;

void init_1wire( void )
{
  Serial.println("Init 1Wire 0");

  if (!ds.search(addr[0]))
  {
    Serial.println("No 1W 0");
    ds.reset_search();
    delay(250);
    return;
  }

  Serial.println("Got 1W 0");
  have_1w = 1;

  if ( !ds.search(addr[1]))
  {
    Serial.println("No 1W 1");
    ds.reset_search();
    delay(250);
    return;
  }

  Serial.println("Got 1W 1");

}


char temp_data[20];

void read_1wire( void )
{
  int HighByte, LowByte, TReading, SignBit, Tc_100, Whole, Fract;

  if (!have_1w) return;

  if ( OneWire::crc8( addr[0], 7) != addr[0][7])
  {
    Serial.println("1W CRC is not valid");
    have_1w = 0;
    return;
  }

  if ( (addr[0][0] != 0x10) && (addr[0][0] != 0x28) )
  {
    Serial.print("Device is not a DS18S20 family device: 0x" );
    Serial.println( addr[0][0], 16 );
    have_1w = 0;
    return;
  }

  //ds.reset();
  //ds.select(addr[0]);
  //ds.write(0x44, 1);        // start conversion, with parasite power on at the end

  //delay(1000);     // maybe 750ms is enough, maybe not
  // we might do a ds.depower() here, but the reset will take care of it.

  if ( got_1w_temp > 0 )
  {
    int present = ds.reset();
    ds.select(addr[0]);
    ds.write(0xBE);         // Read Scratchpad

    char data[10];

    for ( int i = 0; i < 9; i++)
    { // we need 9 bytes
      data[i] = ds.read();
    }

    LowByte = data[0];
    HighByte = data[1];
    TReading = (HighByte << 8) + LowByte;
    SignBit = TReading & 0x8000;  // test most sig bit
    if (SignBit) // negative
    {
      TReading = (TReading ^ 0xffff) + 1; // 2's comp
    }
    Tc_100 = (TReading * 100 / 8);

    Whole = Tc_100 / 100;  // separate off the whole and fractional portions
    Fract = Tc_100 % 100;

    sprintf(temp_data, "%c%d.%d", SignBit ? '-' : '+', Whole, Fract < 10 ? 0 : Fract);

    Serial.print("Temp = ");
    Serial.print(temp_data);
    Serial.println("C");
  }

  ds.reset();
  ds.select(addr[0]);
  ds.write(0x44, 1);        // start conversion, with parasite power on at the end

  if ( got_1w_temp < 2 ) got_1w_temp++;
}




void setup() {
  delay(500);
  Serial.begin(9600);

  init_1wire();

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

//int val = 0;

void loop() {
  read_1wire();
  Ethernet.maintain();
  //int rc =

  //char buf[16];
  //sprintf( buf, "%d", val++ );

  if ( got_1w_temp > 1 )
  {
    Serial.print("Sending... ");
    mqtt_udp_send( 0, "FromArduino", temp_data );
  }
  
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
