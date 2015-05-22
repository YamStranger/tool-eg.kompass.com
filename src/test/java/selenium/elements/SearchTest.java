package selenium.elements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

/**
 * User: YamStranger
 * Date: 4/30/15
 * Time: 10:18 AM
 */
public class SearchTest {
    @Test
    public void find_validObject_proceed() throws ElementNotFound {
        ChromeDriver driver = null;
        try {
            System.setProperty("webdriver.chrome.driver", "chrome-web-driver\\chromedriver.exe");
            driver = new ChromeDriver();
            driver.get("https://www.moe.gov.sa/English/EServices/Public/Pages/SchoolByEducationalOffice.aspx");
            Search search = new Search("Please select District", new Condition(By.xpath("//*[@class=\"FormItemContainer Big fR\"]"),
                    new Condition(By.xpath("div[@class=\"FormLabel fR\"]"),
                            new Condition(By.tagName("span"), "Please select District.*"))),
                    new Condition(By.xpath("div[@class=\"FormDropDown fR\"]")),
                    new Condition(By.tagName("select"))
            );
            WebElement element = search.one(driver);
        } catch (Throwable e) {
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        //*[@id="ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_lblDistrictID"]
// //*[@class="FormItemContainer Big fR"]/div[@class="FormLabel fR"]/span[normalize-space(text())=normalize-space("Please select District ")]
// div[@class="FormDropDown fR"]/span[]
        //*[@id="ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_ddlDistrictID"]
        // //*[@id="ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_ddlDistrictID"]
        //*[@id="ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_UpdatePnlSchool"]/div[2]/div[1]/div[3]

    }
}
