#include <vector>

namespace Utilities {
  void encodeInt(int a, char * message) {
    message[0] = a & 0xFF;
    message[1] = (a >> 8) & 0xFF;
    message[2] = (a >> 16) & 0xFF;
    message[3] = (a >> 24) & 0xFF;
  }

  int encodeListInt(std::vector<int> seq, char *&message) {
    int length = (seq.size() + 1) * 4;
    message = new char[length];
    encodeInt(seq.size(), message);
    for (int i = 0; i < seq.size(); ++i) {
      encodeInt(seq[i], message + (i + 1) * 4);
    }
    return length;
  }

  int decodeInt(char * message) {
    return (int)message[0] | ((int)message[1] << 8) | ((int)message[2] << 16) | ((int)message[3] << 24);
  }

  std::vector<int> decodeListInt(char * message) {
    int length = decodeInt(message);
    std::vector<int> seq;
    for (int i = 0; i < length; ++i) {
      message += 4;
      seq.push_back(decodeInt(message));
    }
    return seq;
  }
};
