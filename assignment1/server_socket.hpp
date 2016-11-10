#include <unistd.h>
#include <pthread.h>

#include "socket.hpp"


class ServerSocket : public Socket {
private:
  const static int backLog = 3;
  const static int messageLimit = 2000;

  struct ServerState {
    ServerSocket * obj;
    int sock;

    ServerState(ServerSocket * obj, int sock): obj(obj), sock(sock) {}
  };

  static void *internalThreadEntryConnectionHandler(void * param) {
    ((ServerState *) param)->obj->connectionHandler(((ServerState *) param)->sock);
  }
protected:
  virtual void onReceive(int sock, char * message, int length) = 0;

  void response(int sock, char * message, int length) {
    write(sock, message, length);
  }

  int connectionHandler(int sock) {
    int readSize;
    char * clientMessage = new char[messageLimit];
    while ((readSize = recv(sock, clientMessage, messageLimit, 0)) > 0) {
      onReceive(sock, clientMessage, readSize);
    }
    return sock;
  }

 public:
  ServerSocket(char * serverAddress, int port): Socket(serverAddress, port) {}

  int start() {
    if (bind(sock, (struct sockaddr *) &server, sizeof(server)) < 0) {
      // Bind failed.
      return -1;
    }

    listen(sock, backLog);

    int c = sizeof(struct sockaddr_in);
    struct sockaddr_in client;
    int clientSock;

    while (clientSock = accept(sock, (sockaddr *) &client, (socklen_t *) &c)) {
      pthread_t snifferThread;

      ServerState * state = new ServerState(this, clientSock);
      if (pthread_create(&snifferThread, NULL, internalThreadEntryConnectionHandler, (void *) state) < 0) {
        return 1;
      }
    }

    if (clientSock < 0) {
      return 2;
    }
    return 0;
  }
};
