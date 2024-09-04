package eu.ill.visa.business.notification.renderers.email;


import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.loader.DelegatingLoader;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRenderer {

    protected PebbleTemplate getTemplate(final String templatePath) {
        List<Loader<?>> defaultLoadingStrategies = new ArrayList<>();
        defaultLoadingStrategies.add(new ClasspathLoader());
        defaultLoadingStrategies.add(new ClasspathLoader(Thread.currentThread().getContextClassLoader()));
        defaultLoadingStrategies.add(new FileLoader());
        final Loader<?> loader = new DelegatingLoader(defaultLoadingStrategies);

        final PebbleEngine engine = new PebbleEngine.Builder().loader(loader).build();
        return engine.getTemplate(templatePath);
    }

}
