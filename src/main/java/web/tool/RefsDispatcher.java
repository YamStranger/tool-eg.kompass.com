package web.tool;

import date.Dates;
import general.Pair;
import general.RangeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selenium.WebDriverHub;
import storage.KeyHolder;

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
 * Date: 5/26/15
 * Time: 6:19 PM
 */
public class RefsDispatcher extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherNew.class);
    private final String session;
    private final Path allRefs;
    private int pages;
    private int companies;
    private String keyword;
    private WebDriverHub hub;
    private final ExecutorService refsExecutor;
    private final int threads;
    private final BlockingQueue<String> refsQueue;

    public RefsDispatcher(String session, Path allRefs, int pages, int companies, String keyword, WebDriverHub hub, int threads, final BlockingQueue<String> refs) {
        this.session = session;
        this.allRefs = allRefs;
        this.pages = pages;
        this.companies = companies;
        this.keyword = keyword;
        this.hub = hub;
        this.refsExecutor = Executors.newFixedThreadPool(threads);
        this.threads = threads;
        this.refsQueue = refs;
    }

    @Override
    public void run() {
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

        final Map<Future, ReferenceCollector> ages = new HashMap<>();

        int read = -1;
        int remaining = companies;
        while (!tasks.isEmpty() || !processingTasks.isEmpty()) {
            if (processingTasks.size() < this.threads) {
                final Pair<Integer, Integer> range = tasks.poll();
                if (range != null && !refsPageCollector.contains(range.toString())) {
                    if (read == -1) {
                        remaining -= refsPageCollector.values().size();
                        read = 0;
                    }
                    //call
                    ReferenceCollector collector = new ReferenceCollector(this.hub, keyword, range.left(), range.right());
                    Future<List<String>> future = refsExecutor.submit(collector);
                    processingTasks.put(range, future);
                    ages.put(future, collector);
                }
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
                            this.refsQueue.put(ref);
                            logger.info(" read new ref, send to queue " + ref);
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
                        last.add(Calendar.SECOND, takesS);
                        last.add(Calendar.MINUTE, min);
                        last.add(Calendar.HOUR, hour);

                        logger.info("user: step(1/2) read " + read +
                                ", refs will be collected in " + word +
                                ", near " + last + ", remaining " +
                                (remaining - read) + " references from " +
                                companies + ", current speed " +
                                (int) (60 / speed) + " per minute");
                        processingIterator.remove();
                        ages.remove(taskFuture);
                    } else {
                        ReferenceCollector collector = ages.get(taskFuture);
                        if (collector != null) {
                            if (collector.waiting() && collector.age() > 120) {
                                taskFuture.cancel(true);
                                collector.kill();
                                ages.remove(taskFuture);
                                tasks.add(taskRange);
                                processingIterator.remove();
                            }
                        }
                    }
                } catch (Exception e) {
                    //returning task to queue
                    tasks.add(taskRange);
                    logger.error("exception during reading range  " + taskRange + " " + e);
                }
            }
        }
        DispatcherNew.finished = true;
        logger.info("user:reading of refs finished" + "DispatcherNew.finished = true");

        //all read successfully
        try {
            Files.delete(knownPages);
        } catch (IOException e) {
            logger.error("can not delete file " + knownPages.toAbsolutePath());
        }
    }
}
