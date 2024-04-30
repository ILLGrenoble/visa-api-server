package eu.ill.visa.test;

import jakarta.persistence.EntityManager;

 public abstract class TestAbstractRepository {

    private EntityManager entityManager;

    public TestAbstractRepository() {
    }

    public TestAbstractRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public EntityManager getEntityManager() {
        return entityManager;
    }
}
