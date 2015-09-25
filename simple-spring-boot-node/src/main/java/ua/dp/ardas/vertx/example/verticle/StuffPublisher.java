package ua.dp.ardas.vertx.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

public class StuffPublisher extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(StuffPublisher.class.getSimpleName());

    public StuffPublisher() {
    }

    @Override
    public void start() throws Exception {

        logger.info("[StuffPublisher] started");

        vertx.setPeriodic(1000, event -> {
                    for (int i = 0; i < 20; i++) {
                        String channel = String.format("stuff-channel-%s", i);
                        vertx.eventBus().publish(channel, createRandomStuff(i));
                    }
                }
        );
    }

    private JsonObject createRandomStuff(int i) {
        Random r = new Random();
        double rD = 0.901 + (0.950 - 0.901) * r.nextDouble();

        BigDecimal randomValue = BigDecimal.valueOf(rD).round(new MathContext(5, RoundingMode.HALF_EVEN));
        DateTime dateTime = new DateTime();

        JsonObject entries = new JsonObject();
        entries.put("number", i);
        entries.put("data", randomValue.doubleValue());
        entries.put("time", dateTime.getMillis());

        return entries;
    }
}
