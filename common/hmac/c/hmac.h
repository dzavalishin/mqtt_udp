#ifndef _CIPHER_HMAC_ALL_H
#define _CIPHER_HMAC_ALL_H
#ifdef  __cplusplus
extern "C" {
#endif /* __cplusplus */

#define SHA256_DIGEST_SIZE  32

    void hmac_sha256(
		unsigned char *key, int key_len,
        unsigned char *text, int text_len, 
		unsigned char *hmac);


#define    MD5_DIGEST_SIZE  16
    void hmac_md5(unsigned char *key, int key_len,
        unsigned char *text, int text_len, unsigned char *hmac);


#ifdef  __cplusplus
}
#endif /* __cplusplus */
#endif /* _CIPHER_HMAC_ALL_H */
