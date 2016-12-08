package com.tmbao.shpictures;

import com.tmbao.shpictures.forms.FormSetUpParameters;

import javax.swing.*;

/**
 * Created by Nguyen on 12/8/2016.
 */
public class SHPicturesClientGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SHPictures");
        frame.setResizable(false);
        frame.setContentPane(new FormSetUpParameters().FormSetup);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
