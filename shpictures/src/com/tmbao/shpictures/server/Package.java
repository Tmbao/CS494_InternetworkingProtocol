package com.tmbao.shpictures.server;

import com.tmbao.shpictures.utils.Settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Tmbao on 12/4/16.
 */
public class Package implements Serializable {

  public enum PackageType implements Serializable {
    INITIATE,
    SUCCESS,
    FAILURE,
    UPLOAD,
    SEARCH_WITH_DESCRIPTION,
    SEARCH_WITH_OWNER,
    LIST_IMAGE,
    CLOSE,
  }

  private ClientIdentifier clientIdentifier;
  private PackageType type;
  private int length;
  private transient byte[] data;

  public Package(ClientIdentifier clientIdentifier, PackageType type) {
    this.clientIdentifier = clientIdentifier;
    this.type = type;
    this.data = null;
    this.length = 0;
  }

  public Package(ClientIdentifier clientIdentifier, PackageType type, byte[] data, int length) {
    this.clientIdentifier = clientIdentifier;
    this.type = type;
    this.data = data;
    this.length = length;
  }

  public PackageType getType() {
    return type;
  }

  public ClientIdentifier getClientIdentifier() {
    return clientIdentifier;
  }

  public byte[] getData() {
    return data;
  }

  public int getLength() {
    return length;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
//    out.writeObject(clientIdentifier);
//    out.writeObject(type);
//    out.writeInt(length);
    for (int index = 0; index < length; ++index) {
      out.writeByte(data[index]);
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
//    clientIdentifier = (ClientIdentifier) in.readObject();
//    type = (PackageType) in.readObject();
//    length = in.readInt();
    data = new byte[Settings.BUFFER_SIZE];
    for (int index = 0; index < length; ++index) {
      data[index] = in.readByte();
    }
  }

}
