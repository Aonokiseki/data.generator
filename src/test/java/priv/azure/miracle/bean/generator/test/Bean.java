package priv.azure.miracle.bean.generator.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import priv.azure.miracle.bean.management.Configurer;
import priv.azure.miracle.data.generator.pojo.Constants;
import priv.azure.miracle.data.generator.pojo.Setting;

public class Bean {
	
	private final static Logger LOGGER = LogManager.getLogger();
	private AnnotationConfigApplicationContext context;
	
	@BeforeClass
	public void beforeClass() {
		Constants.reconfigureLog4j2();
		context = new AnnotationConfigApplicationContext(Configurer.class);
	}
	
	@AfterClass
	public void afterClass() {
		if(context != null)
			context.close();
	}
	
	@Test
	public void setting() {
		Setting setting = context.getBean(Setting.class);
		LOGGER.debug(setting);
	}
}
