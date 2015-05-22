package scraping.moe.gov.sa;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;

/**
 * User: YamStranger
 * Date: 4/30/15
 * Time: 2:50 PM
 */
public class Storage extends Thread {
    public static Logger logger = LoggerFactory.getLogger(Storage.class);
    private BlockingQueue<List<SchoolProfile>> results;
    private Path storage;
    public static String separator = ";";
    public static String valueSeparator = "\"";


    public Storage(Path file, BlockingQueue<List<SchoolProfile>> results) {
        this.storage = file;
        this.results = results;
    }

    @Override
    public void run() {
        setName("Storage-" + getName());
        System.out.println("started " + getName());
        /*init file*/
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(calendar.get(Calendar.MONTH) + 1).append(".");
        idBuilder.append(calendar.get(Calendar.DAY_OF_MONTH)).append(".");
        idBuilder.append(calendar.get(Calendar.YEAR)).append("_");
        idBuilder.append(calendar.get(Calendar.HOUR_OF_DAY)).append(".");
        idBuilder.append(calendar.get(Calendar.MINUTE)).append(".");
        idBuilder.append(calendar.get(Calendar.SECOND)).append(".");
        idBuilder.append(calendar.get(Calendar.MILLISECOND)).append(".");
        String fileId = idBuilder.toString();
        if (Files.exists(this.storage)) {
            if (Files.isDirectory(this.storage)) {
                storage = this.storage.resolve(fileId + this.storage.getFileName());
            } else {
//                storage = Paths.get(fileId + this.storage.getFileName());
            }
        }

        try (CSVWriter csv = new CSVWriter(new OutputStreamWriter(
                new FileOutputStream(storage.toAbsolutePath().toFile(), true),
                Charset.forName("UTF-8").newEncoder()
        ));) {
            if (Files.size(storage) <= 100) {
                //print header
                List<String> header = new LinkedList<>();
                header.add("School ID");
                header.add("School Ministry Number");
                header.add("School Name");
                header.add("Year Established");
                header.add("School Classification");
                header.add("Education Office");
                header.add("District Name");
                header.add("Administrative Region");
                header.add("Administrative Contry");
                header.add("Administrative Centers");
                header.add("School Address");
                header.add("School Phone");
                header.add("School Fax");
                header.add("Study Level");
                header.add("School Gender");
                header.add("Student Count");
                header.add("Saudi Student Count");
                header.add("NonSaudi Student Count");
                header.add("Teachers Count");
                header.add("Saudi Teacher Count");
                header.add("NonSaudi Teacher Count");
                header.add("Classes Count");
                csv.writeNext(header.toArray(new String[header.size()]), true);
                header.clear();
                csv.flush();
            }
            System.out.println("Storage writes result into " + this.storage.toAbsolutePath());
            final LinkedList<String> line = new LinkedList();
            while (!isInterrupted()) {
                List<SchoolProfile> current = results.take();
                System.out.println("In storage new result");
                for (final SchoolProfile schoolProfile : current) {
                    line.add(schoolProfile.id);
                    line.add(schoolProfile.ministryNumber);
                    line.add(schoolProfile.name);
                    line.add(schoolProfile.established);
                    line.add(schoolProfile.classification);
                    line.add(schoolProfile.office);
                    line.add(schoolProfile.district);
                    line.add(schoolProfile.region);
                    line.add(schoolProfile.country);
                    line.add(schoolProfile.centers);
                    line.add(schoolProfile.address);
                    line.add(schoolProfile.email);
                    line.add(schoolProfile.phone);
                    line.add(schoolProfile.fax);
                    line.add(schoolProfile.level);
                    line.add(schoolProfile.gender);
                    line.add(schoolProfile.students);
                    line.add(schoolProfile.saudiStudents);
                    line.add(schoolProfile.notSaudiStudents);
                    line.add(schoolProfile.teachers);
                    line.add(schoolProfile.saudiTeachers);
                    line.add(schoolProfile.notSaudiTeachers);
                    line.add(schoolProfile.classes);
                    csv.writeNext(line.toArray(new String[line.size()]), true);
                    line.clear();
                }
                csv.flush();
                sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("processing storage", e);
            this.interrupt();
            //exception handling left as an exercise for the reader
        }
    }
}
