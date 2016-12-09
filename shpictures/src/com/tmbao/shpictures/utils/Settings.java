package com.tmbao.shpictures.utils;

import java.io.File;
import java.net.InetAddress;

/**
 * Created by Tmbao on 12/3/16.
 */
public class Settings {
  public static final int DEFAULT_PORT = 1234;
  public static final long TIMEOUT = 60000000000000l;
  public static final int SESSIONID_LENGTH = 100;
  public static final int SESSIONID_BASE = 32;
  public static final int BUFFER_SIZE = 2048;
  public static final int BUFFER_CAPACITY = 3072;
  public static final String DATA_BASE_PATH = "data";
  public static final String FORMAT_NAME = "jpg";
  public static final int DISPLAY_WIDTH = 720;
  public static final int DISPLAY_HEIGHT = 576;
  public static final int SLEEP_TIME = 50;

  public static String getFileName(String imageId) {
    return new File(DATA_BASE_PATH, imageId + ".jpg").getPath();
  }
}
