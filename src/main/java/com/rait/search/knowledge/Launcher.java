package com.rait.search.knowledge;

import com.rait.search.knowledge.parser.Parser;
import com.rait.search.knowledge.parser.ParserFactory;
import com.rait.search.knowledge.searcher.EHRSearcher;
import com.rait.search.knowledge.searcher.GoogleSearcher;
import com.rait.search.knowledge.searcher.KVRealEstateSearcher;
import com.rait.search.knowledge.searcher.Searcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Launcher {
  public static void main(String[] args) throws Exception {
    String[] addresses = {"Mustamäe tee 183", "Ida põik 1", "Ehitajate tee 5"};
    new Launcher().runSearch(addresses);
    System.exit(0);
  }

  public void runSearch(String[] addresses){
    Utils.createDir(Configuration.CACHE_DIR);
    Scanner sc = new Scanner(System.in);
    for (int i = 0; i < addresses.length; ++i) {
      String searchFor = addresses[i];
      List<Page> searchResult = new ArrayList<>();
      Searcher googleSearcher = new GoogleSearcher();
      Searcher kvSearcher = new KVRealEstateSearcher();
      Searcher ehrSearcher = new EHRSearcher();
      searchResult.addAll(googleSearcher.getPages(searchFor));
      searchResult.addAll(kvSearcher.getPages(searchFor));
      searchResult.addAll(ehrSearcher.getPages(searchFor));

      parseSearchResult(searchFor, searchResult);

      if (i < addresses.length - 1) {
        System.out.println("Press enter for more results");
        sc.nextLine();
      }
    }
    sc.close();
  }

  private void parseSearchResult(String searchFor, List<Page> searchResult) {
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
    printSearchResult(searchFor, Utils.sortMapByValue(allImportantWordsTogether));
  }

  private void printSearchResult(String searchFor, Map<String, Integer> wordWeightMap) {
    System.out.println(searchFor);
    System.out.println(wordWeightMap);
    //System.out.println(Utils.sortMapByValue(allImportantWordsTogether).size());
  }
}
