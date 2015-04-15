package com.rait.search.knowledge.parser;

import com.rait.search.knowledge.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Iterator;
import java.util.logging.Logger;

public class EHRParser implements Parser {
  private static final Logger logger = Logger.getLogger(EHRParser.class.getName());

  @Override
  public void parse(Page page, String searchFor) {
    String pageContent = page.pageContent;
    Document pageHTML = Jsoup.parse(pageContent);

    page.addWordsRating(pageHTML.select("title").text(), 3);

    Iterator<Element> iterator = pageHTML.select("table tbody tr").iterator();
    while (iterator.hasNext()) {
      Element row = iterator.next();
      String buildingAddress = row.select(".search_datatable_buildings_address").text();
      if (buildingAddress.toLowerCase().contains(searchFor.toLowerCase())) {
        String buildingRegisterCode = row.select(".search_datatable_buildings_ehrCode").text();
        String buildingType = row.select(".search_datatable_buildings_building").text();
        String buildingName = row.select(".search_datatable_buildings_buildingName").text();
        String buildingFirstUsage = row.select(".search_datatable_buildings_firstUsage").text();
        String buildingFloorCount = row.select(".search_datatable_buildings_maxFloorCount").text();
        String buildingArea = row.select(".search_datatable_buildings_area").text();

        page.addWordRating("ehitisregistri kood: " + buildingRegisterCode, 5);
        page.addWordRating(buildingType, 5);
        page.addWordRating(buildingName, 5);
        page.addWordsRating(buildingAddress, 5);
        page.addWordRating("esmane kasutus: " + buildingFirstUsage, 5);
        page.addWordRating("korruste arv: " + buildingFloorCount, 5);
        page.addWordRating("ehitisealune pindala: " + buildingArea, 5);
      }
    }
  }
}
