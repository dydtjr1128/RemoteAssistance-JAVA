package dydtjr1128;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;


public class ScreenCapture extends JFrame {
    private double FPScount = 0;
    private double FPS = 0;
    private String FPSLabel = "FPS : 0";
    private ByteBuffer img;
    private static BufferedImage image;
    private SynchronousQueue<ByteBuffer> imageBlockingQueue2 = new SynchronousQueue<>();
    private BlockingQueue<byte[]> imageBlockingQueue = new ArrayBlockingQueue<>(3);
    private StringBuilder builder = new StringBuilder();
    private final Font myFont = new Font("한컴 윤고딕 250", Font.BOLD, 15);
    ScreenPanel sp;

    public ScreenCapture() throws AWTException {

        setTitle("screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        setLayout(null);
        //this.setAlwaysOnTop(true);
        sp = new ScreenPanel();
        setContentPane(sp);
        setSize(800, 800);
        setVisible(true);

        for (int i = 0; i < 1; i++) {
            ImageCaptureThread th = new ImageCaptureThread();
            //th.setPriority(Thread.MAX_PRIORITY);
            th.start();
        }
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FPS = FPScount;
                FPSLabel = builder.append("FPS : ").append(String.format("%.2f", FPS)).toString();
                builder.setLength(0);
                FPScount = 0;

                /*long total = Runtime.getRuntime().totalMemory();
                long free = Runtime.getRuntime().freeMemory();
                int n = 1024 * 1024;
                System.out.println("Used Mem : " + (total - free) / n + "MB");
                System.out.println("Free Mem : " + (free) / n + "MB");
                System.out.println("Total Mem : " + (total) / n + "MB");
                System.out.println("Max Mem : " + (Runtime.getRuntime().maxMemory()) / n + "MB");
                System.out.println("===========================");*/
            }
        });
        timer.start();

    }

    class ImageCaptureThread extends Thread {
        private final Rectangle rect;
        private final Robot robot = new Robot();
        private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024 * 1);//쓰레드마다 1MB

        public ImageCaptureThread() throws AWTException {
            rect = new Rectangle(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width,
                    Toolkit.getDefaultToolkit().getScreenSize().height);
        }

        public void run() {
            while (true) {
                try {
                    BufferedImage ii = robot.createScreenCapture(rect);
                    buffer.put(toByteArray(ii));
                    imageBlockingQueue2.put(buffer);
                    sp.repaint();
                    ii = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ScreenPanel extends JPanel {
        private int screenWidth, screenHeight;
        private Robot robot = null;
        private int panelWidth;
        private int height;

        public ScreenPanel() {
            screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            /*addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1 && e.getButton() == e.BUTTON3) {
                        //System.out.println("4444");
                        robot.mouseMove(e.getX() * screenWidth / getWidth(), e.getY() * screenHeight / getHeight());
                        robot.mousePress(MouseEvent.BUTTON3_MASK);
                        robot.mouseRelease(MouseEvent.BUTTON3_MASK);
                    } else if (e.getClickCount() == 2) {
                        //mx : wx = getwidth : winwidth
                        robot.mouseMove(e.getX() * screenWidth / getWidth(), e.getY() * screenHeight / getHeight());
                        robot.mousePress(MouseEvent.BUTTON1_MASK);
                        robot.mouseRelease(MouseEvent.BUTTON1_MASK);
                        robot.mousePress(MouseEvent.BUTTON1_MASK);
                        robot.mouseRelease(MouseEvent.BUTTON1_MASK);
                    }
                }

                public void mousePressed(MouseEvent e) {

                }

                public void mouseReleased(MouseEvent e) {

                }

                public void mouseEntered(MouseEvent e) {

                }

                public void mouseExited(MouseEvent e) {

                }
            });*/
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            /*javax.swing.Timer timer = new javax.swing.Timer(1000 / 60, e -> {
                //System.out.println("@@@@@@@@@@@@@@");
                repaint();
            });
            timer.start();*/

        }

        @Override
        protected void paintComponent(Graphics g) {
            //super.paintComponent(g);
            g.clearRect(0, 0, getWidth(), getHeight());
            //스크린X : 스크린Y = 화면X : 화면 Y
            //getWidth()*screenHeight/screenWidth = y;
            //System.out.println(" " + getWidth() + " " + screenHeight);


            //g.drawRect(10,10,200,200);
            try {
                img = imageBlockingQueue2.take();
                image = toBufferedImage(img);
                panelWidth = getWidth();
                height = panelWidth * screenHeight / screenWidth;
                g.drawImage(image, 0, (getHeight() / 2) - (height / 2), panelWidth, height, this);
                g.setFont(myFont);
                g.drawString(FPSLabel, panelWidth / 2 - 10, 30);
                FPScount++;
                image = null;
                img.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public byte[] toByteArray(BufferedImage image) throws IOException {//ImageIO보다 빠름
        //long l = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
        encoder.encode(image);
        //System.out.println((System.currentTimeMillis()-l)/(double)1000+"s");
        return baos.toByteArray();
    }

    public byte[] toByteArray2(BufferedImage image) throws IOException {//ImageIO보다 빠름
        //long l = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        //System.out.println((System.currentTimeMillis()-l)/(double)1000+"s");
        return baos.toByteArray();
    }

    public BufferedImage toBufferedImage(byte[] image) throws IOException {//ImageIO보다 빠름
        ByteArrayInputStream bais = new ByteArrayInputStream(image);
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(bais);

        return decoder.decodeAsBufferedImage();
    }

    public BufferedImage toBufferedImage(ByteBuffer image) throws IOException {//ImageIO보다 빠름

        byte b[] = new byte[image.position()];
        image.flip();
        image.get(b);
        /*for(int i=0; i<(b.length>100?100:b.length); i++)
            System.out.print(b[i]);*/
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(bais);
        b = null;
        return decoder.decodeAsBufferedImage();
    }

    public BufferedImage getScaledImage(BufferedImage myImage, int screenWidth, int screenHeight) {
        BufferedImage background = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = background.createGraphics();
        //g.setColor(Color.WHITE);
        g.drawImage(myImage, 0, 0, screenWidth, screenHeight, null);
        g.dispose();
        return background;
    }

    public static void main(String[] args) throws AWTException {

        System.setProperty("sun.awt.noerasebackground", "true");
        new ScreenCapture();
    }
}
