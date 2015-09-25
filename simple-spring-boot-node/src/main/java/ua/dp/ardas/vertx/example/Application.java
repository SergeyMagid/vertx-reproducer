package ua.dp.ardas.vertx.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.XmlConfigBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

	public static final Logger logger = LoggerFactory.getLogger(Application.class.getSimpleName());
	private static Vertx vertx;

	public static void main(String[] args) {


        Config config = loadOverrideConfig();
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
        options.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));

		Vertx.clusteredVertx(options, res -> {
			if (res.succeeded()) {
                System.out.println("Cluster started");
				vertx = res.result();

				vertx.executeBlocking(block -> {
                    ApplicationContext context = SpringApplication.run(Application.class, args);
					block.complete(context);
				}, result -> {
                    vertx.deployVerticle(new VerticleLauncher(args[0]));
				});

			} else {
				logger.error("Can not start Vertx cluster!", res.cause());
			}
		});
	}

    private static Config loadOverrideConfig() {
        Config cfg = null;
        try (InputStream is = new FileInputStream("config/cluster.xml");
             InputStream bis = new BufferedInputStream(is)) {
            if (is != null) {
                cfg = new XmlConfigBuilder(bis).build();
                System.out.println("!!!!!!!!!! FOUND CLUSTER.xml");
            }
        } catch (IOException ex) {
            System.err.println("Failed to read config" + ex.getMessage());
        }
        return cfg;
    }

}
