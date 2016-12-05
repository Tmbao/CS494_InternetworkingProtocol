package com.tmbao.shpictures.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Tmbao on 12/3/16.
 */
public class ClientIdentifier implements Serializable {
  private String clientName;
  private String clientId;

  public ClientIdentifier(String clientName, String clientId) {
    this.clientName = clientName;
    this.clientId = clientId;
  }

  public String getClientName() {
    return clientName;
  }

  public String getClientId() {
    return clientId;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
//    out.writeObject(clientName);
//    out.writeObject(clientId);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
//    clientName = (String) in.readObject();
//    clientId = (String) in.readObject();
  }
}
