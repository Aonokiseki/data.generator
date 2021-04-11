package priv.azure.miracle.bean.generator.test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import priv.azure.miracle.bean.management.Configurer;
import priv.azure.miracle.data.generator.pojo.Constants;

public class RecordOperator {
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
	public void recordOperator() {
		priv.azure.miracle.bean.management.RecordsOperator recordsOperator =
				context.getBean(priv.azure.miracle.bean.management.RecordsOperator.class);
		recordsOperator.create();
	}
}
