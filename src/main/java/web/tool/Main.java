package web.tool;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import command.line.ArgumentsParser;
import date.Dates;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selenium.WebDriverHub;
import storage.CSVStorage;
import storage.Item;
import web.tool.eg.kompass.com.Kompass;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: YamStranger
 * Date: 5/13/15
 * Time: 10:51 PM
 */
public class Main {
    static {
        //configure logging
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator jc = new JoranConfigurator();
            jc.setContext(context);
            context.reset(); // override default configuration
            // inject the name of the current application as "application-name"
            // property of the LoggerContext
            jc.doConfigure("logback.xml");
        } catch (JoranException e) {
            System.out.println("could not configure logging " + e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws Exception {
        ArgumentsParser parser = new ArgumentsParser(args);
        Map<String, String> arguments = parser.arguments();
        String CONFIG = "config";
        String LOGIN = "login";
        String PASSWORD = "password";
        String LOGIN_INSTANCES = "logIn_instances";
        String NOT_LOGIN_INSTANCES = "not_logIn_instances";
        String SESSION = "session";
        String KEYWORD_FILE = "keyword_file";
        String MAX_PAGE_LOAD_TIME = "max_page_load_time";
        String REMOTE = "remote";
        String REUSE_BROWSERS = "reuse_browsers";
        String SELENIUM_SERVER = "server";

        String config = arguments.get(CONFIG);
        if (config == null || config.isEmpty()) {
            logger.error("user:please specify config file \"-" + CONFIG + "\" option, in same directory, encoding UTF-8");
            return;
        } else {
            logger.info("user:config readed from \"" + config + "\"");
        }
        logger.info("user:Expected:");
        logger.info("user:-" + LOGIN);
        logger.info("user:-" + PASSWORD);
        logger.info("user:-" + LOGIN_INSTANCES);
        logger.info("user:-" + NOT_LOGIN_INSTANCES);
        logger.info("user:-" + SESSION);
        logger.info("user:-" + KEYWORD_FILE);
        logger.info("user:-" + MAX_PAGE_LOAD_TIME);
        logger.info("user:-" + REMOTE);
        logger.info("user:-" + REUSE_BROWSERS);
        logger.info("user:-" + SELENIUM_SERVER);

        logger.info("user:Read:");
        Path configFile = Paths.get(config);
        List<String> configs = new LinkedList<>();
        if (Files.exists(configFile)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(configFile.toAbsolutePath().toFile()),
                    Charset.forName("UTF-8")))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    configs.add(line);
                }
            } catch (IOException e) {
                logger.error("Config reading error" + e);
                return;
            }
        } else {
            logger.info("user:config file not exists, created example.cfg");
            Path example = Paths.get("example.cfg");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(example.toAbsolutePath().toFile(), true),
                    Charset.forName("UTF-8").newEncoder()
            ))) {
                writer.write("Config:");
                writer.newLine();
                writer.write("-" + LOGIN + "=login_example");
                writer.newLine();
                writer.write("-" + PASSWORD + "=password_example");
                writer.newLine();
                writer.write("-" + LOGIN_INSTANCES + "=1");
                writer.newLine();
                writer.write("-" + NOT_LOGIN_INSTANCES + "=1");
                writer.newLine();
                writer.write("-" + SESSION + "=example_session");
                writer.newLine();
                writer.write("-" + KEYWORD_FILE + "=key_words_file_name_example");
                writer.newLine();
                writer.write("-" + MAX_PAGE_LOAD_TIME + "=50");
                writer.newLine();
                writer.write("-" + REMOTE + "=true");
                writer.newLine();
                writer.write("-" + REUSE_BROWSERS + "=false");
                writer.newLine();
                writer.write("-" + SELENIUM_SERVER + "=false");
                writer.newLine();


                writer.flush();
            } catch (IOException e) {
                logger.error("Storage error, cant write used values ", e);
            }
        }
        logger.info("user:read from \"" + config + "\" file");
        for (final String entry : configs) {
            logger.info("user: " + entry);
        }

        parser = new ArgumentsParser(configs.toArray(new String[configs.size()]));
        arguments = parser.arguments();
        String keywordFile = arguments.get(KEYWORD_FILE);
        String login = arguments.get(LOGIN);
        String password = arguments.get(PASSWORD);
        String logInInstances = arguments.get(LOGIN_INSTANCES);
        String notLogInInstances = arguments.get(NOT_LOGIN_INSTANCES);
        String session = arguments.get(SESSION);
        String maxPageLoadTime = arguments.get(MAX_PAGE_LOAD_TIME);
        String remote = arguments.get(REMOTE);
        String reuse = arguments.get(REUSE_BROWSERS);
        String server = arguments.get(SELENIUM_SERVER);

        if ((keywordFile == null || keywordFile.isEmpty()) ||
                (login == null || login.isEmpty()) ||
                (password == null || password.isEmpty()) ||
                (logInInstances == null || logInInstances.isEmpty()) ||
                (session == null || session.isEmpty()) ||
                (maxPageLoadTime == null || maxPageLoadTime.isEmpty()) ||
                (remote == null || remote.isEmpty()) ||
                (reuse == null || reuse.isEmpty()) ||
                (notLogInInstances == null || notLogInInstances.isEmpty()) ||
                (server == null || server.isEmpty())
                ) {
            logger.error("user:please specify all params");
            return;
        }
        String keyword = "";
        Path file = Paths.get(keywordFile);
        if (Files.exists(file)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file.toAbsolutePath().toFile()),
                    Charset.forName("UTF-8")))) {
                keyword = reader.readLine();
                if (keyword != null) {
                    logger.info("user:keyword \"" + keyword + "\"");
                }

                if (keyword == null || keyword.isEmpty()) {
                    logger.error("user:keyword is not specified");
                    return;
                }
            } catch (IOException e) {
                logger.error("Config reading error", e);
                return;
            }
        } else {
            logger.error("user:Could not find file with keyword, name=\"" + keywordFile + "\"");
            return;
        }
        int numberLogIn = 1;
        try {
            numberLogIn = Integer.valueOf(logInInstances);
        } catch (Exception e) {
            logger.error("user:" + LOGIN_INSTANCES + " it not number");
            return;
        }
        logger.info("user:" + LOGIN_INSTANCES + " =" + numberLogIn);
        int numberNotLogIn = 1;
        try {
            numberNotLogIn = Integer.valueOf(notLogInInstances);
        } catch (Exception e) {
            logger.error("user:" + NOT_LOGIN_INSTANCES + " it not number");
            return;
        }
        logger.info("user:" + NOT_LOGIN_INSTANCES + " =" + numberNotLogIn);

        int pageLoadTime = 30;
        try {
            pageLoadTime = Integer.valueOf(maxPageLoadTime);
        } catch (Exception e) {
            logger.info("user:" + MAX_PAGE_LOAD_TIME + " it not number");
            return;
        }
        logger.info("user:" + MAX_PAGE_LOAD_TIME + " =" + pageLoadTime);

        boolean isRemote = Boolean.valueOf(remote);
        logger.info("user:Using " + (isRemote ? "remote" : "local") + " browser");

        boolean isReusable = Boolean.valueOf(reuse);
        if (isReusable) {
            logger.info("user:Using reusable browser");
        } else {
            logger.info("user:Creating new instance of browser every time");

        }
        System.out.println("using server \"" + server + "\"");
        String[] servers = server.split(";");
        LinkedList<String> list = new LinkedList<>();
        list.addAll(Arrays.asList(servers));
        System.out.println("using servers " + Arrays.toString(servers));
        Dates start = new Dates();
        WebDriverHub notSecure = new WebDriverHub(isReusable, isRemote, list, pageLoadTime);
        notSecure.available(numberNotLogIn);
        WebDriverHub secure = new WebDriverHub(isReusable, isRemote, list, pageLoadTime);
        secure.available(numberLogIn);
        try {

            BlockingQueue<Item> results = new LinkedBlockingQueue<Item>(1000);

            //checking if password are correct
            WebDriver driver = secure.driver();
            WebDriverWait wait = secure.driverWait(driver);
            Kompass kompass = new Kompass(driver, wait);
            logger.info("user:check credentials");
            kompass.login(login, password);
            if (!kompass.isLogged()) {
                kompass.quit();
                logger.error("user:can not work with incorrect login and password");
                return;
            }
            kompass.quit();
            logger.info("user:start search");
/*        String session = keyword;
        session.replaceAll("\\W+", "");*/
            Path result = Paths.get(session + "_companies.csv");
            CSVStorage storage = new CSVStorage(result, results, true);
            storage.start();
            DispatcherNew dispatcher = new DispatcherNew(session, keyword, login, password, results, Math.max(numberNotLogIn, numberLogIn), secure, notSecure);
            dispatcher.start();
            dispatcher.join();
            logger.info("user:done, takes " + new Dates().difference(start, Calendar.SECOND) + " seconds");
/*            dispatcher.interrupt();
            storage.interrupt();*/
            secure.quit();
            notSecure.quit();
            System.out.println("quit");
        } finally {
            notSecure.quit();
            secure.quit();
        }
        return;
    }
}
