import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class HelloWorld extends JFrame{
	BufferedImage image;
	// The window handle
	private long window;
	public HelloWorld() {
		setTitle("screen");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		//this.setAlwaysOnTop(true);
		mp m = new mp();
		setContentPane(m);
		new Thread(){
			public void run(){
				
			}
		}.start();
		setSize(800, 800);
		setVisible(true);
	}
	class mp extends JPanel{
		@Override
		protected void paintComponent(Graphics g) {			
			super.paintComponent(g);
			g.drawImage(image, 0, 0, getWidth(), getHeight(), this);		
			
		
		}
	}
	
	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");
		//image = new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR);
		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
/*	private BufferedImage screenShot2(){
	     IntBuffer buff = ByteBuffer.allocateDirect(1920 * 1080 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
         int buffh = ByteBuffergetDirectBufferAddress(buff) ;
         GL11.glReadPixels(0, 0, 1920, 1080, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffh);
         BufferedImage img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);

         for( int ix=0;ix<1920;ix++)
               for( int iy=0;iy<1080;iy++)
               {
                     img.setRGB(ix,iy,buff.get((1080-iy-1)*1920+ix));
               }
         
	}*/
	private BufferedImage screenShot(){
		int screenWidth = 300;
		int screenHeight = 300;
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
/*	private BufferedImage screenshot(){
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );
		for(int x = 0; x < width; x++) 
		{
		    for(int y = 0; y < height; y++)
		    {
		        int i = (x + (width * y)) * bpp;
		        int r = buffer.get(i) & 0xFF;
		        int g = buffer.get(i + 1) & 0xFF;
		        int b = buffer.get(i + 2) & 0xFF;
		        image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
		    }
		}
		  return image;
	}*/
	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}
	
	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			glfwSwapBuffers(window); // swap the color buffers
			image = screenShot();
			
			repaint();
			System.out.println("ee");
			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	public static void main(String[] args) {
		new HelloWorld().run();
	}

}