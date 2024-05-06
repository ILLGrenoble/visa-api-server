package eu.ill.visa;


import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String ... args) {
        logger.info("Starting VISA application");
        Quarkus.run(args);
    }
}
