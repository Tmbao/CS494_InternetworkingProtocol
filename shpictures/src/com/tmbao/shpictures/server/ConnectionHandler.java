package com.tmbao.shpictures.server;

import com.sun.tools.javac.util.Pair;
import com.tmbao.shpictures.utils.Serializer;
import com.tmbao.shpictures.utils.Settings;
import com.tmbao.shpictures.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Tmbao on 12/4/16.
 */
public class ConnectionHandler implements Runnable {
  private ClientManagement clientManagement;
  private Socket socket;
  private CompletelyReceivedCallback callback;

  public ConnectionHandler(ClientManagement clientManagement, Socket socket, CompletelyReceivedCallback callback) {
    this.clientManagement = clientManagement;
    this.socket = socket;
    this.callback = callback;
  }

  @Override
  public void run() {
    try {
      // Receive data from client
      byte[] bytes = new byte[Settings.BUFFER_CAPACITY];
      InputStream inputStream = socket.getInputStream();
      OutputStream outputStream = socket.getOutputStream();

      Package requestPkg;
      Package responsePkg = null;
      ClientState clientState = null;
      while (true) {
        requestPkg = Utils.getPackage(inputStream);

        switch (requestPkg.getType()) {
          case INITIATE:
            // This is an initial request, we should give the client an unique id
            responsePkg = new Package(
                clientManagement.addClient(requestPkg.getClientIdentifier().getClientName()),
                Package.PackageType.SUCCESS);
            break;

          case UPLOAD:
            // The client is uploading an image

            // Get clientState based on sessionId
            clientState = clientManagement.getState(requestPkg.getClientIdentifier().getClientId());

            if (clientState == null) {
              // This state has either expired or doesn't exist, we should erase that Id
              responsePkg = new Package(
                  new ClientIdentifier(requestPkg.getClientIdentifier().getClientName(), ""),
                  Package.PackageType.FAILURE);
            } else {
              clientState.append(requestPkg.getData(), requestPkg.getLength());
              responsePkg = new Package(requestPkg.getClientIdentifier(), Package.PackageType.SUCCESS);
            }
            break;

          case SUCCESS:
            // This server is sending a list of images to client, we should keep doing so

            // Get clientState based on sessionId
            clientState = clientManagement.getState(requestPkg.getClientIdentifier().getClientId());

            if (clientState == null) {
              // This state has either expired or doesn't exist, we should erase that Id
              responsePkg = new Package(
                  new ClientIdentifier(requestPkg.getClientIdentifier().getClientName(), ""),
                  Package.PackageType.FAILURE);
            } else {
              Pair<Integer, byte[]> responseData = clientState.poll();
              if (responseData == null) {
              } else {
                responsePkg = new Package(clientState.getIdentifier(), Package.PackageType.LIST_IMAGE,
                    responseData.snd, responseData.fst);
              }
            }
            break;

          case SEARCH_WITH_DESCRIPTION:
            clientState = clientManagement.getState(requestPkg.getClientIdentifier().getClientId());

            if (clientState == null) {
              // This state has either expired or doesn't exist, we should erase that Id
              responsePkg = new Package(
                  new ClientIdentifier(requestPkg.getClientIdentifier().getClientName(), ""),
                  Package.PackageType.FAILURE);
            } else {
              clientState.append(requestPkg.getData(), requestPkg.getLength());
              responsePkg = callback.onCompletelyReceived(clientState, Package.PackageType.SEARCH_WITH_DESCRIPTION);
            }
            break;

          case SEARCH_WITH_OWNER:
            clientState = clientManagement.getState(requestPkg.getClientIdentifier().getClientId());

            if (clientState == null) {
              // This state has either expired or doesn't exist, we should erase that Id
              responsePkg = new Package(
                  new ClientIdentifier(requestPkg.getClientIdentifier().getClientName(), ""),
                  Package.PackageType.FAILURE);
            } else {
              clientState.append(requestPkg.getData(), requestPkg.getLength());
              responsePkg = callback.onCompletelyReceived(clientState, Package.PackageType.SEARCH_WITH_OWNER);
            }
            break;

          case CLOSE:
            clientState = clientManagement.getState(requestPkg.getClientIdentifier().getClientId());
//            responsePkg = new Package(requestPkg.getClientIdentifier(), Package.PackageType.CLOSE);
            break;
        }

        if (clientState != null) {
          clientManagement.updateState(clientState.getIdentifier().getClientId(), clientState);
        }

        if (requestPkg.getType() == Package.PackageType.CLOSE) {
          callback.onCompletelyReceived(clientState, Package.PackageType.UPLOAD);
          clientManagement.removeState(clientState.getIdentifier().getClientId());
          break;
        }

//        if (responsePkg.getType() == Package.PackageType.CLOSE) {
        if (responsePkg != null) {
          outputStream.write(Serializer.serialize(responsePkg));
          outputStream.flush();
        }
      }

      inputStream.close();
      outputStream.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
