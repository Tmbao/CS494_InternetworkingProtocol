package com.tmbao.shpictures.server;

import com.sun.tools.javac.util.Pair;
import com.tmbao.shpictures.utils.Settings;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Tmbao on 12/3/16.
 */
public class ClientState {
  private final ClientIdentifier identifier;
  private long lastActive;
  private LinkedList<Pair<Integer, byte[]>> bytesList;

  public ClientState(ClientIdentifier identifier) {
    this.identifier = identifier;
    bytesList = new LinkedList<>();
  }

  public void setActive() {
    lastActive = new Date().getTime();
  }

  public boolean isActive() {
    return new Date().getTime() - lastActive < Settings.TIMEOUT;
  }

  public ClientIdentifier getIdentifier() {
    return identifier;
  }

  public boolean append(byte[] bytes, int length) {
    setActive();
    bytesList.add(Pair.of(length, bytes));
    return length < Settings.BUFFER_SIZE;
  }

  public Pair<Integer, byte[]> poll() {
    setActive();
    if (bytesList.isEmpty()) {
      return null;
    } else {
      return bytesList.pollFirst();
    }
  }

  public void resetData() {
    bytesList.clear();
  }

  public byte[] getData() {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    while (!bytesList.isEmpty()) {
      Pair<Integer, byte[]> bytes = bytesList.pollFirst();
      buffer.write(bytes.snd, 0, bytes.fst);
    }
    return buffer.toByteArray();
  }
}
