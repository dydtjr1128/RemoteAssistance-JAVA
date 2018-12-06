import com.sun.jna.Library;
import com.sun.jna.Native;
import org.xerial.snappy.Snappy;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Vector;


public class NetworkScreenServer extends JFrame {
	private final static int SERVER_PORT = 9999;
	private final static int SERVER_CURSOR_PORT = SERVER_PORT - 1;
	private final static int SERVER_KEBOARD_PORT = SERVER_PORT - 2;
	private DataOutputStream dataOutputStream;
	private ObjectOutputStream objectOutputStream;
	private Image cursor;
	private String myFont = "�������";
	private BufferedImage screenImage;
	private Rectangle rect;
	private MainPanel mainPanel = new MainPanel();
	private ServerSocket imageSeverSocket = null;
	private ServerSocket cursorServerSocket = null;
	private ServerSocket keyboardServerSocket = null;
	private Socket imageSocket = null;
	private Socket cursorSocket = null;
	private Socket keyboardSocket = null;
	private Robot robot;
	private int screenWidth, screenHeight;
	private Boolean isRunning = false;
	private Thread imgThread;
	private static int new_Width = 1920;
	private static int new_Height = 1080;
	private JButton startBtn;
	private JButton stopBtn;
	private JTextField widthTextfield;
	private JTextField heightTextfield;
	private JRadioButton compressTrueRBtn;
	private JRadioButton compressFalseRBtn;
	private JLabel widthLabel;
	private JLabel heightLabel;
	private JLabel compressLabel;
	private URL cursorURL = getClass().getClassLoader().getResource("cursor.gif");
	private Boolean isCompress = true;
	private JFrame fff = this;
	private final int MOUSE_MOVE = 1;
	private final int MOUSE_PRESSD = 2;
	private final int MOUSE_RELEASED = 3;
	private final int MOUSE_DOWN_WHEEL = 4;
	private final int MOUSE_UP_WHEEL = 5;
	private final int KEY_PRESSED = 6;
	private final int KEY_RELEASED = 7;
	private final int KEY_CHANGE_LANGUAGE = 8;
	int count = 0, count2 = 0;
	private User32jna u32 = User32jna.INSTANCE;
	private int buffersize = 1;
	private BufferedImage[] img = new BufferedImage[buffersize];
	private Vector<byte[]> imgvec = new Vector<>();

	public NetworkScreenServer() {
		setTitle("원격지원 대상 컴퓨터용");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setContentPane(mainPanel);
		setSize(490, 160);
		setVisible(true);
		setResizable(false);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				System.out.println("type" + e.getKeyCode() + "  " + e.getKeyChar() + "  " + e.getID() + "  "
						+ e.getModifiers() + "  " + e.getKeyLocation() + "  " + e.getExtendedKeyCode());
			}

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("pressed" + e.getKeyCode() + "  " + e.getKeyChar() + "  " + e.getID() + "  "
						+ e.getModifiers() + "  " + e.getKeyLocation() + "  " + e.getExtendedKeyCode());
				super.keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("released" + e.getKeyCode() + "  " + e.getKeyChar() + "  " + e.getID() + "  "
						+ e.getModifiers() + "  " + e.getKeyLocation() + "  " + e.getExtendedKeyCode());
				if (e.getKeyCode() == 0) {
					if (count >= 1) {
						count = 0;
						return;
					}
					// System.out.println(t.getLocale().toString() + " " +
					// t.getLocale().getCountry() + " " +
					// t.getLocale().getDisplayCountry());
					System.out.println("ee");
					count = 1;
					u32.keybd_event((byte) 0x15, (byte) 0, 0, 0);// ����ffDDDddSS
					u32.keybd_event((byte) 0x15, (byte) 00, (byte) 0x0002, 0);// ����
																				// ����
				}
			}
		});
		widthTextfield.requestFocus();
	}

	public interface User32jna extends Library {
		User32jna INSTANCE = (User32jna) Native.loadLibrary("user32.dll", User32jna.class);

		// User32jna INSTANCE = (User32jna)
		// Native.loadLibrary("user32.dll",User32jna.class);
		public void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
	}

	/*
	 * User32jna u32 = User32jna.INSTANCE;
	 */

	class MainPanel extends JPanel implements Runnable {

		public MainPanel() {
			setLayout(null);

			startBtn = new JButton("Start");
			stopBtn = new JButton("Stop");
			widthTextfield = new JTextField(Integer.toString(new_Width), 5);
			heightTextfield = new JTextField(Integer.toString(new_Height), 5);
			widthLabel = new JLabel("width");
			heightLabel = new JLabel("height");
			compressLabel = new JLabel("<html>&nbsp&nbsp&nbsp<span>Image<br>Compress</span></html>");
			compressTrueRBtn = new JRadioButton("True");
			compressFalseRBtn = new JRadioButton("False");

			startBtn.setBounds(0, 0, 150, 130);
			stopBtn.setBounds(150, 0, 150, 130);
			widthLabel.setBounds(327, 8, 50, 15);
			widthTextfield.setBounds(300, 30, 90, 35);
			heightLabel.setBounds(325, 70, 50, 15);
			heightTextfield.setBounds(300, 90, 90, 35);
			compressLabel.setBounds(405, -10, 100, 50);
			compressTrueRBtn.setBounds(390, 30, 80, 30);
			compressFalseRBtn.setBounds(390, 90, 80, 30);

			ButtonGroup group = new ButtonGroup();
			group.add(compressTrueRBtn);
			group.add(compressFalseRBtn);

			widthLabel.setFont(new Font(myFont, Font.PLAIN, 15));
			heightLabel.setFont(new Font(myFont, Font.PLAIN, 15));

			compressLabel.setFont(new Font(myFont, Font.PLAIN, 10));
			startBtn.setFont(new Font(myFont, Font.PLAIN, 20));
			stopBtn.setFont(new Font(myFont, Font.PLAIN, 20));
			compressTrueRBtn.setFont(new Font(myFont, Font.PLAIN, 20));
			compressFalseRBtn.setFont(new Font(myFont, Font.PLAIN, 20));

			compressTrueRBtn.setSelected(true);

			add(startBtn);
			add(stopBtn);
			add(widthLabel);
			add(widthTextfield);
			add(heightLabel);
			add(heightTextfield);
			add(compressLabel);
			add(compressTrueRBtn);
			add(compressFalseRBtn);
			stopBtn.setEnabled(false);
			startBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (isRunning)
						return;
					try {
						new_Height = Integer.parseInt(heightTextfield.getText());
						new_Width = Integer.parseInt(widthTextfield.getText());
					} catch (Exception e1) {
						return;
					}
					heightTextfield.setEditable(false);
					widthTextfield.setEditable(false);
					isRunning = true;
					startBtn.setEnabled(false);
					stopBtn.setEnabled(true);
					if (compressTrueRBtn.isSelected()) {
						isCompress = true;
					} else if (compressFalseRBtn.isSelected()) {
						isCompress = false;
					}
					compressTrueRBtn.setEnabled(false);
					compressFalseRBtn.setEnabled(false);

					imgThread = new Thread(mainPanel);
					CursorThread cursorThread = new CursorThread();
					KeyBoardThread keyBoardThread = new KeyBoardThread();
					imgThread.start();
					cursorThread.start();
					keyBoardThread.start();

				}
			});
			stopBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!isRunning)
						return;
					heightTextfield.setEditable(true);
					widthTextfield.setEditable(true);
					isRunning = false;
					ServerSocketCloseThread closeThread = new ServerSocketCloseThread();
					closeThread.start();
					// imgThread.interrupt();
					stopBtn.setEnabled(false);
					startBtn.setEnabled(true);
					compressTrueRBtn.setEnabled(true);
					compressFalseRBtn.setEnabled(true);

				}
			});
			widthTextfield.transferFocus();
		}

		public void run() {

			try {
				robot = new Robot();
				screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
				screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
				rect = new Rectangle(0, 0, screenWidth, screenHeight);
				System.out.println("��������");
				imageSeverSocket = new ServerSocket(SERVER_PORT);// ImageSERVER

				imageSocket = imageSeverSocket.accept();
				imageSocket.setTcpNoDelay(true);
				dataOutputStream = new DataOutputStream(imageSocket.getOutputStream());
				objectOutputStream = new ObjectOutputStream(imageSocket.getOutputStream());
				dataOutputStream.writeInt(screenWidth);
				dataOutputStream.writeInt(screenHeight);
				dataOutputStream.writeInt(new_Width);
				dataOutputStream.writeInt(new_Height);
				dataOutputStream.writeBoolean(isCompress);
				cursor = ImageIO.read(cursorURL);
			} catch (Exception e) {

			}
			ImgDoubleBufferTh th = new ImgDoubleBufferTh();
			th.start();					
			
			//new ImgDoubleBufferTh().start();
			
			// ImageIO.setUseCache(false);// little more spped

			// imgvec.add(getScaledImage(robot.createScreenCapture(rectangle),screenWidth,screenHeight,BufferedImage.TYPE_3BYTE_BGR));
			// Image cursor = ImageIO.read(new
			// File("c:\\Test\\cursor.gif"));

			// long starttime,estimatedTime;
			int index = 0;
			Runtime runtime = Runtime.getRuntime();
			while (isRunning) {
				try {
					
					// screenImage =
					// JNAScreenShot.getScreenshot(rectangle);//robot.createScreenCapture(rectangle);
					// if (img[index] != null) {
					// screenImage = img[index];//
					// robot.createScreenCapture(rectangle);
					
					//screenImage =imgvec.get(0); //robot.createScreenCapture(rect);//					
					byte[] imageByte = imgvec.get(0);
					if(imgvec.size() == 3){
						synchronized (th) {
							th.notify();
						}						
					}
					
					

					/*
					 * int mouseX = MouseInfo.getPointerInfo().getLocation().x;
					 * int mouseY = MouseInfo.getPointerInfo().getLocation().y;
					 * 
					 * screenImage.getGraphics().drawImage(cursor, mouseX,
					 * mouseY, 30, 30, null);
					 */

					
					

					if (isCompress) {

						//byte[] compressImageByte = compress(imageByte);// 6MB->480KB����						
						// System.out.println("compress : " +
						// (double)compressImageByte.length/1024 + "kb");
						dataOutputStream.writeInt(imageByte.length);
						dataOutputStream.write(imageByte);

						// System.out.println(imageByte.length);
						dataOutputStream.flush();
					} else {
						dataOutputStream.writeInt(imageByte.length);
						dataOutputStream.write(imageByte);
						// System.out.println(imageByte.length);

						dataOutputStream.flush();
					}
					//}
				} catch (Exception e) {

				}
				if (runtime.totalMemory() / 1024 / 1024 > 500)
					System.gc();
				if (imgvec.size() > 1) {
					/*		new Thread(){
								public void run(){						*/	
									//System.out.println(imgvec.size());
									imgvec.remove(0);
									index++;								
									if(index == 30){
										System.out.println("������");
										index=0;
										System.gc();
									}
						/*		}						
							}.start();*/
						}
								

				// Thread.sleep(1000);
			}

		}

	}

	class ImgDoubleBufferTh extends Thread {
		BufferedImage bufferimage;
		Robot robot = null;
		
		synchronized public void run() {			
			try {
				robot = new Robot();
			} catch (AWTException e) {
			}			
			while (true) {

				bufferimage = robot.createScreenCapture(rect);
				bufferimage = getScaledImage(bufferimage, new_Width, new_Height, BufferedImage.TYPE_3BYTE_BGR);
				byte[] imageByte = ((DataBufferByte) bufferimage.getRaster().getDataBuffer()).getData();
				try {
					imgvec.addElement(compress(imageByte));
				} catch (IOException e) {
					e.printStackTrace();
				}
				//System.out.println(imgvec.size());
				if(imgvec.size()>5)
					try {
						System.out.println("wait");
						wait();
					} catch (InterruptedException e) {
						
					}				
			}

		}
	}

	public static byte[] compress(byte[] data) throws IOException {
		byte[] output = Snappy.compress(data);

		return output;
	}

	public BufferedImage getScaledImage(BufferedImage myImage, int width, int height, int type) {
		BufferedImage background = new BufferedImage(width, height, type);
		Graphics2D g = background.createGraphics();
		g.setColor(Color.WHITE);
		g.drawImage(myImage, 0, 0, width, height, null);
		g.dispose();
		return background;
	}

	class ServerSocketCloseThread extends Thread {
		public void run() {
			if (!imageSeverSocket.isClosed() || !cursorServerSocket.isClosed() || keyboardServerSocket.isClosed()) {
				try {
					imageSeverSocket.close();
					cursorServerSocket.close();
					keyboardServerSocket.close();
				} catch (IOException e) {
					DebugMessage.printDebugMessage(e);
				}
			}
		}
	}

	class KeyBoardThread extends Thread {
		public void run() {
			try {
				keyboardServerSocket = new ServerSocket(SERVER_KEBOARD_PORT);
				keyboardSocket = keyboardServerSocket.accept();
				DataInputStream dataInputStream = new DataInputStream(keyboardSocket.getInputStream());
				while (true) {
					int keyboardState = dataInputStream.readInt();
					if (keyboardState == KEY_PRESSED) {// KEYBOARD PRESSED
						int keyCode = dataInputStream.readInt();
						// System.out.println(keyCode + "����");
						u32.keybd_event((byte) keyCode, (byte) 0, 0, 0);// ����ffDDDddSS
						// robot.keyPress(keyCode);
					} else if (keyboardState == KEY_RELEASED) {
						int keyCode = dataInputStream.readInt();
						// System.out.println(keyCode + "����");
						u32.keybd_event((byte) keyCode, (byte) 00, (byte) 0x0002, 0);// ��
						// robot.keyRelease(keyCode);
					}
					yield();
				}
			} catch (Exception e) {

			}
		}
	}

	class CursorThread extends Thread {
		public void run() {
			try {
				System.out.println("�ѱ�");
				cursorServerSocket = new ServerSocket(SERVER_CURSOR_PORT);// cursorSERVER
				cursorSocket = cursorServerSocket.accept();
				System.out.println("Ŀ�� ���� ���Դ�");
				DataInputStream dataInputStream = new DataInputStream(cursorSocket.getInputStream());
				int mouseX = 0;
				int mouseY = 0;
				while (isRunning) {
					int mouseState = dataInputStream.readInt();// mouse,Keyboard
																// state
					if (mouseState == MOUSE_MOVE) {// move
						mouseX = dataInputStream.readInt();
						mouseY = dataInputStream.readInt();

						robot.mouseMove(mouseX, mouseY);
					} else if (mouseState == MOUSE_PRESSD) { // pressed
						int mouseButton = dataInputStream.readInt();
						robot.mouseMove(mouseX, mouseY);
						if (mouseButton == 1) {
							robot.mousePress(MouseEvent.BUTTON1_MASK);
						} else if (mouseButton == 2) {
							robot.mousePress(MouseEvent.BUTTON2_MASK);
						} else if (mouseButton == 3) {
							robot.mousePress(MouseEvent.BUTTON3_MASK);
						}
					} else if (mouseState == MOUSE_RELEASED) {// released
						int mouseButton = dataInputStream.readInt();
						robot.mouseMove(mouseX, mouseY);
						if (mouseButton == 1) {
							robot.mouseRelease(MouseEvent.BUTTON1_MASK);
						} else if (mouseButton == 2) {
							robot.mouseRelease(MouseEvent.BUTTON2_MASK);
						} else if (mouseButton == 3) {
							robot.mouseRelease(MouseEvent.BUTTON3_MASK);
						}
					} else if (mouseState == MOUSE_DOWN_WHEEL) {// MOUSE DOWN
																// WHEEL
						robot.mouseWheel(-3);
					} else if (mouseState == MOUSE_UP_WHEEL) {// MOUSE UP WHEEL
						robot.mouseWheel(3);
					}
					yield();
				}

			} catch (Exception e) {

			}

		}
	}

	public static void main(String[] args) {
		new NetworkScreenServer();
	}

}
