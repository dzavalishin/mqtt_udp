#include <string.h>
#include "../hmac.h"
//#include "../sha256.h"
#include "../md5.h"

#include "../mqtt_udp.h"

void *signature_key;
size_t signature_key_len;

static void do_mqtt_udp_hmac_md5( unsigned char *text, int text_len, unsigned char *hmac );


/// User request to start using digital signature.
int mqtt_udp_enable_signature( const char *key, size_t key_len )
{
    if( (key==0) || (key_len==0) )
    {
        mqtt_udp_hmac_md5 = 0; // Turn OFF
        return 0;    
    }

    mqtt_udp_hmac_md5 = do_mqtt_udp_hmac_md5;
    return 0;
}


#define KEY_IOPAD_SIZE 64
#define KEY_IOPAD_SIZE128 128
void hmac_md5(unsigned char *key, int key_len,
    unsigned char *text, int text_len, unsigned char *hmac)
{
    MD5_CTX context;
    unsigned char k_ipad[KEY_IOPAD_SIZE];    /* inner padding - key XORd with ipad  */
    unsigned char k_opad[KEY_IOPAD_SIZE];    /* outer padding - key XORd with opad */
    int i;

    /*
    * the HMAC_MD5 transform looks like:
    *
    * MD5(K XOR opad, MD5(K XOR ipad, text))
    *
    * where K is an n byte key
    * ipad is the byte 0x36 repeated 64 times

    * opad is the byte 0x5c repeated 64 times
    * and text is the data being protected
    */

    /* start out by storing key in pads */
    memset( k_ipad, 0, sizeof(k_ipad));
    memset( k_opad, 0, sizeof(k_opad));
    memcpy( k_ipad, key, key_len);
    memcpy( k_opad, key, key_len);
    
    /* XOR key with ipad and opad values */
    for (i = 0; i < KEY_IOPAD_SIZE; i++) {
        k_ipad[i] ^= 0x36;
        k_opad[i] ^= 0x5c;
    }
    
    // perform inner MD5
    MD5Init(&context);                    /* init context for 1st pass */
    MD5Update(&context, k_ipad, KEY_IOPAD_SIZE);      /* start with inner pad */
    MD5Update(&context, (unsigned char*)text, text_len); /* then text of datagram */
    MD5Final(hmac, &context);             /* finish up 1st pass */
    
    // perform outer MD5
    MD5Init(&context);                   /* init context for 2nd pass */
    MD5Update(&context, k_opad, KEY_IOPAD_SIZE);     /* start with outer pad */
    MD5Update(&context, hmac, MD5_DIGEST_SIZE);     /* then results of 1st hash */
    MD5Final(hmac, &context);          /* finish up 2nd pass */
}

static void do_mqtt_udp_hmac_md5( unsigned char *text, int text_len, unsigned char *hmac )
{
    hmac_md5( signature_key, signature_key_len, text, text_len, hmac);
}


#if 0

void hmac_sha256(unsigned char *key, int key_len,
    unsigned char *text, int text_len, unsigned char *hmac) {
    SHA256_State context;
    unsigned char k_ipad[KEY_IOPAD_SIZE];    /* inner padding - key XORd with ipad  */
    unsigned char k_opad[KEY_IOPAD_SIZE];    /* outer padding - key XORd with opad */
    int i;

    /* start out by storing key in pads */
    memset(k_ipad, 0, sizeof(k_ipad));
    memset(k_opad, 0, sizeof(k_opad));
    memcpy(k_ipad, key, key_len);
    memcpy(k_opad, key, key_len);

    /* XOR key with ipad and opad values */
    for (i = 0; i < KEY_IOPAD_SIZE; i++) {
        k_ipad[i] ^= 0x36;
        k_opad[i] ^= 0x5c;
    }

    // perform inner SHA256
    SHA256_Init(&context);                    /* init context for 1st pass */
    SHA256_Bytes(&context, k_ipad, KEY_IOPAD_SIZE);      /* start with inner pad */
    SHA256_Bytes(&context, text, text_len); /* then text of datagram */
    SHA256_Final(&context, hmac);             /* finish up 1st pass */

    // perform outer SHA256
    SHA256_Init(&context);                   /* init context for 2nd pass */
    SHA256_Bytes(&context, k_opad, KEY_IOPAD_SIZE);     /* start with outer pad */
    SHA256_Bytes(&context, hmac, SHA256_DIGEST_SIZE);     /* then results of 1st hash */
    SHA256_Final(&context, hmac);          /* finish up 2nd pass */
}


#endif


