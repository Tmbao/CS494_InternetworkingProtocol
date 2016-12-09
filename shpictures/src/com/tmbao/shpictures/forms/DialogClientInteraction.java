package com.tmbao.shpictures.forms;

import com.tmbao.shpictures.utils.Utils;

import javax.swing.*;
import java.awt.event.*;
import java.net.InetAddress;

public class DialogClientInteraction extends JDialog {
  private JPanel contentPane;
  private JButton buttonCancel;
  private JButton buttonUpload;
  private JButton buttonSearchDesc;
  private JButton buttonSearchOwner;

  private InetAddress serverAddress;
  private int portNumber;
  private String clientName;


  public DialogClientInteraction(String _serverAddress, int portNumber, String clientName) {
    setContentPane(contentPane);
    setModal(true);

    try {
      this.serverAddress = InetAddress.getByName(_serverAddress);
      this.portNumber = portNumber;
      this.clientName = clientName;
    } catch (Exception e) {
      e.printStackTrace();
    }


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

    buttonUpload.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DialogUpload dialog = new DialogUpload(serverAddress, portNumber, clientName);
        dialog.setResizable(false);
        dialog.setTitle("Upload image");
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
      }
    });
    buttonSearchDesc.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DialogSearch dialog = new DialogSearch(serverAddress, portNumber, clientName, Utils.SearchType.DESCRIPTION);
        dialog.setResizable(false);
        dialog.setTitle("Search with Description");
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
      }
    });


    buttonSearchOwner.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DialogSearch dialog = new DialogSearch(serverAddress, portNumber, clientName, Utils.SearchType.OWNER);
        dialog.setResizable(false);
        dialog.setTitle("Search with Owner's Name");
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
      }
    });
  }


  private void onCancel() {
    // add your code here if necessary
    dispose();
  }

}
