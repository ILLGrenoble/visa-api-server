package eu.ill.visa.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class TotoRepository {

    private final EntityManager entityManager;

    public TotoRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Toto> getAll() {
        String queryString = """
            SELECT h
            FROM Toto h
            ORDER BY h.id
        """;
        TypedQuery<Toto> query = entityManager.createQuery(queryString, Toto.class);

        return query.getResultList();
    }
}
