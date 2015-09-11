import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress

import javax.servlet.ServletContext
import java.util.concurrent.TimeUnit

class BootStrap {
    TransportClient client = null;
//    private static final Log LOG = LogFactory.getLog(true)

    BootStrap() {
//        LOG.warn("BootStrap")
    }

    def init = { servletContext ->
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        this.client = new TransportClient(settings);
        this.client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        println "Starting elastic client"

        while(true) {
            int connectedNodes = this.client.connectedNodes().size();
            if(0 != connectedNodes) {
                break;
            }

            println("Waiting for elastic to connect to a node...");

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
            } catch (InterruptedException e) {
                log.error("Thread sleep failed", e);
            }
        }
        ((ServletContext) servletContext).setAttribute("elastic", client)
        println "Started elastic client"
//        LOG.warn("BootStrap init" + servletContext)
    }
    def destroy = {
//        LOG.warn("BootStrap destroy");
        if (null != client) {
            println "Stopping elastic client"
            client.close();
            println "Stopped elastic client"
        }
    }
}
