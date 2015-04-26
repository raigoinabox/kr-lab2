package com.rait.search.knowledge;

import com.rait.search.knowledge.parser.Parser;
import com.rait.search.knowledge.parser.ParserFactory;
import com.rait.search.knowledge.searcher.EHRSearcher;
import com.rait.search.knowledge.searcher.GoogleSearcher;
import com.rait.search.knowledge.searcher.KVRealEstateSearcher;
import com.rait.search.knowledge.searcher.Searcher;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import java.lang.InterruptedException;
import java.lang.ProcessBuilder;
import java.lang.Runtime;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Launcher {
  public static void main(String[] args) throws Exception {
    // String[] addresses = {"Mustamäe tee 183", "Ida põik 1", "Ehitajate tee 5"};
    String[] addresses = {"Mustamäe tee 183"};
    new Launcher().runSearch(addresses);
    System.exit(0);
  }

  public void runSearch(String[] addresses){
    Utils.createDir(Configuration.CACHE_DIR);
    for (int i = 0; i < addresses.length; ++i) {
      String searchFor = addresses[i];
      List<Page> searchResult = new ArrayList<>();
      Searcher googleSearcher = new GoogleSearcher();
      Searcher kvSearcher = new KVRealEstateSearcher();
      Searcher ehrSearcher = new EHRSearcher();
      searchResult.addAll(googleSearcher.getPages(searchFor));
      searchResult.addAll(kvSearcher.getPages(searchFor));
      searchResult.addAll(ehrSearcher.getPages(searchFor));

      Map<String, Integer> wordWeightMap = parseSearchResult(searchFor, searchResult);
      printSearchResult(searchFor, wordWeightMap);
      Map<String, Double> normalisedWeightMap = normalisedWeightMap(wordWeightMap);
      runOtter(searchFor, normalisedWeightMap);
    }
  }

  private static void printSearchResult(String searchFor, Map<String, Integer> wordWeightMap) {
    System.out.println(searchFor);
    //System.out.println(wordWeightMap);

    int i = 0;
    for (Map.Entry<String, Integer> entry : wordWeightMap.entrySet()) {
      if (i == 10) {
        break;
      }
      ++i;
      System.out.println(entry);
    }
  }

  private static void runOtter(String searchTerm, Map<String, Double> wordWeightMap) {
    try{
      Scanner scanner = new Scanner(Paths.get("template.otter"));
      List<String> lines = new ArrayList<String>();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.equals("MARK_DATA")) {
          line = "";
          int i = 0;
          for (Map.Entry<String, Double> weightEntry : wordWeightMap.entrySet()) {
            if (i >= 100) {
              break;
            } else {
              line += String.format("word(\"%s\", \"%s\", \"%s\").", searchTerm, weightEntry.getKey(), weightEntry.getValue());
              lines.add(line);
            }
          }
        } else {
          lines.add(line);
        }
      }

      System.out.println("otter");
      ProcessBuilder otterBuilder = new ProcessBuilder("otter");
      otterBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
      otterBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      Process otter = otterBuilder.start();
      PrintWriter writer = new PrintWriter(otter.getOutputStream(), true);
      for (String line : lines) {
        writer.println(line);
      }
      writer.close();
      
      otter.waitFor();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }

  private Map<String, Double> normalisedWeightMap(Map<String, Integer> wordWeightMap) {
    Map<String, Double> normalisedWeightMap = new HashMap<String, Double>();
    for (Map.Entry<String, Integer> weightEntry : wordWeightMap.entrySet()) {
      normalisedWeightMap.put(weightEntry.getKey(), 1 / (1 + Math.exp(-0.01 * (weightEntry.getValue() - 100))));
    }
    return normalisedWeightMap;
  }

  private Map<String, Integer> parseSearchResult(String searchFor, List<Page> searchResult) {
    Map<String, Integer> allImportantWordsTogether = new HashMap<>();
    for (Page page : searchResult) {
      try {
        Parser pageContentParser = ParserFactory.getParser(page);
        pageContentParser.parse(page, searchFor);
        for (Map.Entry<String, Integer> interestingWord : page.getInterestingWords().entrySet()) {
          String word = interestingWord.getKey();
          Integer currentWordRate = interestingWord.getValue();
          Integer wordRate = allImportantWordsTogether.get(word);
          if (wordRate == null) wordRate = new Integer(0);
          wordRate += (currentWordRate * page.getPageRating());
          allImportantWordsTogether.put(word, wordRate);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return Utils.sortMapByValue(allImportantWordsTogether);
  }
}
