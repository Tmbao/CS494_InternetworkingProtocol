package com.tmbao.shpictures.server;

import com.tmbao.shpictures.utils.Settings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Tmbao on 12/5/16.
 */
public class SerializableBufferedImage implements Serializable {

  private transient BufferedImage image;

  public SerializableBufferedImage(BufferedImage image) {
    this.image = image;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    ImageIO.write(image, Settings.FORMAT_NAME, out);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    image = ImageIO.read(in);
  }

  public BufferedImage getImage() {
    return image;
  }

  public void setImage(BufferedImage image) {
    this.image = image;
  }
}
