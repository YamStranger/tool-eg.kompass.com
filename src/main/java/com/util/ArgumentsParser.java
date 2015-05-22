package com.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Class helps parse incoming parameters
 * example of input parameter
 * -key=value
 * <p>
 * User: YamStranger
 * Date: 4/14/15
 * Time: 10:14 AM
 */
public class ArgumentsParser {
    private Map<String, String> arguments;
    private final String[] args;
    private static final Pattern KEY =
            Pattern.compile("(^-)(?<identifier>[^=]+)(=)(?<value>[^[^.+ ]]+)");

    /**
     * Constructor initialize zero length arguments
     */
    public ArgumentsParser() {
        this(new String[0]);
    }

    /**
     * Constructor save all arguments for future processing.
     *
     * @param args - array of key-value pairs
     */
    public ArgumentsParser(String... args) {
        this.arguments = new HashMap<>(args.length);
        this.args = Arrays.copyOf(args, args.length);
    }

    /**
     * Method parsed all entries of initialized array
     * <p>
     * if entry constance key-value pair - it will be added to result collection
     *
     * @return map with parsed values
     */
    public Map<String, String> arguments() {
        if (this.arguments.isEmpty()) {
            for (final String argument : this.args) {
                final Matcher keys = KEY.matcher(argument);
                if (keys.find()) {
                    final String identifier = keys.group("identifier");
                    final String value = keys.group("value");
                    if (!this.arguments.containsKey(identifier)) {
                        this.arguments.put(identifier, value);
                    } else {
                        throw new IllegalArgumentException("duplicated keys");
                    }
                }
            }
        }
        return Collections.unmodifiableMap(this.arguments);
    }


}
