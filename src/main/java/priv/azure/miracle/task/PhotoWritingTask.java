package priv.azure.miracle.task;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import priv.azure.miracle.bean.management.Shop;
import priv.azure.miracle.bean.management.Status;
import priv.azure.miracle.data.generator.pojo.Photo;

public class PhotoWritingTask implements Runnable{
	
	private Status status;
	private Shop<Photo> photoShop;
	
	public PhotoWritingTask(Status status, Shop<Photo> photoShop) {
		this.status = status;
		this.photoShop = photoShop;
	}

	@Override
	public void run() {
		Photo photo = null;
		while(!status.checkPhotosWritingSucceed()) {
			photo = photoShop.offer();
			try {
				ImageIO.write(photo.getImage(), "jpg", new File(photo.getPath()));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				status.addPhotoWritingCoundAndGet();
			}
		}
	}
}
