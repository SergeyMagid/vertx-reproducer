package ua.dp.ardas.test.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.apache.commons.lang3.time.DateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleClient extends AbstractVerticle {

    public static final Logger logger = LoggerFactory.getLogger(SimpleClient.class.getSimpleName());
    public static final String HEALTH_CHANNEL_PREFIX = "health-node-";
    private List channelNames;


    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);


        BridgeOptions opts = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
                .addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));

        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus_test/*").handler(ebHandler);
        router.route().handler(StaticHandler.create());

        EventBus eb = vertx.eventBus();
        vertx.createHttpServer().requestHandler(router::accept).listen(8082);

        channelNames = config().getJsonArray("monitorChannels").getList();

        logger.info("Found channel to monitoring " + channelNames);

        vertx.setPeriodic(2000, event -> {
            for (int i = 0; i< channelNames.size() ;i++) {
                final String channelName = channelNames.get(i).toString();
                logger.info("Send health event to " + HEALTH_CHANNEL_PREFIX + channelName);
                eb.<JsonObject>send(HEALTH_CHANNEL_PREFIX + channelName, "check", healthHandler -> {
                    JsonObject health = new JsonObject();
                    health.put("name", channelName);
                    publishHealthStatus(health, healthHandler);
                });
            }
        });
    }


    private Map<String, Long> lastErrors = new HashMap<>();

    private void publishHealthStatus(JsonObject health, AsyncResult<Message<JsonObject>> healthHandler) {
        String name = health.getString("name");
        if (healthHandler.failed()) {
            health.put("status", "failed");
            health.put("message", healthHandler.result());
            logger.warn("HEALTH PROBLEM " + healthHandler.result(),
                    healthHandler.cause());
            lastErrors.put(name, System.currentTimeMillis());
        } else {
            JsonObject result = healthHandler.result().body();


            if (lastErrors.containsKey(name) &&
                    lastErrors.get(name) > (System.currentTimeMillis() - DateUtils.MILLIS_PER_MINUTE)) {
                health.put("status", "unstable");
            } else {
                health.put("status", "success");
            }
        }

        vertx.eventBus().publish("vertx-health", health);
    }

}

