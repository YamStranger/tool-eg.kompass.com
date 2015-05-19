package web.tool.eg.kompass.com;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selenium.elements.*;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: YamStranger
 * Date: 5/14/15
 * Time: 2:52 PM
 */
public class Kompass implements Navigable {
    private static final Logger logger = LoggerFactory.getLogger(Kompass.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Search page = new Search("Main window loaded",
            new Condition(By.xpath("//html/head/script[contains(@src,\"nr-632.min.js\")]")));
    private final Search searchMainInput = new Search("Main window search text input",
            new Condition(By.xpath("//html/body/div[@id=\"wrapper\"]/div[@id=\"page\"]/div[@id=\"content\"]/div[@id=\"HomepageSearch\"]/div[@class=\"wrapHome\"]/div[@class=\"searchbox\"]/form/div[contains(@class,\"clearfix\")]/input[@id=\"search-main\"]")));
    private final Search searchHeaderInput = new Search("Search window text input",
            new Condition(By.xpath("//html/body/div[@id=\"wrapper\"]/header[@role=\"banner\"]/div[contains(@class, \"searchbox\")]/div[@class=\"wrapHeader\"]/form/div[contains(@class,\"search\")]/div[contains(@class,\"searchEngineMiddle\")]/input[@id=\"search_header\"]")));
    private final Search next = new Search("Next page button on search window",
            new Condition(By.xpath("//html/body/div[contains(@id,\"wrapper\")]/div[@id=\"page\"]/div[@id=\"content\"]/div/div[contains(@class,\"result\")]/form/ul/li/a"),
                    new Condition(By.xpath("i[contains(@class,\"icon-chevron-right\")]"))));
    private final Search previous = new Search("Previous page button on search window",
            new Condition(By.xpath("/html//*[contains(@href,\"scroll?\")]"),
                    new Condition(By.xpath("i[contains(@class,\"icon-chevron-left\")]"))));
    private final Search loading = new Search("New page loading", new Condition(By.xpath("//html/body/div[contains(@class,\"blockOverlay\")]")));
    private final Search pages = new Search("Page buttons on search window",
            new Condition(By.xpath("//html/body/div[contains(@id,\"wrapper\")]/div[@id=\"page\"]/div[@id=\"content\"]/div/div[contains(@class,\"result\")]/form/ul/li/a")));
    private final Search pagesPaged = new Search("Page buttons on search window",
            new Condition(By.xpath("//html/body/div[contains(@id,\"wrapper\")]/div[@id=\"page\"]/*/form/ul/li/a")));

    private final Search active = new Search("Page buttons on search window",
            new Condition(By.xpath("//html/body/div[contains(@id,\"wrapper\")]/div[@id=\"page\"]/div[@id=\"content\"]/div/div[contains(@class,\"result\")]/form/ul/li[contains(@class,\"active\")]/a")));
    private final Search activePaged = new Search("Page buttons on search window",
            new Condition(By.xpath("//html/body/div[contains(@id,\"wrapper\")]/div[@id=\"page\"]/*/form/ul/li[contains(@class,\"active\")]/a")));

    private final Search companies = new Search("Companies on current page",
            new Condition(By.xpath("//html/body/div[@id=\"wrapper\"]/div[@id=\"page\"]/div[@id=\"content\"]/div[contains(@class,\"row\")]/div[contains(@class,\"result\")]/form/div[contains(@id,\"result\")]//div[@class=\"prod_list\"]/div[@class=\"infos\"]/div/h2/a")));
    private final Search companiesPaged = new Search("Companies on current page",
            new Condition(By.xpath("//html/body/div[@id=\"wrapper\"]/div[@id=\"page\"]/div[@id=\"content\"]/form/div[contains(@id,\"result\")]//div[@class=\"prod_list\"]/div[@class=\"infos\"]/div/h2/a")));


    private final Search login = new Search("Login field",
            new Condition(By.xpath("//*/input[@type=\"text\" and @placeholder=\"Login\"]")));
    private final Search password = new Search("Password field",
            new Condition(By.xpath("//*/input[@type=\"password\" and @placeholder=\"Password\"]")));
    private final Search loginDropdown = new Search("Login dropdown",
            new Condition(By.xpath("//*[@class=\"dropdown-toggle\" and @id=\"header-account-link\"]")));
    private final Search logedDropdown = new Search("Login dropdown",
            new Condition(By.xpath("//*[@class=\"dropdown-toggle\" and @id=\"header-header_welcome_text-link\"]")));

    private final Search loginDropdownOpen = new Search("Login dropdown opened",
            new Condition(By.xpath("//*[@class=\"dropdown myMiniAccount open\"]")));
    private final Search loginSubmit = new Search("Password field",
            new Condition(By.xpath("//*/button[@type=\"submit\" and @id=\"header-connexion-link\"]")));
    private final Search incorrectLogin = new Search("Incorrect login",
            new Condition(By.xpath("//*/button[@id=\"login_submit_button\"]")));
    private final Search searchResults = new Search("Number of fended values",
            new Condition(By.xpath("//html/body/div[@id=\"wrapper\"]/div[@id=\"page\"]/div[@id=\"content\"]/div[contains(@class,\"row\")]/div[contains(@id,\"search\")]/div[@class=\"nav_column\"]/div[contains(@class,\"itemResults\")]/span[@id=\"result-count\"]/strong")));


    private final PageNumbers pageNumbers;
    private boolean initPage = true;

    public Kompass(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        this.pageNumbers = new PageNumbers();
    }

    @Override
    public WebDriver webDriver() {
        return this.driver;
    }

    public void quit() {
        this.driver.quit();
    }


    @Override
    public WebDriverWait webDriverWait() {
        return this.wait;
    }

    public Kompass mainPage() {
        driver.get("http://eg.kompass.com/en");
        wait.until(new AllElementsLoaded(page));
        return this;
    }

    public Kompass navigate(String ref) {
        driver.get(ref);
        wait.until(new AllElementsLoaded(page));
        return this;
    }

    public int countResults() throws ElementNotFoundException {
        WebElement element = searchResults.one(this.driver);
        String text = element.getText();
        return Integer.valueOf(text);
    }

    public int countPages() throws ElementNotFoundException {
        double pages = ((double) countResults()) / 20;
        return (int) Math.ceil(pages);
    }


    public Kompass scrollTo(Integer number) throws ElementNotFoundException {
        if (number > 1) {
            driver.get("http://eg.kompass.com/en/searchCompanies/scroll?pageNbre=" + number);
            wait.until(new AllElementsLoaded(page));
            initPage = false;
            this.pageNumbers.reload();
        }
        return this;
    }

    public Kompass scrollRight(Integer page) throws ElementNotFoundException {
        if (page > 1) {
            while (hasRight() && currentPages() + 10 <= page) {
                rightPage();
            }
            while (hasNextPage() && currentPages() != page) {
                nextPage();
            }
            if (currentPages() != page) {
                throw new RuntimeException("incorrect impelementation");
            }
        }
        return this;
    }

    public Kompass login(String userLogin, String userPassword) throws ElementNotFoundException, NotAuthorisedException {
        mainPage();
        //check if logined
        if (!logedDropdown.all(this.driver).isEmpty()) {
            throw new NotAuthorisedException("Already logged");
        }
        final WebElement dropdown = loginDropdown.one(this.driver);
        dropdown.click();
        wait.until(new AllElementsLoaded(loginDropdownOpen));
        final TextField login = new TextField(this.driver, this.login);
        login.fill(userLogin);
        final TextField password = new TextField(this.driver, this.password);
        password.fill(userPassword);
        password.enter();
        wait.until(new AllElementsLoaded(page));
        final Loader loginError = new Loader(this.driver, incorrectLogin);
        if (loginError.isExist()) {
            throw new NotAuthorisedException("Incorrect login and password");
        }
        return this;
    }

    public boolean isLogged() {
        try {
            return !logedDropdown.all(this.driver).isEmpty();
        } catch (ElementNotFoundException e) {
            return false;
        }
    }

    public Kompass searchPage() throws ElementNotFoundException {
        TextField input = new TextField(this.driver, searchMainInput, searchHeaderInput);
        String random = UUID.randomUUID().toString();
        input.fill(random);
        input.enter();
        wait.until(new AllElementsLoaded(page));
        this.initPage = true;
        return this;
    }

    public Kompass search(String keywords) throws ElementNotFoundException {
        TextField input = new TextField(this.driver, searchMainInput, searchHeaderInput);
        input.fill(keywords);
        input.enter();
        wait.until(new AllElementsLoaded(page));
        this.pageNumbers.reload();
        this.initPage = true;
        return this;
    }

    public boolean hasRight() {
        try {
            return !next.all(this.driver).isEmpty();
        } catch (ElementNotFoundException | InvalidSelectorException e) {
            //if no one element this exception can be thrown;
        }
        return false;
    }

    public int rightPage() throws ElementNotFoundException {
        scrollDown();
        int number = 0;
        WebElement button = next.one(this.driver);
        wait.until(ExpectedConditions.elementToBeClickable(button));
        number = getPageNumber(button.getAttribute("href"));
        button.click();

        wait.until(new NoOneElementsLoaded(loading));
        wait.until(new AllElementsLoaded(page));
        this.pageNumbers.reload();
        initPage = false;
        return number;
    }

    /**
     * function for returning current page number.
     *
     * @return number of current page, or 0 if there is no additional pages.
     */
    public int currentPages() {
        return this.pageNumbers.current();
    }

    public boolean hasNextPage() {
        return this.pageNumbers.hasNext();
    }

    public int nextPage() throws ElementNotFoundException {
        if (initPage) {
            initPage = false;
            return 1;
        }
        String href = this.pageNumbers.next();
        if (href == null) {
            throw new RuntimeException("incorrect implementation");
        }
        scrollDown();
        final Search search = new Search("Page with reference on search window",
                new Condition(By.xpath("/html//*[contains(@href,\"" + href.substring(href.length() - 15) + "\")]")));
        List<WebElement> buttons = search.all(this.driver);
        WebElement button = null;
        for (final WebElement element : buttons) {
            if (element.getAttribute("href").equals(href)) {
                button = element;
                break;
            }
        }
        if (button == null) {
            throw new ElementNotFoundException("can not find element");
        }
        wait.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
        wait.until(new NoOneElementsLoaded(loading));
        wait.until(new AllElementsLoaded(page));
        this.pageNumbers.reload();
        return this.pageNumbers.current();
    }


    public void scrollDown() {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight));");
    }

    public List<String> results() throws ElementNotFoundException {
        final LinkedList<String> result = new LinkedList<>();
        List<WebElement> elements = companies.all(this.driver);
        elements.addAll(companiesPaged.all(this.driver));
        for (final WebElement element : elements) {
            result.add(element.getAttribute("href"));
        }
        return result;
    }

    private int getPageNumber(String href) {
        int number = 0;
        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(href);
        if (matcher.find()) {
            number = Integer.valueOf(matcher.group());
        }
        return number;
    }

    private class PageNumbers {
        final TreeMap<Integer, String> references = new TreeMap<>();
        Integer current = 0;

        public void reload() throws ElementNotFoundException {
            references.clear();
            final List<WebElement> elements = Kompass.this.pages.all(Kompass.this.driver);
            int before = elements.size();
            elements.addAll(Kompass.this.pagesPaged.all(Kompass.this.driver));
            for (final WebElement element : elements) {
                final String href = element.getAttribute("href");
                references.put(Kompass.this.getPageNumber(href), href);
            }
            if (!elements.isEmpty()) {
                if (before == elements.size()) {
                    final WebElement selected = Kompass.this.active.one(Kompass.this.driver);
                    final String href = selected.getAttribute("href");
                    this.current = Kompass.this.getPageNumber(href);
                } else {
                    final WebElement selected = Kompass.this.activePaged.one(Kompass.this.driver);
                    final String href = selected.getAttribute("href");
                    this.current = Kompass.this.getPageNumber(href);
                }
            }
        }

        public int current() {
            return this.current;
        }

        public boolean hasNext() {
            return !references.tailMap(current, false).isEmpty();
        }

        public String next() {
            return references.tailMap(current, false).firstEntry().getValue();
        }
    }

}


