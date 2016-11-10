#include <vector>

#include "server_socket.hpp"
#include "utilities.hpp"


class ServerSocketExample : public ServerSocket {
protected:
  virtual void onReceive(int sock, char * message, int length) {
    puts("Message received!");
    printf("Message length: %d\n", length);
    std::vector<int> pr = Utilities::decodeListInt(message);
    printf("Received: %d %d\n", pr[0], pr[1]);
    std::vector<int> rspn{
      pr[0] + pr[1],
      pr[0] - pr[1],
      pr[0] * pr[1],
      pr[0] / pr[1],
      pr[0] % pr[1]};

    length = Utilities::encodeListInt(rspn, message);

    printf("Response encoded: length = %d\n", length);

    response(sock, message, length);
  }

public:
  ServerSocketExample(char * serverAddress, int port): ServerSocket(serverAddress, port) {}
};


int main() {
  ServerSocketExample * server = new ServerSocketExample("127.0.0.1", 8889);
  printf("Exit: %d\n", server->start());
  return 0;
}
