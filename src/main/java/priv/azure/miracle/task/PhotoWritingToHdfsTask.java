package priv.azure.miracle.task;

import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import priv.azure.miracle.bean.management.Shop;
import priv.azure.miracle.bean.management.Status;
import priv.azure.miracle.data.generator.pojo.Photo;

public class PhotoWritingToHdfsTask implements Runnable{
	private Status status;
	private Shop<Photo> photoShop;
	private Configuration configuration;
	
	public PhotoWritingToHdfsTask(Status status, Shop<Photo> photoShop) {
		this.status = status;
		this.photoShop = photoShop;
		configuration = new Configuration();
	}

	@Override
	public void run() {
		Photo photo = null;
		FSDataOutputStream fsDataOutputStream = null;
		FileSystem fileSystem = null;
		while(!status.checkPhotosWritingSucceed()) {
			photo = photoShop.offer();
			try {
				fileSystem = FileSystem.get(URI.create(photo.getPath()), configuration);
				fsDataOutputStream = fileSystem.create(new Path(photo.getPath()));
				ImageIO.write(photo.getImage(), "jpg", fsDataOutputStream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				status.addPhotoWritingCoundAndGet();
			}
		}
	}
}
