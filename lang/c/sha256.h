#ifndef _CIPHER_SHA256_H
#define _CIPHER_SHA256_H
#ifdef  __cplusplus
extern "C" {
#endif /* __cplusplus */
    typedef unsigned int uint32;
    typedef struct {
        uint32 h[8];
        unsigned char block[64];
        int blkused;
        uint32 lenhi, lenlo;
    } SHA256_State;
    void SHA256_Init(SHA256_State * s);
    void SHA256_Bytes(SHA256_State * s, const void *p, int len);
    void SHA256_Final(SHA256_State * s, unsigned char *output);
    void SHA256_Simple(const void *p, int len, unsigned char *output);
    void sha256_do_hmac(unsigned char* key, int keylen,
        unsigned char *blk, int len, unsigned char *hmac);
#ifdef  __cplusplus
}
#endif /* __cplusplus */
#endif /* _CIPHER_SHA256_H */
