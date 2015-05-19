package web.tool.eg.kompass.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: YamStranger
 * Date: 5/14/15
 * Time: 11:25 PM
 */
public class Company implements Item {
    public final String name;
    public final String address;
    public final List<String> phones;
    public final List<String> faxes;
    public final List<String> mails;
    public final List<String> website;
    public final String url;
    public final boolean needAuthorisation;
    public final boolean readLogged;
    private static final Logger logger = LoggerFactory.getLogger(Company.class);

    public Company(String name, String address, List<String> phones, List<String> faxes, List<String> mails, List<String> website, String url, boolean needAuthorisation, boolean readLogged) {
        this.name = name;
        this.address = address;
        this.website = new ArrayList<>(website);
        this.url = url;
        this.phones = new ArrayList<>(phones);
        this.faxes = new ArrayList<>(faxes);
        this.mails = new ArrayList<>(mails);
        this.needAuthorisation = needAuthorisation;
        this.readLogged = readLogged;
    }

    @Override
    public List<String> columns() {
        List<String> result = new ArrayList<String>(7);
        result.add(this.name);
        result.add(this.address);
        result.add(Arrays.toString(this.phones.toArray(new String[this.phones.size()])));
        result.add(Arrays.toString(this.faxes.toArray(new String[this.faxes.size()])));
        result.add(Arrays.toString(this.mails.toArray(new String[this.mails.size()])));
        result.add(Arrays.toString(this.website.toArray(new String[this.website.size()])));
        result.add(this.url);
        result.add(!readLogged ? "public" : "protected");
        return result;
    }

    @Override
    public String id() {
        return this.url;
    }
}
