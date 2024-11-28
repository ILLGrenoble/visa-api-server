package eu.ill.visa.persistence.repositories;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Statement;
import java.util.stream.Collectors;

abstract class AbstractRepository<T> {

    private EntityManager entityManager;

    // Required for Quarkus CDI (must have no-args constructor)
    // https://quarkus.io/guides/cdi-reference#simplified-constructor-injection
    protected AbstractRepository() {
    }

    protected AbstractRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void persist(final T object) {
        entityManager.persist(object);
        entityManager.flush();
    }

    public T merge(final T object) {
        T persistedObject = entityManager.merge(object);
        return persistedObject;
    }

    public void remove(final T object) {
        EntityManager em = entityManager;

        em.remove(em.contains(object) ? object : em.merge(object));
        em.flush();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void initialiseData(String filename) {
        final String sql = getFileAsString(filename);

        EntityManager em = this.getEntityManager();
        em.clear();
        Session session = em.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        });
    }

    private String getFileAsString(String fileName) {
        final InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return null;
    }
}
