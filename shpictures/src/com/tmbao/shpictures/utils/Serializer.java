package com.tmbao.shpictures.utils;

import com.sun.tools.javac.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tmbao on 12/4/16.
 */
public class Serializer {
  public static byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(obj);
    return byteArrayOutputStream.toByteArray();
  }

  public static List<Pair<Integer, byte[]>> serialize(Object obj, int bufferSize) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(obj);
    byte[] largeBuffer = byteArrayOutputStream.toByteArray();

    // Split into chunks
    List<Pair<Integer, byte[]>> chunks = new ArrayList<>();
    for (int index = 0; index <= largeBuffer.length; index += bufferSize) {
      int size = Math.min(bufferSize, largeBuffer.length - index);
      chunks.add(Pair.of(size, Arrays.copyOfRange(largeBuffer, index, index + size)));
    }
    return chunks;
  }

  public static Object deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
    return objectInputStream.readObject();
  }
}
