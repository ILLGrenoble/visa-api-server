package eu.ill.visa.test;

import eu.ill.visa.core.entity.Image;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class TestImageRepository extends  TestAbstractRepository {

    @Inject
    public TestImageRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Image> getAll() {
        String queryString = """
            SELECT i
            FROM TestImage i
            ORDER BY i.id
        """;
        TypedQuery<Image> query = this.getEntityManager().createQuery(queryString, Image.class);

        return query.getResultList();
    }
}
