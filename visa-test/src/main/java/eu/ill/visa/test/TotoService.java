package eu.ill.visa.test;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TotoService {

    private final TotoRepository repository;

    public TotoService(final TotoRepository repository) {
        this.repository = repository;
    }

    public List<Toto> getAll() {
        return this.repository.getAll();
    }
}
