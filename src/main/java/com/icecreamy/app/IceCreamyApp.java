package com.icecreamy.app;

import com.icecreamy.dao.impl.OrderDaoImpl;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import org.jboss.logging.Logger;

import com.icecreamy.dao.impl.CustomerDaoImpl;
import com.icecreamy.dao.impl.ProductDaoImpl;
import com.icecreamy.util.Utilities;

@Singleton
@Startup
public class IceCreamyApp {

	private static final Logger logg = Logger.getLogger(IceCreamyApp.class);

	@EJB
	private ProductDaoImpl productDaoStub;
	@EJB
	private CustomerDaoImpl  customerDaoStub;
    @EJB
    private OrderDaoImpl orderDaoStub;
	@PostConstruct
	public void init() {
		logg.info("IceCreamy Init applicaiton...");
        Utilities.initFileSystem();
		Utilities.initializeDB(productDaoStub,customerDaoStub,orderDaoStub);
		logg.info("IceCreamy Init applicaiton finished");
	}

}
