package web.tool;

import date.Dates;
import general.Pair;
import general.RangeGenerator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selenium.WebDriverHub;
import storage.Item;
import storage.KeyHolder;
import storage.RowsReader;
import web.tool.eg.kompass.com.Company;
import web.tool.eg.kompass.com.Kompass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * User: YamStranger
 * Date: 5/16/15
 * Time: 12:59 AM
 */
public class Dispatcher extends Thread {
    private final BlockingQueue<Item> results;
    private final WebDriverHub secure;
    private final WebDriverHub notSecure;
    private boolean signal = false;
    private String keyword;
    private String login;
    private String password;
    private final ExecutorService executor;
    private final String session;
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    public Dispatcher(String session, String keyword, String login, String password, BlockingQueue<Item> results, int threads, WebDriverHub secure, WebDriverHub notSecure) {
        this.results = results;
        this.keyword = keyword;
        this.login = login;
        this.password = password;
        this.executor = Executors.newFixedThreadPool(threads);
        this.session = session;
        this.secure = secure;
        this.notSecure = notSecure;

    }

    @Override
    public void run() {
        try {
            logger.info("user:start search \"" + this.keyword + '"' + ", session=\"" + this.session + '"');


            final int pages = countAllPages(this.keyword, this.login, this.password);
            int companies = pages * 20;//197514
            Path allRefs = Paths.get(session + "known_references.bin");
            //loading refs
            companies = this.readRefs(allRefs, pages, companies, keyword, this.session, executor);
            logger.info("user:updated number of companies " + companies);

            //loading data
            this.readCompanies(allRefs, this.login, this.password, this.session,
                    this.executor, companies, results);
            try {
                Files.delete(allRefs);
            } catch (IOException e) {
                logger.error("exception during removing refs");
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    public void readCompanies(final Path allRefs, final String login,
                              final String password, final String session,
                              final ExecutorService executor, final int companies,
                              final BlockingQueue<Item> results) {
        final Path knownCompanies = Paths.get(session + "known_companies.bin");
        final KeyHolder companiesCollector = new KeyHolder(knownCompanies);
        RowsReader rowsReader = new RowsReader(allRefs);
        Iterator<String> rows = rowsReader.iterator();

        //read all companies ref by ref
        Dates starts = new Dates();
        final Map<String, Future<Company>> processingTasks = new HashMap<>();
        Queue<String> failed = new LinkedList<>();
        Queue<String> privates = new LinkedList<>();
        int read = 0;
        int remaining = companies;
        while (rows.hasNext() || !processingTasks.isEmpty()) {
            if (rows.hasNext()) {
                final String url = rows.next();
                if (!companiesCollector.contains(url)) {
                    if (read == 0) {
                        remaining -= companiesCollector.values().size();
                    }
                    WebDriver driver = this.notSecure.driver(); //waits until new one available
                    WebDriverWait wait = this.notSecure.driverWait(driver);
                    Kompass kompass = new Kompass(driver, wait);
                    //start processing url as public company
                    CompanyDataReader reader = new CompanyDataReader(url, kompass, false);
                    Future<Company> future = executor.submit(reader);
                    processingTasks.put(url, future);
                } else {
                    logger.info("user:skip as known " + url);
                }
            }

            while (!failed.isEmpty()) {
                //restart failed
                final String url = failed.poll();
                WebDriver driver = this.secure.driver(); //waits until new one available
                WebDriverWait wait = this.secure.driverWait(driver);
                //start processing url as public company
                Kompass kompass = new Kompass(driver, wait);
                CompanyDataReader reader = new CompanyDataReader(url, kompass, false);
                Future<Company> future = executor.submit(reader);
                processingTasks.put(url, future);
            }

            while (!privates.isEmpty()) {
                //restart private company
                final String url = privates.poll();
                //login, password;
                WebDriver driver = this.secure.driver(); //waits until new one available
                WebDriverWait wait = this.secure.driverWait(driver);
                Kompass kompass = new Kompass(driver, wait);
                boolean isLogined = true;
                if (!kompass.isLogged()) {
                    try {
                        kompass.login(login, password);
                    } catch (Exception e) {
                        isLogined = false;
                        logger.info("user:Login/password error, trying without authorization");
                    }
                }
                //start processing url as private company
                CompanyDataReader reader = new CompanyDataReader(url, kompass, isLogined);
                Future<Company> future = executor.submit(reader);
                processingTasks.put(url, future);
            }

            //processing started, checking results;
            Iterator<Map.Entry<String, Future<Company>>> processingIterator = processingTasks.entrySet().iterator();
            while (processingIterator.hasNext()) {
                Map.Entry<String, Future<Company>> task = processingIterator.next();
                String url = task.getKey();
                Future<Company> future = task.getValue();
                try {
                    if (future.isDone()) {
                        Company company = future.get();
                        if (!company.needAuthorisation) {
                            companiesCollector.register(company.url);
                            read += 1;
                            Dates last = new Dates();
                            int seconds = (int) last.difference(starts, Calendar.SECOND);
                            double speed = (double) seconds / (double) read;
                            int takesS = (int) Math.floor(speed * (double) (remaining - read));
                            int hour = 0;
                            int min = 0;
                            String word;
                            if (takesS > 60) {
                                min = takesS / 60;
                                takesS = takesS - min * 60;
                                if (min > 60) {
                                    hour = min / 60;
                                    min = min - hour * 60;
                                }
                            }
                            word = String.valueOf(hour) + " : " + min + " : " + takesS;
                            last.add(Calendar.SECOND, seconds);
                            last.add(Calendar.MINUTE, min);
                            last.add(Calendar.HOUR, hour);
                            logger.info("user:read " + read + ", companies will be read in " + word + ", near " + last + ", remaining " + (remaining - read) + " companies from " + companies);
                            processingIterator.remove();
                            results.put(company);
                        } else {
                            logger.info("user:company  " + url + " is marked as private");
                            privates.add(company.url);
                        }
                    }
                } catch (Exception e) {
                    //returning task to queue
                    failed.add(url);
                    logger.error("exception during reading company  " + url + " ", e);
                }
            }


        }
        try {
            Files.delete(knownCompanies);
        } catch (IOException e) {
            logger.error("can not delete file ", knownCompanies.toAbsolutePath(), e);
        }
    }

    public int readRefs(Path allRefs, int pages, int companies, String keyword, String session, ExecutorService executor) {
        KeyHolder refCollector = new KeyHolder(allRefs);
        Path knownPages = Paths.get(session + "known_pages.bin");
        KeyHolder refsPageCollector = new KeyHolder(knownPages);
        logger.info("user:pages=" + pages);
        logger.info("user:companies=" + companies);

        //collect all references, by pages
        Dates starts = new Dates();
        logger.info("user:start generation rangers");
        final Map<Pair<Integer, Integer>, Future<List<String>>> processingTasks = new HashMap<>();
        RangeGenerator generator = new RangeGenerator(Integer.MAX_VALUE, 10);
        Queue<Pair<Integer, Integer>> tasks = new LinkedList<>(generator.generate(1, pages, 2));
        logger.info("user:finish generation rangers, generated " + tasks.size());

        int read = -1;
        int remaining = companies;
        while (!tasks.isEmpty() || !processingTasks.isEmpty()) {
            final Pair<Integer, Integer> range = tasks.poll();
            if (range != null && !refsPageCollector.contains(range.toString())) {
                if (read == -1) {
                    remaining -= refsPageCollector.values().size();
                }
                WebDriver driver = this.notSecure.driver(); //waits until new one available
                WebDriverWait wait = this.notSecure.driverWait(driver);
                //call
                ReferenceCollector collector = new ReferenceCollector(keyword, range.left(), range.right(), driver, wait);
                Future<List<String>> future = executor.submit(collector);
                processingTasks.put(range, future);
            }
            //processing started, checking results;
            Iterator<Map.Entry<Pair<Integer, Integer>, Future<List<String>>>> processingIterator = processingTasks.entrySet().iterator();
            while (processingIterator.hasNext()) {
                Map.Entry<Pair<Integer, Integer>, Future<List<String>>> task = processingIterator.next();
                Pair<Integer, Integer> taskRange = task.getKey();
                Future<List<String>> taskFuture = task.getValue();
                try {
                    if (taskFuture.isDone()) {
                        List<String> refs = taskFuture.get();
                        for (final String ref : refs) {
                            refCollector.register(ref);
                        }
                        refsPageCollector.register(taskRange.toString());
                        int current = refs.size();
                        read += current;
                        Dates last = new Dates();
                        long seconds = last.difference(starts, Calendar.SECOND);
                        double speed = (double) seconds / (double) read;
                        long takesS = (long) Math.floor(speed * (double) (remaining - read));
                        long hour = 0;
                        long min = 0;
                        String word = "";
                        if (takesS > 60) {
                            min = takesS / 60;
                            takesS = takesS - min * 60;
                            if (min > 60) {
                                hour = min / 60;
                                min = min - hour * 60;
                            }
                        }
                        word = String.valueOf(hour) + " : " + min + " : " + takesS;
                        last.add(Calendar.SECOND, seconds);
                        last.add(Calendar.MINUTE, min);
                        last.add(Calendar.HOUR, hour);

                        logger.info("user:read " + read + ", refs will be collected in " + word + ", near " + last + ", remaining " + (remaining - read) + " references from " + companies);
                        processingIterator.remove();
                    }
                } catch (Exception e) {
                    //returning task to queue
                    tasks.add(taskRange);
                    logger.error("exception during reading range  " + taskRange + " " + e);
                }
            }
        }
        //all read successfully
        try {
            Files.delete(knownPages);
        } catch (IOException e) {
            logger.error("can not delete file " + knownPages.toAbsolutePath());
        }
        return read;
    }

    public int countAllPages(final String keyword, final String login, final String password) throws Exception {
        boolean success = true;
        do {
            WebDriver driver = this.secure.driver();
            try {
                logger.info("user:Counting pages : ");

                final WebDriverWait wait = this.secure.driverWait(driver);
                Kompass kompass = new Kompass(driver, wait);
                if (!kompass.isLogged()) {
                    try {
                        kompass.login(login, password);
                    } catch (Exception e) {
                        logger.info("user:Login/password error, trying without authorization");
                    }
                }
                kompass.searchPage();
                kompass.search(keyword);
                int pages = kompass.countPages();
                return pages;
            } catch (Throwable e) {
                logger.error("user:can not count pages ", e);
                success = false;
            } finally {
                driver.quit();
            }
        } while (!success);
        return 0;
    }

    @Override
    public void interrupt() {
        this.signal = true;
        super.interrupt();    //Generated, need to be changed
    }
}
