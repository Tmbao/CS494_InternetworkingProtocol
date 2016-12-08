package com.tmbao.shpictures.utils;

import com.tmbao.shpictures.forms.DialogShowImage;
import com.tmbao.shpictures.server.SharedPicture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Created by Nguyen on 12/7/2016.
 */
public class Utils {

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";

    public enum SearchType {
        OWNER,
        DESCRIPTION
    }

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public static void outputResult(List<SharedPicture> result) {
        /*
        for (SharedPicture sharedPicture : result) {
            System.out.printf("Owner: %s\nDescription: %s\nImageId: %s\n---\n",
                    sharedPicture.getOwnerName(), sharedPicture.getDescription(), sharedPicture.getImageId());
        }
        */

        DialogShowImage dialog = new DialogShowImage (result);
        dialog.setResizable(false);
        dialog.setTitle("Search result");
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}
