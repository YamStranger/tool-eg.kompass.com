package scraping.moe.gov.sa;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import selenium.elements.ElementNotFound;
import selenium.elements.Search;

import java.util.*;

/**
 * User: YamStranger
 * Date: 4/29/15
 * Time: 3:14 PM
 */

/**
 * DependentSelectors created for loading options of selectors, witch depend on
 * each other.
 * Priority of dependencies described by input array selectors.
 * example of behaving such selectors on page :
 * selectors[3]={xpath1, xpath2, xpath3};
 * for every unique option of selector by xpath2 next selector(xpath3) will contains unique values.
 * for every unique option of selector by xpath1 next selector(xpath2) will contains unique values.
 * and so on.
 */
public class DependableSelectors {
    private final List<Search> searches = new LinkedList<>();
    private final WebDriver driver;
    private final Set<String> skip = new HashSet<>();

    public DependableSelectors(final WebDriver webDriver, final Search... searches) {
        this.driver = webDriver;
        this.searches.addAll(Arrays.asList(searches));
        this.skip.add("-1");
    }

    /**
     * load all options for selectors according selectors xpath.
     * Selectors must depend on previous selector.
     *
    */
    public List<Option> load() throws ElementNotFound {
        final List<Option> options = new LinkedList<>();
        boolean success = true;
        do {
            try {
                load(options, this.searches);
                success = true;
            } catch (Throwable error) {
                System.out.println(Thread.currentThread().getName() + " reloading dependable selectors");
                success = false;
                this.driver.navigate().refresh();
            } finally {
                for (final Option option : options) {
             //       this.skip.add(option.value);
                }
            }
        } while (!success);


        return options;
    }

    int l = 0;
    int counter = 0;

    private void load(final List<Option> values, final List<Search> queue) throws ElementNotFound {
        if (!queue.isEmpty()) {
            l += 1;
            final List<Search> level = new ArrayList<>(queue.size());
            level.addAll(queue);
            final Search search = level.remove(0);
            final Select select = new Select(search.one(driver));
            final Map<String, String> options = this.options(select);
/*            Selector

            final WebElement element = driver.findElement(By.xpath("/*//*[@id=\"ctl00_ctl15_g_ad55844d_fe4d_4f19_8dc9_99de149ea796_ctl00_updProgressSchool\"]"));
            return !element.isDisplayed();*/
            for (final Map.Entry<String, String> option : options.entrySet()) {

                final String value = option.getKey();
                final String text = option.getValue();
                //try {
                if (!skip.contains(value)) {
                    System.out.println(Thread.currentThread().getName() + " " + l + "(" + this.counter + ")"
                            + " value=" + value + ", text=" + text.hashCode() + ",xpath" + search);
                    final Option current = new Option(search, value, text);
                    values.add(current);
                    DropDownList list = new DropDownList(this.driver, search);
                    list.select(value);
                    //this.select(xpath, value, new SelectedCondition(xpath, value));
        /*            if (++counter > 10) {
                        break;
                    }*/
                    this.load(current.dependable, level);
                    if(l==1){
                        this.skip.add(value);
                        System.out.println(Thread.currentThread().getName() + " " + l + " added to skip in future " + value);
                    }
                } else {
                    System.out.println(Thread.currentThread().getName() + " " + l + " skip known value " + value);
                }
            }
            l -= 1;
        }
    }

    private Map<String, String> options(final Select select) {
        final Map<String, String> options = new TreeMap<>();
        final List<WebElement> elements = select.getOptions();
        for (final WebElement element : elements) {
            options.put(element.getAttribute("value"), element.getText());
        }
        return options;
    }

    /**
     * Class for representing values for every deepness
     */
    public static class Option {
        final Search search;
        final String value;
        final String text;
        final List<Option> dependable = new LinkedList<>();

        public Option(final Search search, final String value, final String text) {
            this.search = search;
            this.value = value;
            this.text = text;
        }

        public void add(final Option child) {
            this.dependable.add(child);
        }


    }
}
