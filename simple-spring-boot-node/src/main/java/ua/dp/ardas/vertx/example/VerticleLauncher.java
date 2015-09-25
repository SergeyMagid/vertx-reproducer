package ua.dp.ardas.vertx.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.dropwizard.MetricsService;
import ua.dp.ardas.vertx.example.verticle.BlockOperationVerticle;
import ua.dp.ardas.vertx.example.verticle.StuffPublisher;

public class VerticleLauncher extends AbstractVerticle implements Handler<AsyncResult<String>>{

	public static final Logger logger = LoggerFactory.getLogger(Application.class.getSimpleName());
    private final String channelName;


    public VerticleLauncher(String channelName) {
        this.channelName = channelName;
    }

	@Override
	public void start() throws Exception {

		DeploymentOptions deploymentOptions = new DeploymentOptions();
		deploymentOptions.setConfig(config());

        vertx.deployVerticle(new StuffPublisher(), this);
        vertx.deployVerticle(new BlockOperationVerticle(channelName), this);

        MetricsService metricsService = MetricsService.create(vertx);

        String address = "health-node-" + channelName;
        logger.info("register health node at " + address);
        vertx.eventBus().consumer(address,
                health -> {
                    logger.info("receive health event");
                    health.reply(metricsService.getMetricsSnapshot(vertx));
                });
	}

    @Override
	public void handle(AsyncResult<String> event) {
		logger.info("Started verticle " + event.succeeded() + ", " + event.result());
	}

}
