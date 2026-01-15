package com.icecreamy.integrationtests;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BasePersistenceTest {
    protected static EntityManagerFactory emf;
    protected EntityManager em;

    @BeforeAll
    public static void setUpFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("icecreamy");
        }
    }

    @BeforeEach
    public void setUpParent() {
        if (!emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("icecreamy");
        }
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @AfterEach
    public void tearDownParent() {
        if (em != null) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    protected void injectEntityManager(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, em);
    }
}
