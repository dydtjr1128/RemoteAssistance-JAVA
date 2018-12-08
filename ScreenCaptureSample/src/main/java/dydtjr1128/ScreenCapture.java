package dydtjr1128;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;


public class ScreenCapture extends JFrame {
    private double FPScount = 0;
    private int screenWidth, screenHeight;
    int buffersize = 1;
    byte imgbyte[][] = new byte[buffersize][];
    BufferedImage prevImg;
    BufferedImage img;
    SynchronousQueue<byte[]> imageBlockingQueue2 = new SynchronousQueue<>();
    BlockingQueue<byte[]> imageBlockingQueue = new ArrayBlockingQueue<>(3);

    public ScreenCapture() {
        setTitle("screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        setLayout(null);
        //this.setAlwaysOnTop(true);
        ScreenPanel sp = new ScreenPanel();
        setContentPane(sp);
        setSize(800, 800);
        setVisible(true);

        Thread th = new Thread(sp);
        th.setPriority(Thread.MAX_PRIORITY);
        th.start();
    }

    class ScreenPanel extends JPanel implements Runnable {
        BufferedImage image;
        int screenWidth, screenHeight;
        Robot robot = null;

        public ScreenPanel() {
            screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            addMouseListener(new MouseListener() {
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
            });
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            javax.swing.Timer timer = new javax.swing.Timer(1000 / 60, e -> {
                //System.out.println("@@@@@@@@@@@@@@");
                repaint();
            });
            timer.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            img = toBufferedImage(imageBlockingQueue2.take());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            //if (prevImg != img) {

                //스크린X : 스크린Y = 화면X : 화면 Y
                //getWidth()*screenHeight/screenWidth = y;
                //System.out.println(" " + getWidth() + " " + screenHeight);
                int panelWidth = getWidth();
                int height = panelWidth * screenHeight / screenWidth;
                g.drawImage(img, 0, (getHeight() / 2) - (height / 2), panelWidth, height, this);
                g.setFont(new Font("한컴 윤고딕 250", Font.BOLD, 15));
                g.drawString(("FPS : " + String.format("%.2f",FPScount)), panelWidth / 2 - 10, 30);
                prevImg = img;
            //}
            //g.drawOval(100,100,300,300);
        }

        public void run() {
            Rectangle rect = new Rectangle(0, 0, screenWidth, screenHeight);
            double avg = 0;
            int count = 0;
            while (true) {
                try {

                    long s = System.currentTimeMillis();
                    //image = JNAScreenShot2.getScreenshot(rect);
                    image = robot.createScreenCapture(rect);
                    //image = getScaledImage(image, 1920, 1080);

                    //9~11frame
                    imageBlockingQueue2.put(toByteArray(image));
                    avg += 1 / ((System.currentTimeMillis() - s) / (double) 1000);
                    FPScount = avg / count++;
                    //System.out.println(imageBlockingQueue2.size() + " " + avg / count++);
                    //repaint();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] toByteArray(BufferedImage image) throws IOException {//ImageIO보다 빠름
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
        encoder.encode(image);
        return baos.toByteArray();
    }

    public BufferedImage toBufferedImage(byte[] image) throws IOException {//ImageIO보다 빠름
        ByteArrayInputStream bais = new ByteArrayInputStream(image);
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(bais);

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

    public static void main(String[] args) {
        System.setProperty("sun.awt.noerasebackground", "true");
        new ScreenCapture();
    }
}
