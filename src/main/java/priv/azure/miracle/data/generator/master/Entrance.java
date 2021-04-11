package priv.azure.miracle.data.generator.master;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import priv.azure.miracle.bean.management.Configurer;
import priv.azure.miracle.bean.management.RecordsOperator;
import priv.azure.miracle.data.generator.pojo.Constants;

public class Entrance {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Configurer.class);
		Constants.reconfigureLog4j2();
		RecordsOperator recordOperator = context.getBean(RecordsOperator.class);
		recordOperator.create();
		context.close();
		System.exit(0);
	}
}