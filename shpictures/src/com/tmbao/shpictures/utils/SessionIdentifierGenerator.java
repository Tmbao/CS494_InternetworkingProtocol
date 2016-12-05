package com.tmbao.shpictures.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Tmbao on 12/3/16.
 */
public class SessionIdentifierGenerator {
  private static final SecureRandom RAND = new SecureRandom();

  public static String nextSessionId() {
    return new BigInteger(Settings.SESSIONID_LENGTH, RAND).toString(Settings.SESSIONID_BASE);
  }
}
