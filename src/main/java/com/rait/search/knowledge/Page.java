package com.rait.search.knowledge;

import java.net.URL;
import java.util.*;

import static com.rait.search.knowledge.Configuration.IGNORE_WORDS;

public class Page {
  public URL url;
  public String pageContent;

  // word - weight/importance/rating
  private Map<String, Integer> interestingWords = new HashMap<>();

  public void addWordRating(String word, int rate) {
    word = word.trim().toLowerCase();
    if (word.isEmpty()) return;
    if (IGNORE_WORDS.contains(word)) return;
    Integer wordRate = interestingWords.get(word);
    wordRate = (wordRate == null) ? new Integer(0) : wordRate;
    wordRate += rate;
    interestingWords.put(word, wordRate);
  }

  public void addWordsRating(String words, int rate) {
    String[] wordsList = Utils.removePunctuationMarks(words).split(" ");
    for (String word : wordsList) {
      addWordRating(word, rate);
    }
  }

  public Map<String, Integer> getInterestingWordsInSortedOrder() {
    return Utils.sortMapByValue(interestingWords);
  }

  public Map<String, Integer> getInterestingWords() {
    return interestingWords;
  }

  public int getPageRating() {
    String pageUrl = url.toString().toLowerCase();
    if (pageUrl.contains("www.kv.ee") && !pageUrl.contains("?act=search")) return 3;
    if (pageUrl.contains("www.ehr.ee/app/otsing")) return 20;
    if (pageUrl.contains("wikipedia.org")) return 3;
    if (pageUrl.contains("city24.ee")) return 2;
    return 1;
  }
}
