package dydtjr1128;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.swing.*;
import java.awt.*;
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
    private JLabel FPSlabel;
    private int FPScount = 0;
    private int screenWidth, screenHeight;
    int buffersize = 1;
    byte imgbyte[][] = new byte[buffersize][];
    SynchronousQueue<byte [] > imageBlockingQueue2 = new SynchronousQueue<>();
    BlockingQueue<byte[]> imageBlockingQueue = new ArrayBlockingQueue<>(3);

    public ScreenCapture() {
        setTitle("screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        //this.setAlwaysOnTop(true);
        ScreenPanel sp = new ScreenPanel();
        setContentPane(sp);
        setSize(800, 800);
        setVisible(true);

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
            FPSlabel.setFont(new Font("�������", Font.BOLD, 20));
            FPSlabel.setBounds(10, 10, 100, 50);
            add(FPSlabel);
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
            image = robot.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            try {
                FPScount++;
                BufferedImage ii = toBufferedImage(imageBlockingQueue2.take());
                g.drawImage(ii, 0, 0, getWidth(), getHeight(), this);
                //byte[] imageBytes = ((DataBufferByte) ii.getData().getDataBuffer()).getData();
                //System.out.println(imageBytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        /*private BufferedImage screenShot(){
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
        }*/
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
                    imageBlockingQueue2.add(toByteArray(image));
                    avg += 1/((System.currentTimeMillis() - s) / (double) 1000);
                    System.out.println(imageBlockingQueue2.size() + " " + avg / count++);
                    repaint();

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

    /*class ImgDoubleBufferTh extends Thread {
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
            int ff = 1;
            long starttime, endtime;
            while (true) {
                starttime = System.nanoTime();
                bufferimage = robot.createScreenCapture(rect);//JNAScreenShot.getScreenshot(rect);//robot.createScreenCapture(rect);
                endtime = System.nanoTime() - starttime;
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
			*//*	starttime = System.nanoTime();
				bb.addElement(bufferimage);
				endtime = System.nanoTime() -starttime;
				System.out.println("add  " + endtime);	*//*


            }

        }
    }*/

    class FPSCheckThread extends Thread {
        int sum = 0;
        double avg = 0;
        int roop = 0;

        public void run() {
            while (true) {
                try {

                    sleep(1000);
                    sum += FPScount;
                    roop++;
                    avg = (double) sum / roop;
                    avg = Double.parseDouble(String.format("%.3f", avg));

                    FPSlabel.setText("FPS : " + Integer.toString(FPScount) + " " + Double.toString(avg));
                    //repaint();
                    System.out.println("FPS : " + FPScount + " " + avg);

                    FPScount = 0;

                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void main(String[] args) {
        new ScreenCapture();
    }
}
