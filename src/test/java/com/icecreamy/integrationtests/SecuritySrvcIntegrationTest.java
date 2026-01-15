package com.icecreamy.integrationtests;

import static org.junit.jupiter.api.Assertions.*;

import com.icecreamy.dao.impl.CustomerDaoImpl;
import com.icecreamy.entity.Customer;
import com.icecreamy.obj.SignupWebModel;
import com.icecreamy.obj.UserRole;
import com.icecreamy.obj.WebResponse;
import com.icecreamy.service.SecurityService;
import com.icecreamy.util.AppConstants;
import com.icecreamy.util.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecuritySrvcIntegrationTest extends BasePersistenceTest {

    private CustomerDaoImpl customerDao;
    private SecurityService securityService;

    @BeforeEach
    public void setup() throws Exception {
        customerDao = new CustomerDaoImpl();
        securityService = new SecurityService();

        injectEntityManager(customerDao, "em");

        java.lang.reflect.Field daoField = SecurityService.class.getDeclaredField("customerDaoStub");
        daoField.setAccessible(true);
        daoField.set(securityService, customerDao);
    }

    @Test
    public void testSignUpSuccess() {
        SignupWebModel model = new SignupWebModel("Ivan", "Ivanov", "password123", "password123", "new@test.com");

        WebResponse response = securityService.signUp(model);

        assertTrue(response.isSuccesfullOpt());
        assertEquals(AppConstants.GOOD_SIGNUP, response.getMessage());

        Customer saved = em.createQuery("SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
                .setParameter("email", "new@test.com")
                .getSingleResult();
        assertNotNull(saved);
        assertEquals("Ivan", saved.getFirstName());
    }

    @Test
    public void testSignUpFailureEmailExists() {
        Customer existing = new Customer("Existing", "User", "pwd", "exists@test.com", UserRole.CUSTOMER);
        em.persist(existing);
        em.flush();

        SignupWebModel model = new SignupWebModel("New", "User", "pwd", "pwd", "exists@test.com");

        WebResponse response = securityService.signUp(model);

        assertFalse(response.isSuccesfullOpt());
        assertEquals(AppConstants.EMAIL_EXISTS, response.getAdditionalInfo());
    }

    @Test
    public void testSignUpFailurePasswordsDontMatch() {
        SignupWebModel model = new SignupWebModel("Ivan", "Ivanov", "pass1", "pass2", "mismatch@test.com");

        WebResponse response = securityService.signUp(model);

        assertFalse(response.isSuccesfullOpt());
        assertEquals(AppConstants.PASS_DONT_MATCH, response.getAdditionalInfo());
    }

    @Test
    public void testSignUpFailureInvalidEmail() {
        SignupWebModel model = new SignupWebModel("Ivan", "Ivanov", "pwd", "pwd", "not-an-email");

        WebResponse response = securityService.signUp(model);

        assertFalse(response.isSuccesfullOpt());
        assertEquals(AppConstants.EMAIL_NOT_VALID, response.getAdditionalInfo());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        String email = "login@test.com";
        String pass = "secret123";
        String encrypted = Utilities.encrypt(pass, pass, email);
        Customer c = new Customer("Ivan", "Ivanov", encrypted, email, UserRole.CUSTOMER);
        em.persist(c);
        em.flush();

        WebResponse response = securityService.login(email, pass);

        assertTrue(response.isSuccesfullOpt());
        assertEquals(AppConstants.GOOD_LOGIN, response.getMessage());
    }

    @Test
    public void testLoginFailureWrongPassword() throws Exception {
        String email = "wrongpass@test.com";
        String correctPass = "123";
        String encrypted = Utilities.encrypt(correctPass, correctPass, email);
        em.persist(new Customer("Ivan", "Ivanov", encrypted, email, UserRole.CUSTOMER));
        em.flush();

        WebResponse response = securityService.login(email, "456");

        assertFalse(response.isSuccesfullOpt());
        assertEquals(AppConstants.BAD_LOGIN, response.getMessage());
    }
}