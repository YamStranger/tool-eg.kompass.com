package scraping.moe.gov.sa;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * User: YamStranger
 * Date: 4/30/15
 * Time: 3:40 PM
 */
public class Initializer {
    private static boolean incognito = false;

    public Initializer() {
    }

    WebDriver driver() {
        System.setProperty("webdriver.chrome.driver", "chrome-web-driver\\chromedriver.exe");
//        Path path = Paths.get("").toAbsolutePath();
//        path = path.resolve(Paths.get("sessions"));
//        path = path.resolve(Paths.get(UUID.randomUUID().toString()));
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("user-data-dir=" + path.toAbsolutePath().toString());
        if (isIncognito()) {
            options.addArguments("--incognito");
        }
        //options.addArguments("--disable-images");
        // options.addExtensions("Block-image_v1.0.crx");

//        System.out.println("creating new chrome with user-data-dir=" + path.toAbsolutePath().toString());
        ChromeDriver driver = new ChromeDriver(options);
        return driver;
    }

    private synchronized boolean isIncognito() {
        Initializer.incognito = !Initializer.incognito;
        return Initializer.incognito;
    }

}
