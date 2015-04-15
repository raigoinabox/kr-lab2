package com.rait.search.knowledge.parser;

import com.rait.search.knowledge.Page;

public class ParserFactory {
  public static Parser getParser(Page page) {
    String url = page.url.toString().toLowerCase();
    if (url.contains("www.kv.ee") && !url.contains("?act=search")) {
      return new KVRealEstateParser();
    }
    if (url.contains("www.ehr.ee/app/otsing")) {
      return new EHRParser();
    }
    return new BasicParser();
  }
}
