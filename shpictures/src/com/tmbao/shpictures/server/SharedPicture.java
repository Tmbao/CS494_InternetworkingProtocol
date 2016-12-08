package com.tmbao.shpictures.server;

import com.tmbao.shpictures.utils.Settings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Tmbao on 12/4/16.
 */
public class SharedPicture implements Serializable {
  String ownerName;



  String description;
  String imageId;
  SerializableBufferedImage image;

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public String getDescription() {
    return description;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public String getImageId() {
    return imageId;
  }

  public SerializableBufferedImage getImage() {
    return image;
  }

  public void releaseImage() {
    try {
      File outputFile = new File(Settings.getFileName(imageId));
      ImageIO.write(image.getImage(), Settings.FORMAT_NAME, outputFile);
      image.getImage().flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadImage() {
    try {
      File inputFile = new File(Settings.getFileName(imageId));
      image.setImage(ImageIO.read(inputFile));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public SharedPicture(String ownerName, String description, String imageId, BufferedImage image) {
    this.ownerName = ownerName;
    this.description = description;
    this.imageId = imageId;
    this.image = new SerializableBufferedImage(image);
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
//    out.writeObject(ownerName);
//    out.writeObject(description);
//    out.writeObject(imageId);
//    out.writeObject(image);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
//    ownerName = (String) in.readObject();
//    description = (String) in.readObject();
//    imageId = (String) in.readObject();
//    image = (SerializableBufferedImage) in.readObject();
  }
}
