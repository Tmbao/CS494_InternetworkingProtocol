#include "socket.hpp"


class ClientSocket : public Socket {
protected:
  bool sendMessage(char * message, int length) {
    return send(sock, message, length, 0) >= 0;
  }

  bool waitForResponse(char *&serverResponse, int length) {
    serverResponse = new char[length];
    return recv(sock, serverResponse, length, 0) >= 0;
  }

public:
  ClientSocket(char * serverAddress, int port): Socket(serverAddress, port) {}

  virtual int start() = 0;
};
