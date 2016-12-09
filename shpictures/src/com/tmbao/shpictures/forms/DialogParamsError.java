package com.tmbao.shpictures.forms;

import javax.swing.*;
import java.awt.event.*;

public class DialogParamsError extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;


  public DialogParamsError() {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    });


    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onOK();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onOK() {
    // add your code here
    dispose();
  }


  public static void main(String[] args) {
    DialogParamsError dialog = new DialogParamsError();
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
