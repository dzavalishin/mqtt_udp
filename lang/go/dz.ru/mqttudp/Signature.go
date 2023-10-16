package mqttudp

import (
	"crypto/md5"
)

var signature_key []byte

// / User request to start using digital signature.
func mqtt_udp_enable_signature(key []byte) error {
	if key == nil {
		signature_key = nil // Turn OFF
		return nil
	}

	// key_len sanity check, use PKT_BUF_SIZE as "too big"
	if len(key) > PKT_BUF_SIZE {
		return GlobalErrorHandler(Invalid, "signature too long", "")
	}

	signature_key = key
	return nil
}

const KEY_IOPAD_SIZE = 64
const KEY_IOPAD_SIZE128 = 128

// Returns HMAC digest
func do_hmac_md5(key []byte, text []byte) []byte {

	//MD5_CTX context;
	context := md5.New()

	//key_len := len(key)
	//text_len := len(text)

	//printf("key = '%s' klen %d text '%s' tlen %d\n", key, key_len, text, text_len );

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

	var k_ipad [KEY_IOPAD_SIZE]byte /* inner padding - key XORd with ipad  */
	var k_opad [KEY_IOPAD_SIZE]byte /* outer padding - key XORd with opad */

	/* start out by storing key in pads */
	//memset( k_ipad, 0, sizeof(k_ipad));
	//memset( k_opad, 0, sizeof(k_opad));
	//memcpy( k_ipad, key, key_len);
	//memcpy( k_opad, key, key_len);

	clear(k_ipad[:])
	clear(k_opad[:])

	copy(k_ipad[:], key)
	copy(k_opad[:], key)

	/* XOR key with ipad and opad values */
	for i := 0; i < KEY_IOPAD_SIZE; i++ {
		k_ipad[i] ^= 0x36
		k_opad[i] ^= 0x5c
	}

	context.Reset()
	context.Write(k_ipad[:])
	context.Write(text)
	s1 := context.Sum(nil)

	context.Reset()
	context.Write(k_opad[:])
	context.Write(s1)
	return context.Sum(nil)

	/*
	   // perform inner MD5
	   MD5Init(&context);                    //* init context for 1st pass
	   MD5Update(&context, k_ipad, KEY_IOPAD_SIZE);      //* start with inner pad
	   MD5Update(&context, (unsigned char*)text, text_len); //* then text of datagram
	   MD5Final(hmac, &context);             //* finish up 1st pass

	   // perform outer MD5
	   MD5Init(&context);                   //* init context for 2nd pass
	   MD5Update(&context, k_opad, KEY_IOPAD_SIZE);     //* start with outer pad
	   MD5Update(&context, hmac, MD5_DIGEST_SIZE);     //* then results of 1st hash
	   MD5Final(hmac, &context);          //* finish up 2nd pass
	*/
}

func hmac_md5(text []byte) []byte {
	return do_hmac_md5(signature_key, text)
}

/*
#if 0

void hmac_sha256(unsigned char *key, int key_len,
    unsigned char *text, int text_len, unsigned char *hmac) {
    SHA256_State context;
    unsigned char k_ipad[KEY_IOPAD_SIZE];    //* inner padding - key XORd with ipad
    unsigned char k_opad[KEY_IOPAD_SIZE];    //* outer padding - key XORd with opad
    int i;

    //* start out by storing key in pads
    memset(k_ipad, 0, sizeof(k_ipad));
    memset(k_opad, 0, sizeof(k_opad));
    memcpy(k_ipad, key, key_len);
    memcpy(k_opad, key, key_len);

    //* XOR key with ipad and opad values
    for (i = 0; i < KEY_IOPAD_SIZE; i++) {
        k_ipad[i] ^= 0x36;
        k_opad[i] ^= 0x5c;
    }

    // perform inner SHA256
    SHA256_Init(&context);                    //* init context for 1st pass
    SHA256_Bytes(&context, k_ipad, KEY_IOPAD_SIZE);      //* start with inner pad
    SHA256_Bytes(&context, text, text_len); //* then text of datagram
    SHA256_Final(&context, hmac);             //* finish up 1st pass

    // perform outer SHA256
    SHA256_Init(&context);                   /* init context for 2nd pass
    SHA256_Bytes(&context, k_opad, KEY_IOPAD_SIZE);     /* start with outer pad
    SHA256_Bytes(&context, hmac, SHA256_DIGEST_SIZE);     /* then results of 1st hash
    SHA256_Final(&context, hmac);          /* finish up 2nd pass
} //*/
