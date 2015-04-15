package com.rait.search.knowledge.parser;

import com.rait.search.knowledge.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.logging.Logger;

public class KVRealEstateParser implements Parser {
  private static final Logger logger = Logger.getLogger(KVRealEstateParser.class.getName());

  @Override
  public void parse(Page page, String searchFor) {
    String pageContent = page.pageContent;
    Document pageHTML = Jsoup.parse(pageContent);

    page.addWordsRating(pageHTML.select("title").text(), 3);
    page.addWordsRating(pageHTML.select("h1.title").text(), 3);
    page.addWordsRating(pageHTML.select(".object-article .object-price strong").text(), 3);
    page.addWordsRating(pageHTML.select(".object-article .object-price .object-m2-price").text(), 3);
    //page.addWordsRating(pageHTML.select(".object-article .object-data-meta").text(), 3);
    pageHTML.select(".object-article .object-data-meta tr").stream().forEach(e -> page.addWordRating(e.text(), 3));
    page.addWordsRating(pageHTML.select(".object-article .object-article-body").text(), 1);
  }
}
