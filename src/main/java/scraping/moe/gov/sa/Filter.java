package scraping.moe.gov.sa;

import java.nio.charset.*;
import java.io.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * User: YamStranger
 * Date: 5/5/15
 * Time: 9:58 AM
 */
public class Filter {

    public static void main(String... args) throws IOException {
        Path results = Paths.get("results").toAbsolutePath();
        Set<String> filtered = new HashSet<>();
        String header = "\"School ID\",\"School Ministry Number\",\"School Name\",\"Year Established\",\"School Classification\",\"Education Office\",\"District Name\",\"Administrative Region\",\"Administrative Contry\",\"Administrative Centers\",\"School Address\",\"School Phone\",\"School Fax\",\"Study Level\",\"School Gender\",\"Student Count\",\"Saudi Student Count\",\"NonSaudi Student Count\",\"Teachers Count\",\"Saudi Teacher Count\",\"NonSaudi Teacher Count\",\"Classes Count\"";
        try (DirectoryStream<Path> files = Files.newDirectoryStream(results, "*.bin")) {
			
            for (final Path result : files) {
			    System.out.println(result.toAbsolutePath().toString());
                BufferedReader reader = Files.newBufferedReader(result);
                String read = null;
                int row=0;
                while ((read = reader.readLine()) != null) {
                    row++;
                    if (!read.contains(header)) {
                        if (filtered.contains(read)) {
                           // System.out.println(result.toAbsolutePath() + "at "+row+" contains " + read);
                        }
                        filtered.add(read);
                    } else {
                       // System.out.println("found header");
                    }
                }
                reader.close();
            }

        } catch (IOException e) {
			System.out.println(e);
        }

        Path clear = results.resolve("result.bin");
        if (!Files.exists(clear)) {
            Files.createFile(clear);
        }
        BufferedWriter writer = Files.newBufferedWriter(clear);
        writer.write(header);
        writer.newLine();
        for (final String row : filtered) {
            writer.write(row);
            writer.newLine();
        }
        writer.flush();
        writer.close();


    }
}
