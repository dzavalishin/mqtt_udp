#ifndef HAVE_STRLCAT
//nclude <sys/types.h>
#include <string.h>
#include <stddef.h>

/*
 * '_cups_strlcat()' - Safely concatenate two strings.
 */

size_t                            /* O - Length of string */
    strlcat(char       *dst,      /* O - Destination string */
            const char *src,      /* I - Source string */
            size_t     size)      /* I - Size of destination string buffer */
{
    size_t    srclen;         /* Length of source string */
    size_t    dstlen;         /* Length of destination string */


    /*
     * Figure out how much room is left...
     */

    dstlen = strnlen(dst,size);
    size   -= dstlen + 1;

    if (!size)
        return (dstlen);        /* No room, return immediately... */

    /*
     * Figure out how much room is needed...
     */

    srclen = strnlen(src,size);

    /*
     * Copy the appropriate amount...
     */

    if (srclen > size)
        srclen = size;

    memcpy(dst + dstlen, src, srclen);
    dst[dstlen + srclen] = '\0';

    return (dstlen + srclen);
}
#endif /* !HAVE_STRLCAT */

