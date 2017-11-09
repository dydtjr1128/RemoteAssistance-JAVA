
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLX11;
import org.xerial.snappy.Snappy;

import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Rendering;



public class ScreenCapture extends JFrame {	
	private JLabel FPSlabel;
	private int FPScount = 0;
	private int screenWidth, screenHeight;
	int buffersize = 1;	
	byte imgbyte[][] = new byte[buffersize][];
	Vector<byte[]> b = new Vector<>();
	Vector<BufferedImage> bb = new Vector<>();
	ImgDoubleBufferTh ttt;
	public ScreenCapture() {
		setTitle("screen");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		//this.setAlwaysOnTop(true);
		ScreenPanel sp = new ScreenPanel();
		setContentPane(sp);
		setSize(800, 800);
		setVisible(true);
	/*	ImgDoubleBufferTh th2[] = new ImgDoubleBufferTh[buffersize];
		for (int i = 0; i < buffersize; i++) {
			th2[i] = new ImgDoubleBufferTh(i);
			th2[i].start();
		}*/
		ttt = new ImgDoubleBufferTh();
		ImgDoubleBufferTh ttt2 = new ImgDoubleBufferTh();
		//ttt.start();
		//ttt2.start();
		Thread th = new Thread(sp);
		th.start();		
		FPSCheckThread fps = new FPSCheckThread();
		fps.start();
	}

	class ScreenPanel extends JPanel implements Runnable {
		BufferedImage image;
		int screenWidth, screenHeight;
		Robot robot = null;

		public ScreenPanel() {
			screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
			screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
			FPSlabel = new JLabel("FPS : " + Integer.toString(FPScount));
			FPSlabel.setFont(new Font("¸¼Àº°íµñ", Font.BOLD, 20));
			FPSlabel.setBounds(10, 10, 100, 50);
			add(FPSlabel);
			addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {					
					if(e.getClickCount() == 1 && e.getButton() == e.BUTTON3){
						//System.out.println("4444");
						robot.mouseMove(e.getX()*screenWidth/getWidth(), e.getY()*screenHeight/getHeight());
						robot.mousePress(MouseEvent.BUTTON3_MASK);
						robot.mouseRelease(MouseEvent.BUTTON3_MASK);
					}
					else if(e.getClickCount()==2){
						//mx : wx = getwidth : winwidth
						robot.mouseMove(e.getX()*screenWidth/getWidth(), e.getY()*screenHeight/getHeight());
						robot.mousePress(MouseEvent.BUTTON1_MASK);
						robot.mouseRelease(MouseEvent.BUTTON1_MASK);
						robot.mousePress(MouseEvent.BUTTON1_MASK);
						robot.mouseRelease(MouseEvent.BUTTON1_MASK);
					}
					
					
					
				}
			});
			addMouseMotionListener(new MouseMotionListener() {
				
				@Override
				public void mouseMoved(MouseEvent e) {
					
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					
					
				}
			});
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
			screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
			screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
			image = robot.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));
			
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			
			super.paintComponent(g);			
			g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		
			
		
		}
		private BufferedImage screenShot(){
            //Creating an rbg array of total pixels
            int[] pixels = new int[screenWidth * screenHeight];
            int bindex;
            // allocate space for RBG pixels
            ByteBuffer fb = ByteBuffer.allocateDirect(screenWidth * screenHeight * 3);

            // grab a copy of the current frame contents as RGB
            GL11.glReadPixels(0, 0, screenWidth, screenHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, fb);

            BufferedImage imageIn = new BufferedImage(screenWidth, screenHeight,BufferedImage.TYPE_INT_RGB);
            // convert RGB data in ByteBuffer to integer array
            for (int i=0; i < pixels.length; i++) {
                bindex = i * 3;
                pixels[i] =
                    ((fb.get(bindex) << 16))  +
                    ((fb.get(bindex+1) << 8))  +
                    ((fb.get(bindex+2) << 0));
            }
            //Allocate colored pixel to buffered Image
            imageIn.setRGB(0, 0, screenWidth, screenHeight, pixels, 0 , screenWidth);

            //Creating the transformation direction (horizontal)
            AffineTransform at =  AffineTransform.getScaleInstance(1, -1);
            at.translate(0, -imageIn.getHeight(null));

            //Applying transformation
            AffineTransformOp opRotated = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage imageOut = opRotated.filter(imageIn, null);
            return imageOut;
        }
		public void run() {
			BufferedImage screenImage;
			int index = 0;
			long starttime,endtime;
			Rectangle rect = new Rectangle(0, 0, screenWidth, screenHeight);
			while (true) {
				try {
				//	image = screenShot();
					//Thread.sleep(100);
					//image = img[index];//robot.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));
					/*System.out.println("11");					
					GL11.glReadBuffer(GL11.GL_FRONT);
					System.out.println("22");

					int bpp = 3; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
					ByteBuffer buffer = BufferUtils.createByteBuffer(screenWidth * screenHeight * bpp);
					System.out.println("33");
					GL11.glReadPixels(0, 0, screenWidth, screenHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer );
					System.out.println("44");
					image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_3BYTE_BGR);
					
					for(int x = 0; x < screenWidth; x++) 
					{
					    for(int y = 0; y < screenHeight; y++)
					    {
					        int i = (x + (screenWidth * y)) * bpp;
					        int r = buffer.get(i) & 0xFF;
					        int g = buffer.get(i + 1) & 0xFF;
					        int b = buffer.get(i + 2) & 0xFF;
					        image.setRGB(x, screenHeight - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
					    }
					}*/
					 
					
					//image = JNAScreenShot.getScreenshot(rect);					
					//image = b.get(0);//robot.createScreenCapture(rect);		
					//System.out.println("capture2 " + b.size());
				
								
					
					/*PointerInfo info = MouseInfo.getPointerInfo();
					int mouseX = (int)info.getLocation().getX();
					int mouseY = (int)info.getLocation().getY();*/
					/*Image cursor = ImageIO.read(new File("c:\\Test\\cursor.gif"));  
					int mouseX = MouseInfo.getPointerInfo().getLocation().x;  
					int mouseY = MouseInfo.getPointerInfo().getLocation().y;  
					  
					image.getGraphics().drawImage(cursor, mouseX, mouseY,30,30, null);*/
					
					/*
					byte snappybyte[] = b.get(0);
					starttime = System.nanoTime();
					screenImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR);
					endtime = System.nanoTime() -starttime;					
					System.out.println("bufferedImage  " + endtime);
					
					starttime = System.nanoTime();
					byte[] imageByte = Snappy.uncompress(snappybyte);
					endtime = System.nanoTime() -starttime;					
					System.out.println("uncompress  " + endtime);
					
					starttime = System.nanoTime();
					image.setData(Raster.createRaster(screenImage.getSampleModel(),
							new DataBufferByte(imageByte, imageByte.length), new Point()));					
					endtime = System.nanoTime() -starttime;
					System.out.println("make  " + endtime);
					
					FPScount++;
					index++;
					index = index%buffersize;
					starttime = System.nanoTime();
					repaint();					
					endtime = System.nanoTime() -starttime;
					System.out.println("repaint  " + endtime);
					
					starttime = System.nanoTime();
					b.remove(0);
					endtime = System.nanoTime() -starttime;
					System.out.println("remove  " + endtime);*/										
					//image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(screenWidth, screenHeight, Transparency.TRANSLUCENT);
					
					 image = robot.createScreenCapture(rect);
					 image = getScaledImage(image, 1920, 1080);	
					 starttime = System.nanoTime();
					/* image =  Thumbnails.of(image).outputFormat("jpg").size(1280, 720).asBufferedImage();
					 endtime = System.nanoTime() -starttime;					 
					 System.out.println("remove1  " + endtime);*/
					 
					/* starttime = System.nanoTime();
					 image = Scalr.resize(image,128);
                         
					 endtime = System.nanoTime() -starttime;
						System.out.println("remove2 " + endtime);*/
					 BufferedImage image2 = Thumbnails.of(image).size(1280, 720).outputFormat("jpg").asBufferedImage();
					 byte[] imageByte = ((DataBufferByte) image2.getRaster().getDataBuffer()).getData();
					 System.out.println(imageByte.length);
					
					
					 
					FPScount++;
					repaint();
				} catch (Exception e) {

				}
			}
		}
	}
	public BufferedImage getScaledImage(BufferedImage myImage, int screenWidth, int screenHeight) {
		BufferedImage background = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = background.createGraphics();
		//g.setColor(Color.WHITE);
		g.drawImage(myImage, 0, 0, screenWidth, screenHeight, null);
		g.dispose();
		return background;
	}
	class ImgDoubleBufferTh extends Thread {
		BufferedImage bufferimage;
		public void run() {
			int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
			int screennHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
			BufferedImage screenImage;
			Robot robot = null;
			try {
				robot = new Robot();
			} catch (AWTException e) {
			}

			Rectangle rect = new Rectangle(0, 0, screenWidth, screennHeight);
			int ff=1;
			long starttime,endtime;
			while (true) {				
				starttime = System.nanoTime();
				bufferimage = robot.createScreenCapture(rect);//JNAScreenShot.getScreenshot(rect);//robot.createScreenCapture(rect);
				endtime = System.nanoTime() -starttime;
				System.out.println("capture  " + endtime);
				bufferimage = getScaledImage(bufferimage, 1920, 1080);				
				byte[] imageByte = ((DataBufferByte) bufferimage.getRaster().getDataBuffer()).getData();
				
				try {
					byte[] bbb = Snappy.compress(imageByte);					
					b.addElement(bbb);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			/*	starttime = System.nanoTime();
				bb.addElement(bufferimage);
				endtime = System.nanoTime() -starttime;
				System.out.println("add  " + endtime);	*/			
			
	
			}
			
		}
	}
	class FPSCheckThread extends Thread{
		int sum = 0;
		double avg = 0;
		int roop =0;
		
		public void run(){
			while(true){
				try {
					
					sleep(1000);
					sum+=FPScount;
					roop++;
					avg = (double)sum/roop;
					avg = Double.parseDouble(String.format("%.3f", avg));
					
					FPSlabel.setText("FPS : " + Integer.toString(FPScount) + " " + Double.toString(avg));					
					//repaint();
					System.out.println("FPS : " + FPScount + " " + avg);
					FPScount=0;
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public static void main(String[] args) {	
		new ScreenCapture();
	}
}
