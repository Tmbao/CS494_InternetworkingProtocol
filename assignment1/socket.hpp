#include <sys/socket.h>
#include <arpa/inet.h>


// A socket interface.
class Socket {
protected:
  int sock;
  sockaddr_in server;

public:
  Socket(char * serverAddress, int portNumber) {
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == -1) {
      // Throw some exception.
      return;
    }

    server.sin_addr.s_addr = inet_addr(serverAddress);
    server.sin_family = AF_INET;
    server.sin_port = htons(portNumber);
  }
};
