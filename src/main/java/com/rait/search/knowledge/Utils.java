package com.rait.search.knowledge;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.rait.search.knowledge.Configuration.*;

public class Utils {
  private static final Logger logger = Logger.getLogger(Utils.class.getName());
  //private static final Logger logger = Logger.getLogger("com.rait.search.knowledge.Utils");

  public static void createDir(String dirName) {
    File cacheDir = new File(dirName);
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
  }

  public static String createHash(String stringToHash) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = messageDigest.digest(stringToHash.getBytes());
      StringBuffer hash = new StringBuffer();
      for (int i = 0; i < hashBytes.length; ++i) {
        hash.append(Integer.toHexString((hashBytes[i] & 0xFF) | 0x100).substring(1, 3));
      }
      return hash.toString();
    } catch (NoSuchAlgorithmException e) {
      logger.severe("Failed to create HASH: " + e.getStackTrace());
    }
    return null;
  }

  private static void saveFile(String fileName, String pageContent) {
    try (PrintWriter writer = new PrintWriter(fileName, ENCODING)) {
      writer.write(pageContent);
    } catch (FileNotFoundException |UnsupportedEncodingException  e) {
      logger.severe("Failed to save file: " + e.getStackTrace());
      e.printStackTrace();
    }
  }

  public static String getPageContent(URL url) {
    try {
      String pageContentFromCache = getPageContentFromCache(url);
      if (pageContentFromCache != null) {
        logger.info("Get page content from cache (" + url.toString() + ") hash (" + createHash(url.toString()) + ")");
        return pageContentFromCache;
      }
      logger.info("Get page content from web (" + url.toString() + ")");
      String pageContentFromWeb = getPageContentFromWeb(url);
      cachePageContent(url, pageContentFromWeb);
      return pageContentFromWeb;
    } catch (Exception e) {
      logger.severe("Failed to get page content: " + e.getMessage() + ": " + e.getStackTrace());
    }
    return "";
  }

  public static void cachePageContent(URL url, String pageContent) {
    String urlHash = createHash(url.toString());
    saveFile(CACHE_DIR + "/" + urlHash, pageContent);
  }

  private static String getPageContentFromWeb(URL url) {
    InputStreamReader inputStreamReader = null;
    try {
      URLConnection connection = url.openConnection();
      connection.setConnectTimeout(CONNECTION_TIMEOUT);
      connection.setReadTimeout(READ_TIMEOUT);
      connection.connect();
      inputStreamReader = new InputStreamReader(connection.getInputStream(), ENCODING);
      int byteValue;
      StringBuilder pageContent = new StringBuilder();
      while ((byteValue = inputStreamReader.read()) != -1) {
        pageContent.append((char)byteValue);
      }
      return pageContent.toString();
    } catch (IOException e) {
      logger.severe("Failed to read page content from web (" + url.toString() + "): " + e.getStackTrace());
    } finally {
        try {
          if (inputStreamReader != null) inputStreamReader.close();
        } catch (IOException e) {}
    }
    return null;
  }

  public static String getPageContentFromCache(URL url) {
    String urlHash = createHash(url.toString());
    File cachedFile = new File(CACHE_DIR + "/" + urlHash);
    if (cachedFile.exists()) {
      try (FileReader fileReader = new FileReader(cachedFile)) {
        int byteValue;
        StringBuilder fileContent = new StringBuilder();
        while ((byteValue = fileReader.read()) != -1) {
          fileContent.append((char)byteValue);
        }
        return fileContent.toString();
      } catch (IOException e) {
        logger.severe("Failed to read page content from cache (" + url.toString() + "): " + e.getStackTrace());
      }
    }
    return null;
  }

  public static boolean stringContainsSearchableWords(String content, String searchFor) {
    String[] words = searchFor.toLowerCase().split(" ");
    content = content.toLowerCase();
    for (String word : words) {
      if (!content.contains(word)) return false;
    }
    return true;
  }

  public static String removePunctuationMarks(String text) {
    return PUNCTUATION_MARKS.matcher(text).replaceAll("");
  }

  public static void findNearbyWords(Page page, String content, String searchString, int nearbyWordCount) {
    searchString = searchString.toLowerCase();
    content = content.toLowerCase();
    String splitPattern = Pattern.quote(searchString);
    String[] searchStringParts = removePunctuationMarks(searchString).split(" ");
    for (String searchStringPart : searchStringParts) {
      // Ignore house numbers
      if (searchStringPart.matches(".*?[a-zA-Z]+.*?")) {
        splitPattern += "|" + Pattern.quote(searchStringPart);
      }
    }
    String[] contentParts = removePunctuationMarks(content).split(splitPattern);

    if (contentParts.length < 2) return;

    for (int i = 0; i < contentParts.length; i++) {
      String[] words = contentParts[i].split(" ");
      // Take words from the beginning
      if (i > 0) {
        int startCount = 0;
        int endCount = (words.length > nearbyWordCount) ? nearbyWordCount : words.length;
        for(int j = startCount, rate = 1; j < endCount; j++, rate++) {
          page.addWordRating(words[j], rate);
        }
      }
      // Take words from the end
      if (i < contentParts.length-1) {
        int startCount = (words.length - nearbyWordCount) > 0 ? (words.length - nearbyWordCount) : 0;
        int endCount = words.length;
        for(int j = startCount, rate = nearbyWordCount; j < endCount; j++, rate--) {
          page.addWordRating(words[j], rate);
        }
      }
    }
  }

  public static Map<String, Integer> sortMapByValue(Map mapToSort) {
    List<Map.Entry<String, Integer>> list = new LinkedList(mapToSort.entrySet());
    Map<String, Integer> sortedMap = new LinkedHashMap<>();
    Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
    list.stream().forEach(o -> sortedMap.put(o.getKey(), o.getValue()));
    return sortedMap;
  }
}
