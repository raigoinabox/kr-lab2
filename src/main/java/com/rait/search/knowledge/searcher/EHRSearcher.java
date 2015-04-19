
package com.rait.search.knowledge.searcher;

import com.rait.search.knowledge.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.rait.search.knowledge.Utils.cachePageContent;
import static com.rait.search.knowledge.Utils.getPageContentFromCache;

public class EHRSearcher implements Searcher {
  private static final Logger logger = Logger.getLogger(EHRSearcher.class.getName());
  private static String pageUrl = "https://www.ehr.ee/app/otsing";

  @Override
  public List<Page> getPages(String searchString) {
    List<Page> pages = new ArrayList<>();
    try {
      URL url = new URL(pageUrl + "/" + searchString);
      String pageContent = getPageContentFromCache(url);
      if (pageContent == null) {
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setJavascriptEnabled(true);
        if (!System.getProperty("os.name").equalsIgnoreCase("Linux")) {
          capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "bin/phantomjs.exe");
        }
        String[] phantomArgs = new  String[] {
            "--webdriver-loglevel=NONE"
        };
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        WebDriver driver = new PhantomJSDriver(capabilities);
        driver.get(pageUrl);
        driver.findElement(By.id("s")).sendKeys(searchString);
        driver.findElement(By.id("id5")).click();
        Thread.sleep(1000);
        pageContent = driver.getPageSource();
        driver.quit();

        cachePageContent(url, pageContent);
      }
      Page page = new Page();
      page.url = url;
      page.pageContent = pageContent;
      pages.add(page);
    } catch (Exception e) {
      System.out.println("error: " + e.getMessage());
      e.printStackTrace();
    }
    return  pages;
  }
}
