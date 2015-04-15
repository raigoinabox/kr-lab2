
package com.rait.search.knowledge.searcher;

import com.rait.search.knowledge.Configuration;
import com.rait.search.knowledge.Page;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.rait.search.knowledge.Configuration.ENCODING;
import static com.rait.search.knowledge.Utils.getPageContent;
import static com.rait.search.knowledge.Utils.stringContainsSearchableWords;

public class GoogleSearcher implements Searcher {
  private static final Logger logger = Logger.getLogger(GoogleSearcher.class.getName());

  @Override
  public List<Page> getPages(String searchString) {
    int start = 0;
    int linksPerPage = 8;
    int depth = 8;
    URL searchUrl;
    List<Page> pages = new ArrayList<>();
    while (depth-- > 0) {
      logger.finest("Google search depth: " + depth);
      try {
        searchUrl = getSearchUrl(searchString, start, linksPerPage);
        String pageContent = getPageContent(searchUrl);
        JSONObject jsonObject = new JSONObject(pageContent);
        int responseStatus = jsonObject.getInt("responseStatus");
        if (responseStatus != 200) break;

        JSONArray results = jsonObject.getJSONObject("responseData").getJSONArray("results");
        int resultCount = results.length();
        for (int i = 0; i < resultCount; i++) {
          JSONObject searchResult = results.getJSONObject(i);
          String content = searchResult.getString("content");
          String url = searchResult.getString("unescapedUrl");
          if (!stringContainsSearchableWords(content, searchString)) continue;
          if (isUrlForbidden(url)) continue;

          Page page = new Page();
          page.url = new URL(url);
          page.pageContent = getPageContent(page.url);
          page.addWordsRating(Jsoup.parse(content).text(), 2);
          pages.add(page);
        }
      } catch (UnsupportedEncodingException | MalformedURLException e) {
        logger.severe("Failed to read google results: " + e.getStackTrace());
      }
      start += linksPerPage;
    }
    return  pages;
  }

  private boolean isUrlForbidden(String url) {
    for (String forbiddenSite : Configuration.FORBIDDEN_SITES) {
      if (url.toLowerCase().contains(forbiddenSite)) return true;
    }
    return false;
  }

  private URL getSearchUrl(String searchString, int start, int linksPerPage) throws UnsupportedEncodingException, MalformedURLException {
    String urlString = "https://ajax.googleapis.com/ajax/services/search/web?v=1.0" +
        "&rsz=" + linksPerPage +
        "&start=" + start +
        "&q=" + URLEncoder.encode("\"" + searchString + "\"", ENCODING);
    return new URL(urlString);
  }
}
