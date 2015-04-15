package com.rait.search.knowledge.parser;

import com.rait.search.knowledge.Page;
import com.rait.search.knowledge.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;
import java.util.logging.Logger;

public class BasicParser implements Parser {
  private static final Logger logger = Logger.getLogger(BasicParser.class.getName());

  @Override
  public void parse(Page page, String searchFor) {
    String pageContent = page.pageContent;

    // Parse only HTML pages
    if (!pageContent.toLowerCase().contains("<html")) {
      logger.info("Not HTML page: " + page.url);
      return;
    }

    Document pageHTML = Jsoup.parse(pageContent);

    // TITLE
    Elements title = pageHTML.select("title");
    page.addWordsRating(title.text(), 4);

    //H1
    Elements h1 = pageHTML.select("h1");
    Iterator<Element> h1Iterator = h1.iterator();
    while (h1Iterator.hasNext()) {
      page.addWordsRating(h1Iterator.next().text(), 4);
    }

    //H2
    /*
    Elements h2 = pageHTML.select("h2");
    Iterator<Element> h2Iterator = h2.iterator();
    while (h2Iterator.hasNext()) {
      page.addWordsRating(h2Iterator.next().text(), 2);
    }
    */

    //ADDRESS
    Elements address = pageHTML.select("address");
    Iterator<Element> addressIterator = address.iterator();
    while (addressIterator.hasNext()) {
      page.addWordsRating(addressIterator.next().text(), 2);
    }

    // Meta info
    Elements metaInfo = pageHTML.select("meta");
    for (int i = 0; i < metaInfo.size(); i++) {
      String metaName = metaInfo.get(i).attr("name").toLowerCase();
      String metaProperty = metaInfo.get(i).attr("property").toLowerCase();
      String metaContent = metaInfo.get(i).attr("content");

      if (metaName.equals("description") || metaName.equals("keywords") ||
          metaProperty.equals("og:title") || metaProperty.equals("og:description")) {
        page.addWordsRating(metaContent, 2);
      }
    }

    // Nearby words
    Utils.findNearbyWords(page, pageHTML.text(), searchFor, 5);
  }
}
