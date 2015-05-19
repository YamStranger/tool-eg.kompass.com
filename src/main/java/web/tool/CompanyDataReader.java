package web.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.tool.eg.kompass.com.Companies;
import web.tool.eg.kompass.com.Company;
import web.tool.eg.kompass.com.Kompass;

import java.util.concurrent.Callable;

/**
 * User: YamStranger
 * Date: 5/17/15
 * Time: 9:20 AM
 */
public class CompanyDataReader implements Callable<Company> {
    private static final Logger logger = LoggerFactory.getLogger(CompanyDataReader.class);
    private final Kompass kompass;
    private final String url;
    private final boolean isLogined;


    public CompanyDataReader(String url, Kompass kompass, boolean isLogined) {
        this.kompass = kompass;
        this.url = url;
        this.isLogined = isLogined;
    }

    @Override
    public Company call() throws Exception {
        try {
            this.kompass.navigate(url);
            Companies reader = new Companies(this.kompass, isLogined);
            Company company = reader.load();
            return company;
        } finally {
            this.kompass.quit();
        }
    }
}
