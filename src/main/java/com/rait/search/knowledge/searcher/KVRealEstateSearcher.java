
package com.rait.search.knowledge.searcher;

import com.rait.search.knowledge.Configuration;
import com.rait.search.knowledge.Page;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.rait.search.knowledge.Utils.getPageContent;

public class KVRealEstateSearcher implements Searcher {
  private static final Logger logger = Logger.getLogger(KVRealEstateSearcher.class.getName());
  private static final int PAGE_SIZE = 1000;

  @Override
  public List<Page> getPages(String searchString) {
    List<Page> pages = new ArrayList<>();
    try {
      URL[] searchUrls = getSearchUrl(searchString);
      for (URL searchUrl : searchUrls) {
        String pageContent = getPageContent(searchUrl);
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new StringReader(pageContent));
        Element rssNode = document.getRootElement();
        Element channelNode = rssNode.getChildren("channel").get(0);
        List<Element> items = channelNode.getChildren("item");
        for (Element item : items) {
          String title = item.getChildText("title");
          String url = item.getChildText("link");

          Page page = new Page();
          page.url = new URL(url);
          page.pageContent = getPageContent(page.url);
          page.addWordsRating(title, 2);
          pages.add(page);
        }
      }
    } catch (UnsupportedEncodingException | MalformedURLException e) {
      logger.severe("Failed to read KV results: " + e.getMessage());
    } catch (JDOMException | IOException e) {
      logger.warning("Cannot parse KV results: " + e.getMessage());
    }
    return  pages;
  }

  private URL[] getSearchUrl(String searchString) throws UnsupportedEncodingException, MalformedURLException {
    String saleUrlQuery = URLEncoder.encode("act=search.simple&company_id=&page=1&orderby=ob&page_size=" + PAGE_SIZE + "&deal_type=1&dt_select=1&county=0&parish=&price_min=&price_max=&price_type=1&rooms_min=&rooms_max=&area_min=&area_max=&floor_min=&floor_max=&keyword=" + searchString, Configuration.ENCODING);
    String rentUrlQuery = URLEncoder.encode("act=search.simple&company_id=&page=1&orderby=ob&page_size=" + PAGE_SIZE + "&deal_type=2&dt_select=1&county=0&parish=&price_min=&price_max=&price_type=1&rooms_min=&rooms_max=&area_min=&area_max=&floor_min=&floor_max=&keyword=" + searchString, Configuration.ENCODING);
    String pageURl = "http://www.kv.ee/?act=rss.objectsearch&qry=";

    URL[] urls = {
        new URL(pageURl + saleUrlQuery),
        new URL(pageURl + rentUrlQuery)
    };
    return urls;
  }
}
