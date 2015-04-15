package com.rait.search.knowledge.searcher;

import com.rait.search.knowledge.Page;

import java.util.List;

public interface Searcher {
  public List<Page> getPages(String searchString);
}
