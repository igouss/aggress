package com.naxsoft;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CommandLineParser {
    public static OptionSet parse(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("help");
        parser.accepts("populate");
        parser.accepts("clean");
        parser.accepts("crawl");
        parser.accepts("parse");
        parser.accepts("createESIndex");
        parser.accepts("createESMappings");
        parser.accepts("server");

        return parser.parse(args);
    }
}
