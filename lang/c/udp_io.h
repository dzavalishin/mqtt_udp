
// all includes needed for UDP IO to compile

# if 0

// broken on CYGWIN :(

#ifdef HAVE_SOCKET
#  include <sys/socket.h>
#endif

#ifdef HAVE_NETINET_IN_H
#  include <netinet/in.h>
#endif

#ifdef HAVE_ARPA_INET_H
#  include <arpa/inet.h>
#endif

#endif

#ifdef __MINGW32__
//#  include "winsock.h"
#  include "ws2tcpip.h"
#else

#  include <sys/socket.h>
#  include <netinet/in.h>
#  include <arpa/inet.h>

#endif



