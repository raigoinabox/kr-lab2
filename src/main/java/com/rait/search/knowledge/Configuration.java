package com.rait.search.knowledge;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class Configuration {
  public static final String CACHE_DIR = "cache";
  public static final int CONNECTION_TIMEOUT = 15000;
  public static final int READ_TIMEOUT = 15000;
  public static final String ENCODING = "UTF-8";
  public static final String OTTER_TEMPLATE = "template.otter";
  public static final String[] FORBIDDEN_SITES = {"map.ee", "kaart.delfi.ee"};
  public static final Pattern PUNCTUATION_MARKS = Pattern.compile("[\\[\\]\\-\\|(){},.;!?<>]");
  public static final List IGNORE_WORDS = asList("ja", "ning", "ega", "ehk");
}
