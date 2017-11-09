import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserType;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;

public class Spotlight extends JFrame {
	private ScreenPanel sp;
	Spotlight spotlight = this;
	Stack<Integer> keyStack = new Stack();

	public Spotlight() {
		sp = new ScreenPanel();
		makeTray();
		setTitle("screen");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(null);
		setContentPane(sp);
		setSize(800, 400);
		setVisible(true);

	}
	public void makeTray(){
		URL trayURL = getClass().getClassLoader().getResource("tray.png");	
		ImageIcon trayIcon = new ImageIcon(trayURL);
		MenuItem exititem = new MenuItem("exit");
		PopupMenu menu = new PopupMenu("My Menu");
		menu.add(exititem);
		exititem.addActionListener(new ActionListener(){					
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(1);						
			}
		});
		TrayIcon myTray = new TrayIcon(Toolkit.getDefaultToolkit().getImage(trayURL),"chat",menu);
		SystemTray tray = SystemTray.getSystemTray();
		try {
			tray.add(myTray);
		} catch (AWTException e1) {
			System.out.println(e1.getMessage());
		}
		myTray.setImageAutoSize(true);	
	}

	class ScreenPanel extends JPanel {
		FocusThread th;
		Browser browser;
		BrowserView view;
		TextField field;
		Robot robot;
		Clipboard clipboard;

		public ScreenPanel() {
			setLayout(new BorderLayout());
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			field = new TextField();
			try {
				robot = new Robot();
			} catch (AWTException e2) {
				e2.printStackTrace();
			}
			
			//add(field,BorderLayout.NORTH);
			/*browser = new Browser();
			view = new BrowserView(browser);*/
						
			//add(view,BorderLayout.CENTER);
			//browser.loadURL("http://wwww.naver.com");
			
			spotlight.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					spotlight.setVisible(false);
				}
			});
			spotlight.addWindowListener(new WindowAdapter() {

				@Override
				public void windowOpened(WindowEvent e) {
					spotlight.setAlwaysOnTop(true);
					spotlight.setVisible(false);
					System.out.println("===");

				}

				@Override
				public void windowIconified(WindowEvent e) {
					spotlight.setVisible(false);

				}

				@Override
				public void windowActivated(WindowEvent e) {
					System.out.println("------" + Window.getWindows().length);					
					if (spotlight.isVisible()) {
						th = new FocusThread(spotlight);
						th.start();
					}
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
					th.interrupt();
				}
			});
			GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

				@Override
				public void nativeKeyTyped(NativeKeyEvent e) {
					
				}

				@Override
				public void nativeKeyReleased(NativeKeyEvent e) {
					System.out.println(e.getKeyCode());
					keyStack.push(e.getKeyCode());					
					if (keyStack.size() > 1) {
						int a = keyStack.get(0);
						int b = keyStack.get(1);
						int lc = NativeKeyEvent.VC_CONTROL;
						int space = NativeKeyEvent.VC_SPACE;
						if ((a == lc && b == space) || (a == space && b == lc)) {
							new Thread(){
								public void run(){
									/*StringSelection stringSelection = new StringSelection("");
									Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
									            stringSelection, null);*/
									robot.keyPress(KeyEvent.VK_CONTROL);
									robot.keyPress(KeyEvent.VK_C);
									robot.keyRelease(KeyEvent.VK_CONTROL);
									robot.keyRelease(KeyEvent.VK_C);
									//spotlight.setExtendedState(JFrame.NORMAL);
									//spotlight.setAlwaysOnTop(true);
									//spotlight.setVisible(true);
									
									try {
										sleep(300);										
										Transferable contents = clipboard.getContents(clipboard);							
										if (contents != null && Desktop.isDesktopSupported()) {									
										      String pasteString = (String)(contents.getTransferData(DataFlavor.stringFlavor));
										      pasteString = pasteString.replaceAll("\\+","%2B");
										      pasteString = pasteString.replaceAll(" ","+");
										      pasteString = pasteString.replaceAll("https://","");
										      pasteString = pasteString.replaceAll("http://","");
										      pasteString = pasteString.replaceAll("&","%26");
										      pasteString = pasteString.replaceAll("/","%2F");										      
										      pasteString = pasteString.replaceAll("=","%3D");										      										      
										      pasteString = pasteString.replaceAll("\"","");
										      System.out.println(pasteString);
										      
										      String URL = "http://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + pasteString + "";								      
										      Desktop.getDesktop().browse(new URI(URL));
										}																
									} catch (Exception e1) {

										e1.printStackTrace();
									} 
									// spotlight.toFront();
								}
							}.start();

						}
						keyStack.clear();
					}
				}

				@Override
				public void nativeKeyPressed(NativeKeyEvent e) {}
			});
			GlobalScreen.addNativeMouseListener(new NativeMouseListener() {

				@Override
				public void nativeMouseReleased(NativeMouseEvent arg0) {
					

				}

				@Override
				public void nativeMousePressed(NativeMouseEvent arg0) {
					

				}

				@Override
				public void nativeMouseClicked(NativeMouseEvent arg0) {
					

				}
			});
			GlobalScreen.addNativeMouseMotionListener(new NativeMouseMotionListener() {

				@Override
				public void nativeMouseMoved(NativeMouseEvent e) {
					

				}

				@Override
				public void nativeMouseDragged(NativeMouseEvent e) {

				}
			});

		}
	}

	class FocusThread extends Thread {
		Spotlight spotlight;

		public FocusThread(Spotlight spotlight) {
			this.spotlight = spotlight;
		}

		public void run() {
			int count = 0;
			while (true) {
				try {
					for (int i = 0; i < Window.getWindows().length; i++) {
						spotlight.toFront();
					}
					sleep(100);
					if (count == 15)// 1.4s
						break;
					count++;
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			if (!GlobalScreen.isNativeHookRegistered()) {
				GlobalScreen.registerNativeHook();
				Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
				logger.setLevel(Level.OFF);
			}

		} catch (NativeHookException e) {

		}
		new Spotlight();
	}
}
