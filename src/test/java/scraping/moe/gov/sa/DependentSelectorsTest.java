package scraping.moe.gov.sa;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import selenium.elements.Condition;
import selenium.elements.ElementNotFound;
import selenium.elements.Search;

import java.io.File;
import java.util.List;

/**
 * User: YamStranger
 * Date: 4/29/15
 * Time: 4:28 PM
 */
public class DependentSelectorsTest {
    @Test
    public void load_dropdownList_proceed() throws ElementNotFound {
        ChromeDriver driver = null;
        try {
            System.setProperty("webdriver.chrome.driver", "chrome-web-driver\\chromedriver.exe");
            driver = new ChromeDriver();
            driver.get("https://www.moe.gov.sa/English/EServices/Public/Pages/SchoolByEducationalOffice.aspx");
            final Search district = new Search("Please select District",
                    new Condition(By.xpath("//*[@id=\"ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_ddlDistrictID\"]")));
            final Search office = new Search("Please Select Educational Office",
                    new Condition(By.xpath("//*[@id=\"ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_ddlEducationalOffice\"]")));

            final DependableSelectors selectors = new DependableSelectors(driver, district, office);
            List<DependableSelectors.Option> options = selectors.load();
            File scrsht = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        } catch (Throwable e) {
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
