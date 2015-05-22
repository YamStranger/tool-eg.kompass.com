package scraping.moe.gov.sa;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import selenium.elements.*;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * User: YamStranger
 * Date: 4/30/15
 * Time: 2:00 PM
 */
public class Reader implements Callable<Map<List<DependableSelectors.Option>, List<SchoolProfile>>> {
    private final Initializer initializer;
    private final Map<List<DependableSelectors.Option>, List<SchoolProfile>> options = new LinkedHashMap<>();
    private final Set<String> schoolsProcessed = new HashSet<>();
    Set<SchoolProfile> profiles = new HashSet<>();
    Set<String> pages = new HashSet<>();

    public Reader(final Initializer initializer, Map<List<DependableSelectors.Option>, List<SchoolProfile>> options) {
        this.options.putAll(options);
        this.initializer = initializer;
    }

    @Override
    public Map<List<DependableSelectors.Option>, List<SchoolProfile>> call() throws Exception {
        Map<List<DependableSelectors.Option>, List<SchoolProfile>> result = new LinkedHashMap<>();
        StringBuilder message = new StringBuilder();
        WebDriver driver = null;
        boolean success = false;
        int incorrectTask = 30;
        do {
            try {
                message.setLength(0);
                message.append(Thread.currentThread().getName() + " Started processing of new Task");
                driver = initializer.driver();
                driver.get("https://www.moe.gov.sa/English/EServices/Public/Pages/SchoolByEducationalOffice.aspx");
                List<DependableSelectors.Option> task = options.keySet().iterator().next();
                List<SchoolProfile> loaded = options.get(task);
                Search loading = new Search("loading window", new Condition(
                        By.xpath("//*[contains(@src, \"mdn.js\")]")));

                //load load and load
                for (final DependableSelectors.Option option : task) {
                    message.append(" : " + option.value);
                }
                System.out.println(message);

                //choose drop down lists
                for (final DependableSelectors.Option option : task) {
                    DropDownList dropDownList = new DropDownList(driver, option.search);
                    dropDownList.select(option.value);
                }

                WebDriverWait wait = new WebDriverWait(driver, 120);
                //click search
                Search submit = new Search("Pages",
                        new Condition(By.xpath("//*[@class=\"FormBtnCont fR\"]")),
                        new Condition(By.tagName("input")));
                Search schools = new Search("Schools",
                        new Condition(By.xpath("//*[@class=\"StudentInfoDiv fR\"]/div[@class=\"Value fR\"]")),
                        new Condition(By.tagName("a"))
                );
                WebElement start = submit.one(driver);

                wait.until(new AllElementsLoaded(loading, submit));
                wait.until(ExpectedConditions.elementToBeClickable(start));
//                Thread.sleep(10000);
//            start.submit();
                start.click();
                try {
                    Thread.sleep(100);
                    wait.until(new AllElementsLoaded(schools));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println(Thread.currentThread().getName() + " started search");
                //*[@class="StudentInfoDiv fR"]
                //*[@class="StudentInfoDiv fR"]/div[@class="Value fR"]
                Search next = new Search("Button Next Page",
                        new Condition(By.xpath("//*[@class=\"ServicesContainer fR\"]/*[@id=\"paging\"]")),
                        new Condition(By.xpath("a[@class=\"next_item\"]")));
                Search previous = new Search("Button Previous Page",
                        new Condition(By.xpath("//*[@class=\"ServicesContainer fR\"]/*[@id=\"paging\"]")),
                        new Condition(By.xpath("a[@class=\"prev_item\"]")));
                Search error = new Search("No school loaded", new Condition(By.xpath("//*[@class=\"GrayMiddleShadow\"]")));


                String href = "0";
                int max = 2;
                do {

                    wait.until(new AnyElementLoaded(schools, error));
                    final List<WebElement> errors = error.all(driver);
                    if (!errors.isEmpty()) {
                        System.out.println(Thread.currentThread().getName() + " found empty page ");
                        throw new NoSuchElementException("Empty page");
                    }

                    wait.until(new AllElementsLoaded(submit));
                    wait.until(ExpectedConditions.elementToBeClickable(submit.one(driver)));
                    wait.until(new AllElementsLoaded(previous));
                    wait.until(ExpectedConditions.elementToBeClickable(previous.one(driver)));

                    try {

                        System.out.println(Thread.currentThread().getName() + " try to load data from page " + href);

                        int first = 0;
                        int windows = 5;
                        int lastResult = 0;
//                    Collection<SchoolProfile> current = new HashSet<>();
                        if (!pages.contains(href)) {
                            do {
                                first += windows;

                                lastResult = load(profiles, driver, windows, first);
                                //                      profiles.addAll(current);

                                //reload search
                                WebElement reload = submit.one(driver);
                                wait.until(ExpectedConditions.elementToBeClickable(reload));
                                reload.click();
                                try {
                                    Thread.sleep(100);
                                    wait.until(new AllElementsLoaded(loading, next, previous, submit));
                                    wait.until(ExpectedConditions.elementToBeClickable(previous.one(driver)));
                                    wait.until(ExpectedConditions.elementToBeClickable(next.one(driver)));
                                    wait.until(ExpectedConditions.elementToBeClickable(submit.one(driver)));

                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                System.out.println(Thread.currentThread().getName() + " keeping alive of page");
                                Thread.sleep(3000);
                            } while (lastResult > 0);
                            pages.add(href);
                        }

                        WebElement button = next.one(driver);
                        href = button.getAttribute("href");
                        href = href.substring(href.length() - 1, href.length());
                        if (!"#".equals(href)) {
/*                        if (--max <= 0) {
                            System.out.println("finished for tests");
                            break;

                        }*/
                            //change url
                            wait.until(new AllElementsLoaded(loading));
                            wait.until(ExpectedConditions.elementToBeClickable(button));
                            button.click();
                            try {
                                Thread.sleep(100);
                                //  (new WebDriverWait(driver, 120)).until(new ElementLoaded(next));
                                //  (new WebDriverWait(driver, 120)).until(new ElementLoaded(loading));
                                //  (new WebDriverWait(driver, 120)).until(new ElementLoaded(submit));
//                            wait.until(new ElementLoaded(schools));
//                            wait.until(new ElementLoaded(previous));
                                Search updated = new Search("Button Next Page processed",
                                        new Condition(By.xpath("//*[@class=\"ServicesContainer fR\"]/*[@id=\"paging\"]")),
                                        new Condition(By.xpath("a[@class=\"next_item\" and @href!=\"" + href + "\"]")));
                                wait.until(new AllElementsLoaded(updated));
                                wait.until(ExpectedConditions.elementToBeClickable(previous.one(driver)));
                                wait.until(ExpectedConditions.elementToBeClickable(next.one(driver)));
                                wait.until(ExpectedConditions.elementToBeClickable(submit.one(driver)));

                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            Thread.sleep(3000);
                            //reload search
                            wait.until(new AllElementsLoaded(loading));
                            WebElement reload = submit.one(driver);
                            wait.until(ExpectedConditions.elementToBeClickable(reload));
                            reload.click();
                            try {
                                Thread.sleep(100);
                                wait.until(ExpectedConditions.elementToBeClickable(previous.one(driver)));
                                wait.until(ExpectedConditions.elementToBeClickable(next.one(driver)));
                                wait.until(ExpectedConditions.elementToBeClickable(submit.one(driver)));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            Thread.sleep(3000);
                            System.out.println(Thread.currentThread().getName() + " opened next page " + href);
                        }
                    } catch (ElementNotFound e) {
                        System.out.println("during loading next page " + e);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ee) {
                            Thread.currentThread().interrupt();
                        }
                        throw e;
                    }
                } while (!"#".equals(href));
                System.out.println(Thread.currentThread().getName() + " successful finish loading data, loaded " + profiles.size());
                loaded.addAll(profiles);
                success = true;
                result.put(task, loaded);
            } catch (NoSuchElementException e) {
                incorrectTask--;
                System.out.println(Thread.currentThread().getName() + " cached exception, started search again : " + e + "counter=" + incorrectTask);
            } catch (Throwable e) {
                incorrectTask--;
                System.out.println(Thread.currentThread().getName() + " cached exception, started search again : " + e + "counter=" + incorrectTask);
            } finally {
                if (driver != null) {
                    driver.quit();
                }
                System.out.println(message.toString().replace("Started", "Finished"));
            }
        } while (!success && incorrectTask > 0);
        if (incorrectTask <= 0) {
            System.out.println(Thread.currentThread().getName() + " Incorrect task limit reached");
            throw new Exception("skip this value for now");
        }
        return result;
    }

    private int load(final Set<SchoolProfile> profiles, final WebDriver driver, int windows, int start) throws ElementNotFound {
        Search schools = new Search("Schools",
                new Condition(By.xpath("//*[@class=\"StudentInfoDiv fR\"]/div[@class=\"Value fR\"]")),
                new Condition(By.tagName("a"))
        );
        Search detailed = new Search("Check If Schools loaded",
                new Condition(By.xpath("//*[@class=\"StudentInfoDiv fR\"]")),
                new Condition(By.xpath("div[@class=\"Label fR\"]"))
        );

//        List<SchoolProfile> loaded = new LinkedList<>();
/*        if (count-- <= 0) {
            System.out.println("schools are skipped for test");
            return loaded;
        }*/
        String main = driver.getWindowHandle();
        if (driver.getWindowHandles().size() > 1) {
            throw new IllegalStateException("Can't be open more than one window");
        }
        final List<String> tabs = new LinkedList<>();
        int loaded = 0;

        boolean finished = false;
        do {
            ArrayList<WebElement> elements = new ArrayList<>(schools.all(driver));
            for (int i = start; i < start + windows; ++i) {
                if ((elements.size() <= i)) {
                    finished = true;
                    break;
                } else {
                    WebElement school = elements.get(i);
                    String href = school.getAttribute("href");
                    school = elements.get(i);
                    if (!this.schoolsProcessed.contains(href)) {
                        Actions action = new Actions(driver);
                        action.moveToElement(school)
                                .keyDown(Keys.SHIFT).click(school)
                                .keyUp(Keys.SHIFT).perform();
                        System.out.println(Thread.currentThread().getName() + " starting opening school " + href);
                    } else {
                        loaded += 1;
                        System.out.println(Thread.currentThread().getName() + " school already processed, skipped " + href);
                    }
                }
            }
            start += windows;

            //procesing
            final Set<String> handles = driver.getWindowHandles();
            handles.remove(main);
            final Search propety = new Search("Property Name",
                    new Condition(By.xpath("//*[@class=\"StudentInfoDiv fR\"]/div[@class=\"Label fR\"]")),
                    new Condition(By.tagName("span"))
            );
            final Search value = new Search("Property Name",
                    new Condition(By.xpath("//*[@class=\"StudentInfoDiv fR\"]/div[@class=\"Value fR\"]")),
                    new Condition(By.tagName("span"))
            );
            Search error = new Search("No school loaded", new Condition(By.xpath("//*[@class=\"GrayMiddleShadow\"]")));

            WebDriverWait wait = new WebDriverWait(driver, 60);
            for (final String handler : handles) {
                driver.switchTo().window(handler);
/*                int exceptions = 3;
                do {*/
                try {
                    try {
                        Thread.sleep(100);
                        wait.until(new AnyElementLoaded(detailed, error));
                        final List<WebElement> errors = error.all(driver);
                        if (!errors.isEmpty()) {
                            throw new ElementNotFound("found error");
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    //start processing
                    System.out.println(Thread.currentThread().getName() + " opened school");
                    final SchoolProfile profile = new SchoolProfile();
                    final List<WebElement> properties = propety.all(driver);
                    final List<WebElement> values = value.all(driver);
                    fill(properties, values, profile);
                    profiles.add(profile);
                    loaded += 1;
                    this.schoolsProcessed.add(driver.getCurrentUrl());
                    //cosing window
                    System.out.println(Thread.currentThread().getName() + " processed school " + driver.getCurrentUrl());
                    try {
                        driver.close();
                    } catch (NoSuchWindowException closed) {
                        System.out.println(closed);
                    }
                    //exceptions = 0;
                } catch (ElementNotFound e) {
                    System.out.println("diring parsing page " + e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ee) {
                        Thread.currentThread().interrupt();
                    }
                    throw e;
                }
/*                } while (--exceptions >= 0);*/
            }
            driver.switchTo().window(main);
        } while (!finished);







/*       int tries = 3;
        do {
            try {*/

/*        for (final WebElement school : schools.all(driver)) {
            Actions action = new Actions(driver);
            action.moveToElement(school).keyDown(Keys.SHIFT).click(school).keyUp(Keys.SHIFT).perform();
        }*/
/*
                tries = 0;
            } catch (ElementNotFound e) {
                System.out.println("diring loading schools " + e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ee) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (--tries >= 0);
*/

        return loaded;
    }


    private void fill(final List<WebElement> properties, final List<WebElement> values,
                      final SchoolProfile profile) {
        final Iterator<WebElement> property = properties.iterator();
        final Iterator<WebElement> value = values.iterator();
        while (property.hasNext() && value.hasNext()) {
            switch (property.next().getText().trim()) {
                case "School ID":
                    profile.id = value.next().getText();
                    System.out.println(Thread.currentThread().getName() + " school id " + profile.id);
                    break;
                case "School Ministry Number":
                    profile.ministryNumber = value.next().getText();
                    break;
                case "School Name":
                    profile.name = value.next().getText();
                    break;
                case "Year Established":
                    profile.established = value.next().getText();
                    break;
                case "School Classification":
                    profile.classification = value.next().getText();
                    break;
                case "Education Office":
                    profile.office = value.next().getText();
                    break;
                case "District Name":
                    profile.district = value.next().getText();
                    break;
                case "Administrative Region":
                    profile.region = value.next().getText();
                    break;
                case "Administrative Contry":
                    profile.country = value.next().getText();
                    break;
                case "Administrative Centers":
                    profile.centers = value.next().getText();
                    break;
                case "School Address":
                    profile.address = value.next().getText();
                    break;
                case "School Email":
                    profile.email = value.next().getText();
                    break;
                case "School Phone":
                    profile.phone = value.next().getText();
                    break;
                case "School Fax":
                    profile.fax = value.next().getText();
                    break;
                case "Study Level":
                    profile.level = value.next().getText();
                    break;
                case "School Gender":
                    profile.gender = value.next().getText();
                    break;
                case "Student Count":
                    profile.students = value.next().getText();
                    break;
                case "Saudi Student Count":
                    profile.saudiStudents = value.next().getText();
                    break;
                case "NonSaudi Student Count":
                    profile.notSaudiStudents = value.next().getText();
                    break;
                case "Teachers Count":
                    profile.teachers = value.next().getText();
                    break;
                case "Saudi Teacher Count":
                    profile.saudiTeachers = value.next().getText();
                    break;
                case "NonSaudi Teacher Count":
                    profile.notSaudiTeachers = value.next().getText();
                    break;
                case "Classes Count":
                    profile.classes = value.next().getText();
                    break;
            }
        }

    }


}
