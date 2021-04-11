package priv.azure.miracle.data.generator.pojo;

import java.awt.image.BufferedImage;

public class Photo {
	private String path;
	private BufferedImage image;
	
	public Photo(String path, BufferedImage image) {
		this.path = path;
		this.image = image;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public BufferedImage getImage() {
		return image;
	}
	public void setImage(BufferedImage image) {
		this.image = image;
	}
}
