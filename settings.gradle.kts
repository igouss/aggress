rootProject.name = "aggress"

include(":common")
include(":crawler")
include(":frontend")
include(":webadmin")
include(":storage")
include(":crawler-admin")
include(":parsers")
include(":http")
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("bucket4jVersion", "4.0.1")
            version("buildScanPluginVersion", "1.13.4")
            version("commonsCodecVersion", "1.10") //  https://search.maven.org/artifact/commons-codec/commons-codec
            version("commonsCollections4Version", "4.1")
            version("commonsIoVersion", "2.6")
            version("daggerVersion", "2.16")
            version("elasticVersion", "6.2.4")
            version("gradleAptPluginVersion", "0.15")
            version("gradleDockerPluginVersion", "3.3.2")
            version("gsonVersion", "2.8.5")
            version("javaxInjectVersion", "1")
            version("joptVersion", "5.0.4")
            version("jsoupVersion", "1.11.3")
            version("jtsVersion", "1.13")
            version("junitVersion", "5.7.2")
            version("lettuceVersion", "4.4.5.Final")
            version("logbackVersion", "1.2.3")
            version("lombokVersion", "1.18.20")
            version("metricsVersion", "4.0.2")
            version("nettyVersion", "4.1.24.Final")
            version("okhttpVersion", "3.10.0")
            version("reflectionsVersion", "0.9.11")
            version("rxjavaVersion", "1.3.8")
            version("slf4jVersion", "1.7.32")
            version("thymeleafVersion", "3.0.9.RELEASE")
            version("validationApiVersion", "2.0.1.Final")
            version("vertxVersion", "3.5.2")
            version("guavaVersion", "30.1.1-jre")
            version("jansiVersion", "2.3.4")

            alias("commonsCodec").to("commons-codec", "commons-codec").versionRef("commonsCodecVersion")
            alias("commonsCollections4").to("org.apache.commons", "commons-collections4")
                .versionRef("commonsCollections4Version")
            alias("commonsIo").to("commons-io", "commons-io").versionRef("commonsIoVersion")
            alias("elasticsearch").to("org.elasticsearch", "elasticsearch").versionRef("elasticVersion")
            alias("elasticsearchClientTransport").to("org.elasticsearch.client", "transport")
                .versionRef("elasticVersion")
            alias("gson").to("com.google.code.gson", "gson").versionRef("gsonVersion")
            alias("guava").to("com.google.guava", "guava").versionRef("guavaVersion")
            alias("jansi").to("org.fusesource.jansi", "jansi").versionRef("jansiVersion")
            alias("javaxInject").to("javax.inject", "javax.inject").versionRef("javaxInjectVersion")
            alias("joptSimple").to("net.sf.jopt-simple", "jopt-simple").versionRef("joptVersion")
            alias("jsoup").to("org.jsoup", "jsoup").versionRef("jsoupVersion")
            alias("jts").to("com.vividsolutions", "jts").versionRef("jtsVersion")
            alias("junit").to("org.junit.jupiter", "junit-jupiter").versionRef("junitVersion")
            alias("lettuce").to("biz.paluch.redis", "lettuce").versionRef("lettuceVersion")
            alias("logbackClassic").to("ch.qos.logback", "logback-classic").versionRef("logbackVersion")
            alias("logbackCore").to("ch.qos.logback", "logback-core").versionRef("logbackVersion")
            alias("lombok").to("org.projectlombok", "lombok").versionRef("lombokVersion")
            alias("nettyCommon").to("io.netty", "netty-common").versionRef("nettyVersion")
            alias("okhttp").to("com.squareup.okhttp3", "okhttp").versionRef("okhttpVersion")
            alias("okhttpUrlconnection").to("com.squareup.okhttp3", "okhttp-urlconnection").versionRef("okhttpVersion")
            alias("reflections").to("org.reflections", "reflections").versionRef("reflectionsVersion")
            alias("rxjava").to("io.reactivex", "rxjava").versionRef("rxjavaVersion")
            alias("slf4j").to("org.slf4j", "slf4j-api").versionRef("slf4jVersion")
            alias("thymeleaf").to("org.thymeleaf", "thymeleaf").versionRef("thymeleafVersion")
            alias("validationApi").to("javax.validation", "validation-api").versionRef("validationApiVersion")
            alias("vertxCore").to("io.vertx", "vertx-core").versionRef("vertxVersion")
            alias("vertxWeb").to("io.vertx", "vertx-web").versionRef("vertxVersion")
            alias("vertxWebTemplThymeleaf").to("io.vertx", "vertx-web-templ-thymeleaf").versionRef("vertxVersion")

            bundle(
                "logging", listOf(
                    "slf4j",
                    "logbackClassic",
                    "logbackCore"
                )
            )
            bundle(
                "vertxWeb", listOf(
                    "thymeleaf",
                    "vertxCore",
                    "vertxWeb",
                    "vertxWebTemplThymeleaf"
                )
            )

            bundle(
                "testing", listOf(
                    "junit"
                )
            )

            bundle(
                "okhttp", listOf(
                    "okhttp",
                    "okhttpUrlconnection"
                )
            )
            bundle(
                "elasticsearch", listOf(
                    "elasticsearch",
                    "elasticsearchClientTransport"
                )
            )
        }
    }
}


pluginManagement {
    plugins {
//        id("io.micronaut.application") version("2.0.4")
//        id("io.micronaut.library") version("2.0.4")
    }
}


