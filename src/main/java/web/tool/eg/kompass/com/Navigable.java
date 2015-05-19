package web.tool.eg.kompass.com;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * User: YamStranger
 * Date: 5/14/15
 * Time: 3:08 PM
 */
public interface Navigable {
    public WebDriver webDriver();
    public WebDriverWait webDriverWait();
}
