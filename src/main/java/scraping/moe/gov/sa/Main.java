package scraping.moe.gov.sa;

import com.util.ArgumentsParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import selenium.elements.Condition;
import selenium.elements.Search;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: YamStranger
 * Date: 4/29/15
 * Time: 1:09 PM
 */
public class Main extends Thread {
    private final Set<String> hash = new HashSet();
    private final Path storage;
    private int mod = 1;
    private int rest = 0;
    private int threads=0;

    public Main(int mod, int rest, int threads) {
        this.mod = mod;
        this.rest = rest;
        this.threads = threads;
        this.storage = Paths.get("storage.bin");
    }

    @Override
    public void run() {
        List<DependableSelectors.Option> options = this.options();
        System.out.println("loaded options.size() " + options.size());
        BlockingQueue<List<SchoolProfile>> results = new LinkedBlockingQueue<>(3);
        Storage storage = new Storage(Paths.get("www.moe.gov.sa.csv"), results);
        storage.start();
        load(options, results);
        System.out.println("all loaded successfully");
    }

    public void load(final List<DependableSelectors.Option> options, final BlockingQueue<List<SchoolProfile>> results) {
        List<List<DependableSelectors.Option>> prepared = new LinkedList<>();

        System.out.println(getName() + " downloaded combinations:");
        int combinations = 0;
        for (final DependableSelectors.Option option : options) {
            System.out.println(option.value + " " + option.text);
            int childs = 0;
            for (final DependableSelectors.Option child : option.dependable) {
                combinations++;
                childs++;
                System.out.println(getName() + "    " + child.value + " " + child.text);
            }
            System.out.println(getName() + "    total" + childs);
        }
        System.out.println(getName() + " combinations must be " + combinations);

        prepare(prepared, options);

        int res = 0;
        for (final List<DependableSelectors.Option> optionList : prepared) {
            System.out.println("combination number " + res);
            res++;
            for (final DependableSelectors.Option option : optionList) {
                System.out.print(option.value + " " + option.text + " : ");
            }
            System.out.println();
        }
        System.out.println("total combinations " + res);


        ExecutorService service = Executors.newFixedThreadPool(this.threads);
        final List<Future<Map<List<DependableSelectors.Option>, List<SchoolProfile>>>> subresults = new LinkedList<>();
        Initializer initializer = new Initializer();

        do {
            int i = 0;
            for (List<DependableSelectors.Option> current : prepared) {
                i += 1;
                if (i % this.mod == rest && !known(current)) {
                    Map<List<DependableSelectors.Option>, List<SchoolProfile>> map =
                            new HashMap<>();
                    map.put(current, new LinkedList<SchoolProfile>());
                    subresults.add(service.submit(new Reader(initializer, map)));
                }
            }
            while (subresults.size() > 0) {
                Iterator<Future<Map<List<DependableSelectors.Option>, List<SchoolProfile>>>> futures = subresults.iterator();
                while (futures.hasNext()) {
                    Future<Map<List<DependableSelectors.Option>, List<SchoolProfile>>> current = futures.next();
                    if (current.isDone()) {
                        futures.remove();
                        try {
                            System.out.println("New result");
                            Map<List<DependableSelectors.Option>, List<SchoolProfile>> result = current.get();
                            Iterator<List<DependableSelectors.Option>> iterator = result.keySet().iterator();
                            if (iterator.hasNext()) {
                                List<DependableSelectors.Option> task = iterator.next();
                                List<SchoolProfile> loaded = result.get(task);
                                results.put(loaded);
                                prepared.remove(task);
                                register(task);
                            }
                        } catch (Throwable e) {
                            System.out.println("exeption during executing " + e);
                        }
                        //store result
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    this.interrupt();
                }

            }
        } while (prepared.size() > 0);
    }

    void register(List<DependableSelectors.Option> values) {

        StringBuilder check = new StringBuilder();
        for (final DependableSelectors.Option value : values) {
            check.append(value.text.replace("\\s+", ""));
            check.append(value.value.replace("\\s+", ""));
        }
        this.hash.add(check.toString());
        try {
            if (!Files.exists(this.storage)) {
                Files.createFile(this.storage);
            }
        } catch (IOException e) {
            System.out.println("Storage error, can not create storage, cant write used values " + e);
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(storage.toAbsolutePath().toFile(),true),
                Charset.forName("UTF-8").newEncoder()
        ))) {
            writer.write(check.toString());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Storage error, cant write used values " + e);
        }
    }

    /**
     * return true - if already processed.
     */
    boolean known(List<DependableSelectors.Option> values) {
        if (hash.isEmpty()) {
            //load
            if (Files.exists(storage)) {
                try (BufferedReader reader=new BufferedReader(new InputStreamReader(
                        new FileInputStream(this.storage.toAbsolutePath().toFile()),
                        Charset.forName("UTF-8")))) {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        this.hash.add(line);
                    }
                } catch (IOException e) {
                    System.out.println("Storage error, cant check if values used before" + e);
                }
            }
        }

        StringBuilder check = new StringBuilder();
        for (final DependableSelectors.Option value : values) {
            check.append(value.text.replace("\\s+", ""));
            check.append(value.value.replace("\\s+", ""));
        }

        boolean known = this.hash.contains(check.toString());
        //check and update
        return known;
    }


    public List<DependableSelectors.Option> options() {
        final List<DependableSelectors.Option> options = new LinkedList<>();
        WebDriver driver = null;
        try {
            driver = new Initializer().driver();
            driver.get("https://www.moe.gov.sa/English/EServices/Public/Pages/SchoolByEducationalOffice.aspx");

            final Search district = new Search("Please select District",
                    new Condition(By.xpath("//*[@class=\"FormItemContainer Big fR\"]"),
                            new Condition(By.xpath("div[@class=\"FormLabel fR\"]"),
                                    new Condition(By.tagName("span"), "Please select District.*"))),
                    new Condition(By.xpath("div[@class=\"FormDropDown fR\"]")),
                    new Condition(By.tagName("select"))
            );
            final Search office = new Search("Please Select Educational Office",
                    new Condition(By.xpath("//*[@class=\"FormItemContainer Big fR\"]"),
                            new Condition(By.xpath("div[@class=\"FormLabel fR\"]"),
                                    new Condition(By.tagName("span"), "Please Select Educational Office.*"))),
                    new Condition(By.xpath("div[@class=\"FormDropDown fR\"]")),
                    new Condition(By.tagName("select"))
            );
            final DependableSelectors selectors = new DependableSelectors(driver, district, office);
            options.addAll(selectors.load());
        } catch (Throwable th) {
            System.out.println(th);
            //Close the browser
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return options;
    }

    public void prepare(final List<List<DependableSelectors.Option>> result,
                        final List<DependableSelectors.Option> current,
                        final DependableSelectors.Option option) {
        current.add(option);
        if (!option.dependable.isEmpty()) {
            for (final DependableSelectors.Option internal : option.dependable) {
                List<DependableSelectors.Option> values = new LinkedList<>();
                values.addAll(current);
                this.prepare(result, values, internal);
            }
        } else {
            result.add(current);
        }
    }

    public void prepare(final List<List<DependableSelectors.Option>> result,
                        final List<DependableSelectors.Option> source) {
        for (DependableSelectors.Option option : source) {
            List<DependableSelectors.Option> values = new LinkedList<>();
            this.prepare(result, values, option);
        }
    }


    public static void main(String... args) throws InterruptedException, OperationNotSupportedException {
        ArgumentsParser argumentsParser = new ArgumentsParser(args);
        int mod = 1;
        int rest = 0;
        int threads = 0;
        if (argumentsParser.arguments().containsKey("mod")) {
            System.out.println("mod=" + mod);
            mod = Integer.parseInt(argumentsParser.arguments().get("mod"));
        } else {
            throw new OperationNotSupportedException("run without mod");
        }
        if (argumentsParser.arguments().containsKey("rest")) {
            System.out.println("rest=" + rest);
            rest = Integer.parseInt(argumentsParser.arguments().get("rest"));
        } else {
            throw new OperationNotSupportedException("run without rest");
        }

        if (argumentsParser.arguments().containsKey("threads")) {
            System.out.println("threads=" + threads);
            threads = Integer.parseInt(argumentsParser.arguments().get("threads"));
        } else {
            throw new OperationNotSupportedException("run without threads");
        }



        Thread thread = new Main(mod, rest, threads);
        thread.start();
        thread.join();
    }
}
