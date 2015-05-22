package web.tool.eg.kompass.com;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selenium.elements.Condition;
import selenium.elements.ElementNotFoundException;
import selenium.elements.Search;

import java.util.LinkedList;
import java.util.List;

/**
 * User: YamStranger
 * Date: 5/14/15
 * Time: 11:23 PM
 */
public class Companies {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final boolean isLogined;
    private final Search companySearch = new Search("Company info element", new Condition(By.xpath("//*/div[@class=\"headerDetailsCompany\"]")));
    private final Search coordinatesSearch = new Search("company coordinates element", new Condition(By.xpath("div[@class=\"headerRowCoordinates\"]/div[@class=\"coordinates\"]/ul")));
    private final Search phonesSearch = new Search("company phones element", new Condition(By.xpath("div[@class=\"headerRowCoordinates\"]/div[@class=\"phoneCompany\"]/div[@class=\"phone\"]/p[@id=\"phone\"]/a")));
    private final Search faxSearch = new Search("company fax element", new Condition(By.xpath("li[@class=\"fax\"]/p")));
    private final Search mailSearch = new Search("company mail elements", new Condition(By.xpath("li[@class=\"mail\"]/p/a")));
    private final Search mailHiddenSearch = new Search("company mail elements", new Condition(By.xpath("li[@class=\"mail\"]/p/span")));
    private final Search showSiteSearch = new Search("company website button to show ref", new Condition(By.xpath("li[@class=\"website\"]/p/a")));
    private final Search nameSearch = new Search("Company name", new Condition(By.xpath("div[@class=\"headerRow\"]/h1")));
    private final Search infoSearch = new Search("Company info", new Condition(By.xpath("//div[@class=\"headerRow\"]/div[@class=\"headerRowLeft\"]")));
    private final Search addressSearch = new Search("Company address", new Condition(By.xpath("div[@class=\"headerRowTop\"]/div[@class=\"infos\"]/div[@class=\"addressCoordinates\"]/p")));
    private static final Logger logger = LoggerFactory.getLogger(Companies.class);

    public Companies(Kompass kompass, boolean isLogined) {
        this.driver = kompass.webDriver();
        this.wait = kompass.webDriverWait();
        this.isLogined = isLogined;
    }

    public Company load() throws ElementNotFoundException {
        String name = "";
        String address = "";
        List<String> phones = new LinkedList<>();
        List<String> faxes = new LinkedList<>();
        List<String> mails = new LinkedList<>();
        List<String> website = new LinkedList<>();
        String url = this.driver.getCurrentUrl();
        boolean isNeedAuthorization = false;
        String main = driver.getWindowHandle();
        WebElement top = this.companySearch.one(this.driver);
        name = this.nameSearch.one(top).getText().trim();
        WebElement information = this.infoSearch.one(top);
        try {
            address = this.addressSearch.one(information).getText().replaceAll("[\\n\\r\\t]", "");
        } catch (ElementNotFoundException e) {
            //address is empty
            logger.info("user:address is empty " + url);
        }
        WebElement coordinates = this.coordinatesSearch.one(information);
        int newWindow = 0;
        try {
            final List<WebElement> refs = this.showSiteSearch.all(coordinates);
            for (final WebElement ref : refs) {
                website.add(ref.getAttribute("href"));
            }
        } catch (ElementNotFoundException e) {
            //site is absent
            logger.error("site is not present " + url);
        }

        try {
            List<WebElement> faxElements = this.faxSearch.all(coordinates);
            for (final WebElement element : faxElements) {
                String fax = element.getText().replaceAll(" ", "");
                faxes.add(fax);
            }
        } catch (ElementNotFoundException e) {
            //fax is absent
            logger.error("fax is absent " + url);
        }
        try {
            List<WebElement> mailElements = this.mailSearch.all(coordinates);
            if (mailElements.isEmpty()) {
                //check if emails are hiden
                if (!this.mailHiddenSearch.all(coordinates).isEmpty()) {
                    isNeedAuthorization = true;
                    logger.error("need authorization for " + url);
                }
            }
            for (final WebElement element : mailElements) {
                final String mail = element.getText();
                mails.add(mail);
            }
        } catch (ElementNotFoundException e) {
            //mail is absent
            logger.info("mail is absent " + url);
        }
        try {
            WebElement button = this.phonesSearch.one(information);
            phones.add(button.getAttribute("href").replaceAll("\\D+", ""));
        } catch (ElementNotFoundException e) {
            //phone is absent
            logger.info("phone is absent " + url);
        }

        return new Company(name, address, phones, faxes, mails, website, url, isNeedAuthorization, isLogined);
    }

    private String getName() {
        return Thread.currentThread().getName();
    }
}
