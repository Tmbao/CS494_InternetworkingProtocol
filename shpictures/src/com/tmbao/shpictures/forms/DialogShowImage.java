package com.tmbao.shpictures.forms;

import com.tmbao.shpictures.server.SharedPicture;
import com.tmbao.shpictures.utils.Settings;
import com.tmbao.shpictures.utils.Utils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class DialogShowImage extends JDialog {
  private JPanel contentPane;
  private JList listImage;
  private JScrollPane listScroller;
  private JLabel labelImage;
  private List<SharedPicture> imageList;

  public DialogShowImage(List<SharedPicture> imageList) {
    setContentPane(contentPane);
    setModal(true);

    listImage.setLayoutOrientation(JList.VERTICAL);
    this.imageList = imageList;

    listImage.setModel(new DefaultListModel());
    listScroller.setPreferredSize(new Dimension(Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT/5));
    labelImage.setPreferredSize(new Dimension(Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT));

    DefaultListModel listModel = (DefaultListModel) listImage.getModel();

    int index = 0;
    for(SharedPicture image : this.imageList) {
      String imageInfo = "Owner: " + image.getOwnerName() + ". Description: " + image.getDescription() + ". ID: " + image.getImageId();
      listModel.add(index, imageInfo);
      ++index;
    }

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


    listImage.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()){
          JList source = (JList)event.getSource();
          int index = source.getSelectedIndex();
          BufferedImage img = imageList.get(index).getImage().getImage();
          if(img.getWidth() > Settings.DISPLAY_WIDTH || img.getHeight() > Settings.DISPLAY_HEIGHT) {
            img = Utils.resizeImageToFit(img, Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT);
          }
          labelImage.setIcon(new ImageIcon(img));
          labelImage.setHorizontalAlignment(SwingConstants.CENTER);
        }
      }
    });
  }

  private void onOK() {
    // add your code here
    dispose();
  }
}
