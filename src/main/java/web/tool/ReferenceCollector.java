package web.tool;

import date.Dates;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selenium.WebDriverHub;
import web.tool.eg.kompass.com.Kompass;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * User: YamStranger
 * Date: 5/16/15
 * Time: 9:49 AM
 */
public class ReferenceCollector implements Callable<List<String>> {
    private final String keyword;
    private final Integer startPage;
    private final Integer endPage;
    private final WebDriverHub hub;
    private static final Logger logger = LoggerFactory.getLogger(ReferenceCollector.class);
    private boolean waiting;
    private final Dates created;

    public ReferenceCollector(WebDriverHub hub, String keyword, Integer startPage, Integer endPage) {
        this.keyword = keyword;
        this.startPage = startPage;
        this.endPage = endPage;
        this.hub = hub;
        this.waiting = true;
        this.created = new Dates();
    }

    public boolean waiting() {
        return this.waiting;
    }

    public long age() {
        return this.created.difference(new Dates(), Calendar.SECOND);
    }

    @Override
    public List<String> call() throws Exception {
        final WebDriver driver = this.hub.driver(); //waits until new one available
        final WebDriverWait wait = this.hub.driverWait(driver);
        this.waiting = false;
        logger.trace("ReferenceCollector started");
        try {
            List<String> companies = new LinkedList<>();
            final Kompass kompass = new Kompass(driver, wait);
            kompass.mainPage().searchPage();
            kompass.searchPage();
            kompass.search(this.keyword);
            kompass.scrollTo(startPage - 1);
            while (kompass.hasNextPage()) {
                Dates start = new Dates();
                kompass.nextPage();
                if (kompass.currentPages() >= this.startPage && kompass.currentPages() <= this.endPage) {
                    for (final String ref : kompass.results()) {
                        companies.add(ref);
                    }
                    logger.info("user:Collecting refs from page " + kompass.currentPages() + " takes " + new Dates().difference(start, Calendar.SECOND) + " seconds");
                } else {
                    break;
                }
            }
            return companies;
        } finally {
            driver.quit();
        }
    }
}
