package scraping.moe.gov.sa;

import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * User: YamStranger
 * Date: 5/5/15
 * Time: 4:22 PM
 */
public class MainTest {
    @Test
    public void combination_correct_proceed() {
        final List<List<DependableSelectors.Option>> result = new LinkedList<>();
        final List<DependableSelectors.Option> source = new LinkedList<>();
        final DependableSelectors.Option first = new DependableSelectors.Option(null, "1:1", "1:1");
        final DependableSelectors.Option childFirst1 = new DependableSelectors.Option(null, "1:1:1", "1:1:1");
        final DependableSelectors.Option childFirst2 = new DependableSelectors.Option(null, "1:1:2", "1:1:2");
        final DependableSelectors.Option childFirst3 = new DependableSelectors.Option(null, "1:1:3", "1:1:3");
        first.add(childFirst1);
        first.add(childFirst2);
        first.add(childFirst3);
        source.add(first);

        final DependableSelectors.Option second = new DependableSelectors.Option(null, "1:2", "1:2");
        final DependableSelectors.Option childSecond1 = new DependableSelectors.Option(null, "1:2:1", "1:2:1");
        final DependableSelectors.Option childSecond2 = new DependableSelectors.Option(null, "1:2:2", "1:2:2");
        second.add(childSecond1);
        second.add(childSecond2);
        source.add(second);

        for (final List<DependableSelectors.Option> options : result) {
            System.out.println();
            for (final DependableSelectors.Option option : options) {
                System.out.print(option.value + " - ");
            }
        }


    }
}
