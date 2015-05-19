package web.tool;

import date.Dates;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selenium.WebDriverHub;
import web.tool.eg.kompass.com.Companies;
import web.tool.eg.kompass.com.Company;
import web.tool.eg.kompass.com.Kompass;

import java.util.Calendar;
import java.util.concurrent.Callable;

/**
 * User: YamStranger
 * Date: 5/17/15
 * Time: 9:20 AM
 */
public class CompanyDataReader implements Callable<Company> {
    private static final Logger logger = LoggerFactory.getLogger(CompanyDataReader.class);
    private final String url;
    private final boolean needLogin;
    private final WebDriverHub hub;
    private String login;
    private String password;
    private boolean waiting;
    private final Dates created;


    public CompanyDataReader(final String url, final WebDriverHub hub) {
        this(url, hub, "", "");
    }

    public CompanyDataReader(final String url, final WebDriverHub hub, final String login, final String password) {
        this.hub = hub;
        this.login = login;
        this.password = password;
        this.url = url;
        this.created = new Dates();
        this.waiting = true;
        this.needLogin = !(login.isEmpty() && password.isEmpty());
    }

    public boolean waiting() {
        return this.waiting;
    }

    public long age() {
        return this.created.difference(new Dates(), Calendar.SECOND);
    }

    @Override
    public Company call() throws Exception {
        final WebDriver driver = this.hub.driver(); //waits until new one available
        final WebDriverWait wait = this.hub.driverWait(driver);
        waiting = false;
        logger.trace("CompanyDataReader started");
        final Kompass kompass = new Kompass(driver, wait);
        try {

            boolean isLogined = false;
            if (this.needLogin && !kompass.isLogged()) {
                try {
                    kompass.login(this.login, this.password);
                    isLogined = true;
                } catch (Exception e) {
                    isLogined = false;
                    logger.info("user:Login/password error, trying without authorization");
                }
            }
            kompass.navigate(url);
            final Companies reader = new Companies(kompass, isLogined);
            final Company company = reader.load();
            return company;
        } finally {
            kompass.quit();
        }
    }
}
