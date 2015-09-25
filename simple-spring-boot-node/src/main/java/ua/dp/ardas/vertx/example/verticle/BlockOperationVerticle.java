package ua.dp.ardas.vertx.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

public class BlockOperationVerticle extends AbstractVerticle {

    public static final Logger logger = LoggerFactory.getLogger(BlockOperationVerticle.class.getSimpleName());
    private String channelName;

    public BlockOperationVerticle(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public void start() throws Exception {
        String address = StringUtils.isBlank(channelName) ? "block-operation-channel" : channelName;
        logger.info("Register handler for channel: " + address);
        vertx.eventBus().<JsonObject>consumer(address,
                eventRequest -> {
                    JsonObject body = eventRequest.body();
                    logger.info("Receive event to make blocking operation. " + body.toString());
                    Integer count = body.getInteger("count");

                    vertx.executeBlocking(block -> {
                        try {
                            logger.info("Before long operation ");

                            Thread.sleep(5000);

                            logger.info("After long operation ");

                            block.complete("ok");
                        } catch (Exception ex) {
                            logger.error("Can not get..", ex);
                            block.fail("Can not get..");
                        }
                    }, result -> {
                        if (result.failed()) {
                            logger.error("Can not get..", result.cause());
                            eventRequest.fail(400, "Can not get...");
                            return;
                        }
                        eventRequest.reply(count);
                    });
                });
    }

}
