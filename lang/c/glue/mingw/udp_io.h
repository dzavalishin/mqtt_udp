
// all includes needed for UDP IO to compile
#define WIN32_LEAN_AND_MEAN

#ifdef __MINGW32__

//#  include "winsock.h"
#  include <winsock2.h>
#  include "ws2tcpip.h"

#else

#  include <sys/socket.h>
#  include <netinet/in.h>
#  include <arpa/inet.h>

#endif



