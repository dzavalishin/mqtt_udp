#ifndef _HMAC_H
#define _HMAC_H

#include <stdint.h>
#include <stddef.h> // size_t

#ifdef  __cplusplus
extern "C" {
#endif /* __cplusplus */


#if 0
#define SHA256_DIGEST_SIZE  32

    void hmac_sha256(
		unsigned char *key, int key_len,
        unsigned char *text, int text_len, 
		unsigned char *hmac);
#endif

    void hmac_md5(unsigned char *key, int key_len,
        unsigned char *text, int text_len, unsigned char *hmac);


extern void *signature_key;
extern size_t signature_key_len;

#ifdef  __cplusplus
}
#endif /* __cplusplus */
#endif /* _HMAC_H */
