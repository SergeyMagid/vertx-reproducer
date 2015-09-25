package ua.dp.ardas.vertx.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.dropwizard.MetricsService;

public class VerticleLauncher extends AbstractVerticle {

	public static final Logger logger = LoggerFactory.getLogger(VerticleLauncher.class.getSimpleName());

	@Override
	public void start() throws Exception {

        String chName = config().getString("channelName");

        DeploymentOptions deploymentOptions = new DeploymentOptions();
		deploymentOptions.setConfig(config());

        MetricsService metricsService = MetricsService.create(vertx);

        String address = "health-node-" + chName;
        logger.info("register health node at " + address);
        vertx.eventBus().consumer(address,
                health -> {
                    logger.info("receive health event");
                    health.reply(metricsService.getMetricsSnapshot(vertx));
                });
	}

}
