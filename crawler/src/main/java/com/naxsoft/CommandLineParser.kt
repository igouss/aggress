package com.naxsoft

import joptsimple.OptionParser
import joptsimple.OptionSet

fun parse(args: Array<String>) : OptionSet {
    val parser = OptionParser()
    parser.accepts("help")
    parser.accepts("populate")
    parser.accepts("clean")
    parser.accepts("crawl")
    parser.accepts("parse")
    parser.accepts("createESIndex")
    parser.accepts("createESMappings")
    parser.accepts("server")

    return parser.parse(*args)
}
