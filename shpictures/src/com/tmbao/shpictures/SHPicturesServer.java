package com.tmbao.shpictures;

import com.sun.tools.javac.util.Pair;
import com.tmbao.shpictures.server.*;
import com.tmbao.shpictures.server.Package;
import com.tmbao.shpictures.utils.Serializer;
import com.tmbao.shpictures.utils.Settings;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Tmbao on 12/3/16.
 */
public class SHPicturesServer {


  public static void main(String[] args) {
    int portNumber = Settings.DEFAULT_PORT;
    if (args.length > 0) {
      portNumber = Integer.parseInt(args[0]);
    }

    ClientManagement clientManagement = new ClientManagement();
    List<SharedPicture> sharedPictures = new CopyOnWriteArrayList<>();

    try {
      ServerSocket serverSocket = new ServerSocket(portNumber);

      System.out.printf("Listening to portNumber %d ...\n", portNumber);

      while (true) {
        Socket socket = serverSocket.accept();
        System.out.printf("Received a request from portNumber %d ...\n", socket.getPort());
        new Thread(new ConnectionHandler(clientManagement, socket, (clientState, type) -> {
          try {
            SharedPicture sharedPic;
            String query;
            List<SharedPicture> result;

            switch (type) {
              case UPLOAD:
                sharedPic = (SharedPicture) Serializer.deserialize(clientState.getData());
                sharedPic.setImageId(clientState.getIdentifier().getClientId());
                sharedPic.releaseImage();
                System.out.printf("released\n");

                clientState.resetData();

                // Put this into a set
                sharedPictures.add(sharedPic);
                return new Package(clientState.getIdentifier(), Package.PackageType.SUCCESS);

              case SEARCH_WITH_DESCRIPTION:
                query = (String) Serializer.deserialize(clientState.getData());
                result = new ArrayList<>();
                for (SharedPicture picture : sharedPictures) {
                  if (picture.getDescription().contains(query)) {
                    result.add(picture);
                  }
                }
                return serializeAndPoll(clientState, result);

              case SEARCH_WITH_OWNER:
                query = (String) Serializer.deserialize(clientState.getData());
                result = new ArrayList<>();
                for (SharedPicture picture : sharedPictures) {
                  if (picture.getOwnerName().equals(query)) {
                    result.add(picture);
                  }
                }
                // Serialize
                return serializeAndPoll(clientState, result);
            }
          } catch (IOException e) {
            e.printStackTrace();
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
          return new Package(clientState.getIdentifier(), Package.PackageType.FAILURE);
        })).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static Package serializeAndPoll(ClientState clientState, List<SharedPicture> result) throws IOException {
    List<Pair<Integer, byte[]>> buffers;

    clientState.resetData();
    buffers = Serializer.serialize(result, Settings.BUFFER_SIZE);
    for (Pair<Integer, byte[]> buffer : buffers) {
      clientState.append(buffer.snd, buffer.fst);
    }
    Pair<Integer, byte[]> buffer = clientState.poll();
    return new Package(clientState.getIdentifier(), Package.PackageType.LIST_IMAGE, buffer.snd, buffer.fst);
  }
}
