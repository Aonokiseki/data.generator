package priv.azure.miracle.bean.management;

import java.util.LinkedList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import priv.azure.miracle.data.generator.pojo.Constants;

@Component
@Scope("prototype")
public class Shop<T> {
	private int maxSize;
	private List<T> elements;
	
	public Shop() {
		this.maxSize = Constants.DEFAULT_TEMP_STOCKPILE_MAX_SIZE;
		this.elements = new LinkedList<T>();
	}
	public Shop(int maxSize) {
		this();
		this.maxSize = maxSize;
	}
	
	public synchronized boolean add(T element) {
		while(elements.size() >= this.maxSize)
			try {
				this.wait();
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
		this.notify();
		return elements.add(element);
	}
	
	public synchronized T offer() {
		while(elements.isEmpty())
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		this.notify();
		return elements.remove(0);
	}
}
