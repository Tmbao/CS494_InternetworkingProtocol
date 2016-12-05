package com.tmbao.shpictures;

import com.sun.tools.javac.util.Pair;
import com.tmbao.shpictures.server.ClientIdentifier;
import com.tmbao.shpictures.server.Package;
import com.tmbao.shpictures.server.SharedPicture;
import com.tmbao.shpictures.utils.Serializer;
import com.tmbao.shpictures.utils.Settings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Tmbao on 12/3/16.
 */
public class SHPicturesClient {
  private static Scanner scanner;

  private static InetAddress serverAddress;
  private static int portNumber;
  private static String clientName;
  private static Socket clientSocket;
  private static InputStream inputStream;
  private static OutputStream outputStream;

  public static void main(String[] args) {
    try {
      // Setup environment
      scanner = new Scanner(System.in);

      // Setup parameters
      serverAddress = InetAddress.getLocalHost();
      portNumber = Settings.DEFAULT_PORT;
      clientName = "Unnamed client";

      if (args.length > 0) {
        serverAddress = InetAddress.getByName(args[0]);
      }
      if (args.length > 1) {
        portNumber = Integer.parseInt(args[1]);
      }
      if (args.length > 2) {
        clientName = args[2];
      }

      for (int option = 0; option != 5; ) {
        option = 0;
        while (option == 0) {
//          clearScreen();
          option = getOption();
        }

        SharedPicture picture;
        ClientIdentifier identifier = null;
        String query;

        switch (option) {
          case 1:
            clientName = getNewClientName();
            break;
          case 2:
            picture = getNewPicture();

            if (picture != null) {
              // First, get a session id
              identifier = getClientIdenfier();

              if (identifier != null) {
                // Serialize and split into chunks
                List<Pair<Integer, byte[]>> serializedSharedPics = Serializer.serialize(picture, Settings.BUFFER_SIZE);
                for (Pair<Integer, byte[]> serializedSharedPic : serializedSharedPics) {
                  Package pkg = new Package(identifier, Package.PackageType.UPLOAD,
                      serializedSharedPic.snd, serializedSharedPic.fst);
                  // Send a chunk
                  outputStream.write(Serializer.serialize(pkg));
                  outputStream.flush();
                  // Wait for response
                  byte[] bytes = new byte[Settings.BUFFER_CAPACITY];

                  try {
                    int length = inputStream.read(bytes);
                    pkg = (Package) Serializer.deserialize(bytes);
                  } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                  }

                  if (pkg.getType() != Package.PackageType.SUCCESS) {
                    System.out.println("FAILED to send!");
                    break;
                  }
                }
              }
            }
            break;
          case 3:
            query = getQuery();

            // First, get a session id
            identifier = getClientIdenfier();

            if (identifier != null) {
              byte[] serializedQuery = Serializer.serialize(query);
              Package pkg = new Package(identifier, Package.PackageType.SEARCH_WITH_DESCRIPTION,
                  serializedQuery, serializedQuery.length);

              List<SharedPicture> result = sendQueryAndGetResult(pkg);
              outputResult(result);
            }

            break;
          case 4:
            query = getQuery();

            // First, get a session id
            identifier = getClientIdenfier();

            if (identifier != null) {
              byte[] serializedQuery = Serializer.serialize(query);
              Package pkg = new Package(identifier, Package.PackageType.SEARCH_WITH_DESCRIPTION,
                  serializedQuery, serializedQuery.length);

              List<SharedPicture> result = sendQueryAndGetResult(pkg);
              outputResult(result);
            }
            break;
        }

        closeConnection(identifier);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void closeConnection(ClientIdentifier identifier) throws IOException {
    if (identifier != null) {
      Package pkg = new Package(identifier, Package.PackageType.CLOSE);
      try {
        outputStream.write(Serializer.serialize(pkg));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    inputStream.close();
    outputStream.close();
    clientSocket.close();
  }

  private static void outputResult(List<SharedPicture> result) {
    for (SharedPicture sharedPicture : result) {
      System.out.printf("Owner: %s\nDescription: %s\nImageId: %s\n---\n",
          sharedPicture.getOwnerName(), sharedPicture.getDescription(), sharedPicture.getImageId());
    }
  }

  private static List<SharedPicture> sendQueryAndGetResult(Package requestPkg) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      outputStream.write(Serializer.serialize(requestPkg));
      outputStream.flush();

      byte[] bytes = new byte[Settings.BUFFER_CAPACITY];
      int length = inputStream.read(bytes);

      Package responsePkg = (Package) Serializer.deserialize(Arrays.copyOf(bytes, length));
      if (responsePkg.getType() == Package.PackageType.LIST_IMAGE) {
        byteArrayOutputStream.write(responsePkg.getData(), 0, responsePkg.getLength());
        while (responsePkg.getLength() == Settings.BUFFER_SIZE) {
          requestPkg = new Package(responsePkg.getClientIdentifier(), Package.PackageType.SUCCESS, null, 0);
          outputStream.write(Serializer.serialize(requestPkg));
          outputStream.flush();

          length = inputStream.read(bytes);
          if (length > 0) {
            responsePkg = (Package) Serializer.deserialize(Arrays.copyOf(bytes, length));
            if (responsePkg.getType() == Package.PackageType.LIST_IMAGE) {
              byteArrayOutputStream.write(responsePkg.getData(), 0, responsePkg.getLength());
            } else {
              break;
            }
          }
        }
      }

      List<SharedPicture> result = (List<SharedPicture>) Serializer.deserialize(byteArrayOutputStream.toByteArray());
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getQuery() {
    System.out.print("Enter a query: ");
    return scanner.nextLine().trim();
  }

  private static ClientIdentifier getClientIdenfier() throws IOException {
    // Initiate socket
    clientSocket = new Socket(serverAddress, portNumber);
    inputStream = clientSocket.getInputStream();
    outputStream = clientSocket.getOutputStream();

    ClientIdentifier identifier = new ClientIdentifier(clientName, "");
    Package pkg = new Package(identifier, Package.PackageType.INITIATE);
    // Send an initial package to get a session id
    try {
      outputStream.write(Serializer.serialize(pkg));
      outputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    byte[] bytes = new byte[Settings.BUFFER_CAPACITY];
    try {
      int length = inputStream.read(bytes);
      pkg = (Package) Serializer.deserialize(bytes);

      if (pkg.getType() == Package.PackageType.SUCCESS) {
        System.out.printf("Successfully connected.\nClient Id: %s\n", pkg.getClientIdentifier().getClientId());
        return pkg.getClientIdentifier();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static SharedPicture getNewPicture() {
    System.out.printf("Enter owner's name (Enter for %s: ", clientName);
    String ownerName = scanner.nextLine().trim();
    if (ownerName.equals("")) {
      ownerName = clientName;
    }

    System.out.println("Enter description:");
    String description = scanner.nextLine().trim();

    System.out.println("Enter image path:");
    String imagePath = scanner.nextLine().trim();

    File inputFile = new File(imagePath);
    BufferedImage image = null;
    try {
      image = ImageIO.read(inputFile);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    return new SharedPicture(ownerName, description, "", image);
  }

  private static String getNewClientName() {
    System.out.print("Enter client name: ");
    return scanner.nextLine().trim();
  }

  private static int getOption() {
    System.out.println("Select one of the following:");
    System.out.println("1. Change client name.");
    System.out.println("2. Upload an image.");
    System.out.println("3. Search for images with description.");
    System.out.println("4. Search for images with owner's name.");
    System.out.println("5. Exit.");

    return Integer.parseInt(scanner.nextLine());
  }

  private static void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
}
