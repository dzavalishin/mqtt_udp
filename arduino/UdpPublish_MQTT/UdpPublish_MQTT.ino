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
  mqtt_udp_send( 0, "From", "Arduino1" );
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

#if 0


#define BUFLEN 512



static int pack_len( char *buf, int *blen, int *used, int data_len )
{
    *used = 0;
    while( 1 )
    {
        if( *blen <= 0 ) return 1;

        int byte = data_len % 128;
        data_len /= 128;

        if( data_len > 0 )
            byte |= 0x80;

        *buf++ = byte;
        (*blen)--;
        (*used)++;

        if( data_len == 0 ) return 0;
    }
}


int mqtt_udp_send( int fd, char *topic, char *data )
{
    unsigned char buf[BUFLEN];

    int tlen = strlen(topic);
    int dlen = strlen(data);

    int blen = sizeof(buf);
    unsigned char *bp = buf;

    *bp++ = PTYPE_PUBLISH;
    blen--;

    int total = tlen + dlen + 2; // packet size
    if( total > blen )
        return 1;

    //int size = total+1;

    int used = 0;
    int rc = pack_len( (char *)bp, &blen, &used, total );
    if( rc ) return rc;

    bp += used;


    *bp++ = (tlen >>8) & 0xFF;
    *bp++ = tlen & 0xFF;
    blen -= 2;

    //NB! Must be UTF-8
    while( tlen-- > 0 )
    {
        if( blen <= 0 ) return 1;
        *bp++ = *topic++;
        blen--;
    }

    while( dlen-- > 0 )
    {
        if( blen <= 0 ) return 1;
        *bp++ = *data++;
        blen--;
    }

    return mqtt_udp_send_pkt( fd, (char *)buf, bp-buf );
}

#endif



