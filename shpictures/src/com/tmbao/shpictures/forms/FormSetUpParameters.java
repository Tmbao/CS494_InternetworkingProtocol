package com.tmbao.shpictures.forms;

import com.tmbao.shpictures.utils.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

/**
 * Created by Nguyen on 12/7/2016.
 */
public class FormSetUpParameters {
    private JTextField tfIP;
    private JFormattedTextField tfPort;
    private JButton buttonSetUp;
    public JPanel FormSetup;
    private JTextField tfClientName;

    public static void main(String[] args) {
        JFrame frame = new JFrame("SHPictures");
        frame.setResizable(false);
        frame.setContentPane(new FormSetUpParameters().FormSetup);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }



    public FormSetUpParameters() {
        try {
            tfIP.setText(InetAddress.getLocalHost().getHostAddress());
            tfPort.setText(Integer.toString(Settings.DEFAULT_PORT));
        } catch (Exception e) {
            e.printStackTrace();
        }

        buttonSetUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isParamsValid()) {
                    DialogClientInteraction dialog = new DialogClientInteraction(tfIP.getText(), Integer.parseInt(tfPort.getText()), tfClientName.getText());
                    dialog.setResizable(false);
                    dialog.setTitle(tfClientName.getText());
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
                else {
                    DialogParamsError dialog = new DialogParamsError();
                    dialog.setResizable(false);
                    dialog.setTitle("ERROR");
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
            }
        });


    }

    private boolean isParamsValid() {
        String ip = tfIP.getText();
        int portNumber = Settings.DEFAULT_PORT;
        try {
            portNumber = Integer.parseInt(tfPort.getText());
        } catch (Exception e) {
            return false;
        }

        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            return false;
        }


        return !(portNumber < 1024 || portNumber > 65535);
    }
}
