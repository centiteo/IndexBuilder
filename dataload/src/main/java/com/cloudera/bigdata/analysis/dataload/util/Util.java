package com.cloudera.bigdata.analysis.dataload.util;

import org.apache.hadoop.hbase.util.Bytes;

public class Util {

  public static boolean checkIsEmpty(String value) {
    if (null == value || value.isEmpty()) {
      return true;
    }

    return false;
  }

  public static byte[][] genSplitKeysAlphaDig(String prefixesStr, int splitSize)
      throws Exception {
    if (prefixesStr == null) {
      prefixesStr = "";
    }
    String[] prefixes = prefixesStr.split(",");
    int round = 1;
    int num = 0;
    if (!checkSplitSizeAlphaDig(splitSize)) {
      throw new RuntimeException(
          "The split size should be a number with 62^n.");
    }
    if (prefixesStr.equals("") || prefixes.length == 0) {
      prefixes = new String[62];
      for (int i = 0; i < 10; i++) {
        prefixes[i] = new String(new char[] { (char) ('0' + i) });
      }
      for (int i = 10; i < 36; i++) {
        prefixes[i] = new String(new char[] { (char) ('A' + (i - 10)) });
      }
      for (int i = 36; i < 62; i++) {
        prefixes[i] = new String(new char[] { (char) ('a' + (i - 36)) });
      }
    }
    prefixes = trimStrings(prefixes);

    while (round < splitSize) {
      num++;
      round *= 62;
    }

    byte[][] splitKeys = new byte[splitSize * prefixes.length][];

    char[] alphadigit = new char[num];
    String[] suffixes = new String[splitSize];
    for (int i = 0; i < splitSize; i++) {
      suffixes[i] = "";
    }
    if (num > 0) {
      for (int i = 0; i < splitSize; i++) {
        int k = num - 1;
        int j = i;
        while (j != 0) {
          alphadigit[k--] = mapChar(j % 62);
          j /= 62;
        }
        for (int m = 0; m <= k; m++) {
          alphadigit[m] = '0';
        }
        
        String suffix = new String(alphadigit);
        suffixes[i] = suffix;
      }
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < prefixes.length; i++) {
      for (int j = 0; j < suffixes.length; j++) {
        splitKeys[i * suffixes.length + j] =
            Bytes.toBytes(prefixes[i] + suffixes[j]);
        sb.append(prefixes[i] + suffixes[j] + "|");
      }
    }

    return splitKeys;
  }

  private static char mapChar(int val) {
    if (val <= 9) {
      return (char) ('0' + val);
    } else if (val >= 10 && val <= 35) {
      return (char) ('A' + val - 10);
    } else {
      return (char) ('a' + val - 36);
    }
  }

  private static String[] trimStrings(String[] orignal) {
    String[] result = new String[orignal.length];

    for (int i = 0; i < orignal.length; i++) {
      result[i] = orignal[i].trim();
    }

    return result;
  }

  private static boolean checkSplitSizeAlphaDig(int splitSize) {
    if (splitSize < 1) {
      return false;
    }

    while (true) {
      if (splitSize < 62) {
        if (splitSize == 1) {
          return true;
        } else {
          return false;
        }
      } else {
        if (splitSize % 62 != 0) {
          return false;
        }
        splitSize /= 62;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Util.genSplitKeysAlphaDig("", 1);
  }
}
