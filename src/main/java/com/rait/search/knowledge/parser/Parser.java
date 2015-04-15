package com.rait.search.knowledge.parser;

import com.rait.search.knowledge.Page;

public interface Parser {
  public void parse(Page page, String searchFor) throws Exception;
}
