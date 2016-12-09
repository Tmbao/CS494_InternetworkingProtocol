package com.tmbao.shpictures.forms;

import com.tmbao.shpictures.server.ClientIdentifier;
import com.tmbao.shpictures.server.Package;
import com.tmbao.shpictures.server.SharedPicture;
import com.tmbao.shpictures.utils.Serializer;
import com.tmbao.shpictures.utils.Settings;
import com.tmbao.shpictures.utils.Utils;

import javax.swing.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class DialogSearch extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextField tfQuery;
  private InetAddress serverAddress;
  private int portNumber;
  private String clientName;
  private static Socket clientSocket;
  private static InputStream inputStream;
  private static OutputStream outputStream;
  private ClientIdentifier identifier = null;
  private Utils.SearchType searchType;

  public DialogSearch(InetAddress serverAddress, int portNumber, String clientName, Utils.SearchType searchType) {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    this.serverAddress = serverAddress;
    this.portNumber = portNumber;
    this.clientName = clientName;
    this.searchType = searchType;

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    });

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    });

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onOK() {

    String query = tfQuery.getText();
    // First, get a session id
    try {
      identifier = getClientIdenfier();

      if (identifier != null) {
        byte[] serializedQuery = Serializer.serialize(query);

        Package.PackageType pkgType;
        if(searchType == Utils.SearchType.DESCRIPTION) {
          pkgType = Package.PackageType.SEARCH_WITH_DESCRIPTION;
        }
        else {
          pkgType = Package.PackageType.SEARCH_WITH_OWNER;
        }

        Package pkg = new Package(identifier, pkgType,
            serializedQuery, serializedQuery.length);

        List<SharedPicture> result = sendQueryAndGetResult(pkg);
        Utils.outputResult(result);

        closeConnection(identifier);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    //dispose();
  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
  }

  private ClientIdentifier getClientIdenfier() throws IOException {
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

  private static List<SharedPicture> sendQueryAndGetResult(Package requestPkg) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      outputStream.write(Serializer.serialize(requestPkg));
      outputStream.flush();

      Package responsePkg = Utils.getPackage(inputStream);
      if (responsePkg.getType() == Package.PackageType.LIST_IMAGE) {
        byteArrayOutputStream.write(responsePkg.getData(), 0, responsePkg.getLength());
        while (responsePkg.getLength() == Settings.BUFFER_SIZE) {
          requestPkg = new Package(responsePkg.getClientIdentifier(), Package.PackageType.SUCCESS, null, 0);
          outputStream.write(Serializer.serialize(requestPkg));
          outputStream.flush();

          responsePkg = Utils.getPackage(inputStream);
          if (responsePkg.getType() == Package.PackageType.LIST_IMAGE) {
            byteArrayOutputStream.write(responsePkg.getData(), 0, responsePkg.getLength());
          } else {
            break;
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
}
