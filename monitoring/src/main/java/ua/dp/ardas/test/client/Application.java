package ua.dp.ardas.test.client;

import com.hazelcast.config.Config;
import com.hazelcast.config.InterfacesConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    public static final Logger logger = LoggerFactory.getLogger(Application.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        logger.info(">>> STARTING ");

        Config config = null;//loadOverrideConfig();
        ClusterManager mgr = config == null
                ? new HazelcastClusterManager()
                : new HazelcastClusterManager(config);

        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        if (null != config) {
            InterfacesConfig interfaces = config.getNetworkConfig().getInterfaces();
            if (null != interfaces && interfaces.isEnabled()) {
                options.setClusterHost(interfaces.getInterfaces().iterator().next());
            }
        }

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                logger.info("Starting cluster >>>");
                Vertx vertx = res.result();

                vertx.deployVerticle(new SimpleClient(args));
            } else {
                logger.error("Can not start Vertx cluster!", res.cause());
            }
        });

    }

}
