#include <cstdio>
#include <vector>

#include "client_socket.hpp"
#include "utilities.hpp"


class ClientSocketExample : public ClientSocket {
public:
  ClientSocketExample(char * serverAddress, int port): ClientSocket(serverAddress, port) {}

  virtual int start() {
    if (connect(sock, (struct sockaddr *) &server, sizeof(server)) < 0) {
      puts("Connect failed!");
      return -1;
    }
    while (true) {
      int a, b;
      std::scanf("%d%d", &a, &b);
      char * message;
      int length = Utilities::encodeListInt(std::vector<int>{a, b}, message);
      printf("Encoded message: length = %d\n", length);
      if (!sendMessage(message, length)) {
        puts("Could not send message!");
      } else {
        puts("Message sent!");
        if (waitForResponse(message, 32)) {
          std::vector<int> response = Utilities::decodeListInt(message);
          printf("Response received: length = %d\n", response.size());
          for (int x : response) {
            std::printf("%d ", x);
          }
          std::printf("\n");
        } else {
          puts("Server doesn't response!");
        }
      }
    }
    return 0;
  }
};

int main() {
  ClientSocketExample * client = new ClientSocketExample("127.0.0.1", 8889);
  printf("Exit: %d\n", client->start());
  return 0;
}
