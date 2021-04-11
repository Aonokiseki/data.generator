package priv.azure.miracle.utility;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

public final class PhotoOperator {
	private PhotoOperator(){}
	/**
	 * 
	 * Text类，目前用于生成文字图片<br><br>
	 * 
	 * 成员变量:<br>
	 * String content - 文本内容<br>
	 * Color color - 文本颜色<br>
	 * Font font   - 文本字体<br>
	 * int lineSpace - 行间距<br>
	 * int margin - 页前空白<br>
	 *
	 */
	public static class Text{
		private String content;
		private Color color;;
		private Font font;
		private int lineSpace;
		private int margin;
		
		public static Text build(String content) {
			return new Text(content);
		}
		private Text(String content) {
			this.content = content;
			this.color = Color.BLACK;
			this.font = new Font("Serief", Font.PLAIN, 16);
			this.lineSpace = 0;
			this.margin = 0;
		}
		public Text setMargin(int margin) {
			if(margin > 0)
				this.margin = margin;
			return this;
		}
		public int getMargin() {
			return this.margin;
		}
		public Text setLineSpace(int lineSpace) {
			if(lineSpace > 0)
				this.lineSpace = lineSpace;
			return this;
		}
		public int getLineSpace() {
			return this.lineSpace;
		}
		public Text setContent(String content){
			this.content = content;
			return this;
		}
		public String getContent(){
			return this.content;
		}
		public Text setColor(Color color){
			this.color = color;
			return this;
		}
		public Color getColor(){
			return this.color;
		}
		public Text setFont(Font font){
			this.font = font;
			return this;
		}
		public Font getFont(){
			return this.font;
		}
	}
	
	private final static String EXCEPTION_INFORMATION_WHEN_CHARACTER_SIZE_EQUALS_ZERO = 
			"Characters'size equal zero. Please check your parameter";
	/**
	 * 将文本内容打印到图片中
	 * @param texts 文本列表, 这是一个由Text类组成的List, 类Text保存着每条文本的文本,颜色和字体
	 * @param directory 存储目录
	 * @param fileName 图片名
	 * @param background 背景色,空则默认为白色
	 * @return String 图片的绝对路径
	 * @throws IOException
	 * @throws IllgalStateException 当根据文本计算出的像素为0时(这样无法创建画布)
	 */
	public static String transferTextToPicture(
			List<Text> texts, String directory, String fileName, Color background) throws IOException {
		File picture = new File(directory, fileName + ".jpg");
		FontRenderContext fontRenderContext = 
				new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false);
		int size[] = calculateCharactersSize(texts, fontRenderContext);
		if(size[0] == 0 || size[1] == 0) {
			throw new IllegalStateException(EXCEPTION_INFORMATION_WHEN_CHARACTER_SIZE_EQUALS_ZERO);
		}
		/* 画布尺寸要比文本尺寸大一点 */
		int width = size[0] + 1; int height = size[1] + 3;
		write(picture, width, height, texts, background, fontRenderContext);
		return picture.getAbsolutePath();
	}
	/**
	 * 计算图片中的文字占用的像素数
	 * @param texts
	 * @param fontRenderContext
	 * @return int[0]-宽度; int[1]-高度
	 */
	private static int[] calculateCharactersSize(List<Text> texts, FontRenderContext fontRenderContext) {
		Text text; Rectangle2D rectangle = null;
		int width = 0; int height = 0; int currentWidth = 0;
		for(int i=0, size=texts.size(); i<size; i++) {
			text = texts.get(i);
			if(text.content.isEmpty())
				continue;
			rectangle = text.font.getStringBounds(text.content, fontRenderContext);
			currentWidth = (int) Math.round(rectangle.getWidth()) + text.margin;
			if(currentWidth > width)
				width = currentWidth;
			height += (int)Math.floor(rectangle.getHeight()) + text.lineSpace;
		}
		return new int[] {width, height};
	}
	/**
	 * 创建画布, 将文字按照字体和大小设置写入图片中
	 * @param picture
	 * @param width
	 * @param height
	 * @param texts
	 * @param background
	 * @param fontRenderContext
	 * @throws IOException
	 */
	private static void write(File picture, int width, int height, List<Text> texts, Color background, 
			FontRenderContext fontRenderContext) throws IOException {
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
	    Graphics graphics = image.getGraphics();
	    if(background == null)
	    	background = Color.WHITE;
	    graphics.setColor(background);
        graphics.fillRect(0,0,width,height);
        int brushOrdinate = 0; Text text; Rectangle2D rectangle = null; String str;
        for(int i=0, size=texts.size(); i<size; i++) {
        	text = texts.get(i);
        	str = text.content;
        	/* 空串跳过 */
        	if(str.isEmpty())
        		continue;
        	graphics.setColor(text.color);
            graphics.setFont(text.font);
            rectangle = text.getFont().getStringBounds(str, fontRenderContext);
            /* 挪动画笔纵坐标 */
            brushOrdinate += (int) Math.round(rectangle.getHeight());
            /* 绘图 */
            graphics.drawString(str, text.margin, brushOrdinate);
            brushOrdinate += text.lineSpace;
        }
        graphics.dispose();
        ImageIO.write(image, "jpg", picture);
        image.flush();
	}
	
	public static BufferedImage transferTextToPicture(List<Text> texts, Color background) throws IOException {
		FontRenderContext fontRenderContext = 
				new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false);
		int size[] = calculateCharactersSize(texts, fontRenderContext);
		if(size[0] == 0 || size[1] == 0) {
			throw new IllegalStateException(EXCEPTION_INFORMATION_WHEN_CHARACTER_SIZE_EQUALS_ZERO);
		}
		/* 画布尺寸要比文本尺寸大一点 */
		int width = size[0] + 1; int height = size[1] + 3;
		return write(width, height, texts, background, fontRenderContext);
	}
	private static BufferedImage write(int width, int height, List<Text> texts, Color background,
			FontRenderContext fontRenderContext) throws IOException {
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
	    Graphics graphics = image.getGraphics();
	    if(background == null)
	    	background = Color.WHITE;
	    graphics.setColor(background);
        graphics.fillRect(0,0,width,height);
        int brushOrdinate = 0; Text text; Rectangle2D rectangle = null; String str;
        for(int i=0, size=texts.size(); i<size; i++) {
        	text = texts.get(i);
        	str = text.content;
        	/* 空串跳过 */
        	if(str.isEmpty())
        		continue;
        	graphics.setColor(text.color);
            graphics.setFont(text.font);
            rectangle = text.getFont().getStringBounds(str, fontRenderContext);
            /* 挪动画笔纵坐标 */
            brushOrdinate += (int) Math.round(rectangle.getHeight());
            /* 绘图 */
            graphics.drawString(str, text.margin, brushOrdinate);
            brushOrdinate += text.lineSpace;
        }
        graphics.dispose();
        return image;
	}
}