package scraping.moe.gov.sa;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import selenium.elements.AllElementsLoaded;
import selenium.elements.Condition;
import selenium.elements.ElementNotFound;
import selenium.elements.Search;

/**
 * User: YamStranger
 * Date: 4/30/15
 * Time: 9:25 AM
 */
public class DropDownList {
    private final Search search;
    private final ExpectedCondition<Boolean> expectation;
    private final WebDriver driver;

    public DropDownList(final WebDriver driver, final Search search) {
        this(driver, search, new AllElementsLoaded(
                new Search("loading window", new Condition(
                        By.xpath("//*[@id=\"ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_updProgressSchool\"]")))));
    }

    public DropDownList(final WebDriver driver, final Search search,
                        final ExpectedCondition<Boolean> expectation) {
        this.search = search;
        this.expectation = expectation;
        this.driver = driver;
    }

    /**
     * Selects in current dropDown list option by value, and wait until page will
     * be updated according expectation
     *
     * @param value
     */
    public void select(final String value) throws ElementNotFound {
//        final WebElement element = this.search.one(driver);
        final Select select = new Select(this.search.one(driver));
//        select.deselectAll();
        select.selectByValue(value);
        this.search.one(driver).submit();
        try {
            Thread.sleep(50);
            (new WebDriverWait(driver, 30)).until(this.expectation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
