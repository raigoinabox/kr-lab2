package com.rait.search.knowledge;

import com.rait.search.knowledge.parser.Parser;
import com.rait.search.knowledge.parser.ParserFactory;
import com.rait.search.knowledge.searcher.EHRSearcher;
import com.rait.search.knowledge.searcher.GoogleSearcher;
import com.rait.search.knowledge.searcher.KVRealEstateSearcher;
import com.rait.search.knowledge.searcher.Searcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Launcher {
  public static void main(String[] args) throws Exception {
    String[] addresses = {"Mustamäe tee 183", "Ida põik 1", "Ehitajate tee 5"};
    new Launcher().runSearch(addresses);
  }

  public void runSearch(String[] addresses){
    Utils.createDir(Configuration.CACHE_DIR);
    for (String searchFor : addresses) {
      List<Page> searchResult = new ArrayList<>();
      Searcher googleSearcher = new GoogleSearcher();
      Searcher kvSearcher = new KVRealEstateSearcher();
      Searcher ehrSearcher = new EHRSearcher();
      searchResult.addAll(googleSearcher.getPages(searchFor));
      searchResult.addAll(kvSearcher.getPages(searchFor));
      searchResult.addAll(ehrSearcher.getPages(searchFor));

      parseSearchResult(searchFor, searchResult);

    }
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
      } catch (Exception e) {}
    }
    System.out.println(searchFor);
    System.out.println(Utils.sortMapByValue(allImportantWordsTogether));
    //System.out.println(Utils.sortMapByValue(allImportantWordsTogether).size());
  }
}
