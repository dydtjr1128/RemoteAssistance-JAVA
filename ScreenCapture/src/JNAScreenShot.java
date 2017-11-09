import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.KeyboardUtils;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.win32.W32APIOptions;

public class JNAScreenShot {

	public static BufferedImage getScreenshot(Rectangle bounds) {

		HDC windowDC = GDI.GetDC(USER.GetDesktopWindow());
		HBITMAP outputBitmap = GDI.CreateCompatibleBitmap(windowDC, bounds.width, bounds.height);
		try {
			HDC blitDC = GDI.CreateCompatibleDC(windowDC);
			try {
				HANDLE oldBitmap = GDI.SelectObject(blitDC, outputBitmap);
				try {
					GDI.BitBlt(blitDC, 0, 0, bounds.width, bounds.height, windowDC, bounds.x, bounds.y, GDI32.SRCCOPY);
					//SRCCOPY는 원본그림 그대로 복사해오는 것 설정
					} finally {
					GDI.SelectObject(blitDC, oldBitmap);
				}
				BITMAPINFO bi = new BITMAPINFO(40);				
				bi.bmiHeader.biSize = 40;
						
				
				boolean ok = GDI.GetDIBits(blitDC, outputBitmap, 0, bounds.height, (byte[]) null, bi, 0x00);								 

				
				if (ok) {								
					System.out.println("11111");
					BITMAPINFOHEADER bih = bi.bmiHeader;
					System.out.println("22222");
					bih.biHeight = -Math.abs(bih.biHeight);
					System.out.println("33333");
					bi.bmiHeader.biCompression = 0;					
					return bufferedImageFromBitmap(blitDC, outputBitmap, bi);
				} else {
					return null;
				}
			} finally {
				GDI.DeleteObject(blitDC);
			}
		} finally {
			GDI.DeleteObject(outputBitmap);
		}
	}
	HBITMAP ScreenCapture(HWND hWnd)
	{
	  int ScreenWidth = 1920;
	  int ScreenHeight = 1080;
	  HDC hScrDC, hMemDC;
	  HBITMAP hBitmap;	 
	  
	  HDC windowDC = GDI.GetDC(USER.GetDesktopWindow());
	  hMemDC =  GDI.CreateCompatibleDC(windowDC);
	  hBitmap = GDI.CreateCompatibleBitmap(windowDC, ScreenWidth, ScreenHeight);
	  GDI.SelectObject(hMemDC, hBitmap);
	 
	  GDI.BitBlt(hMemDC, 0, 0, ScreenWidth, ScreenHeight, windowDC, 0, 0, GDI.SRCCOPY);
	 
	  GDI.DeleteDC(hMemDC);
	  GDI.DeleteDC(windowDC);	 
	  return hBitmap;
	}
	
	private static BufferedImage bufferedImageFromBitmap(HDC blitDC, HBITMAP outputBitmap, BITMAPINFO bi) {
		BITMAPINFOHEADER bih = bi.bmiHeader;		
		int height = Math.abs(bih.biHeight);
		final ColorModel cm;
		final DataBuffer buffer;
		final WritableRaster raster;
		int strideBits = (bih.biWidth * bih.biBitCount);
		int strideBytesAligned = (((strideBits - 1) | 0x1F) + 1) >> 3;//8배줄임
		final int strideElementsAligned;		
		System.out.println(strideBits + " " + strideBytesAligned + "  " + bih.biBitCount);
		bi.bmiHeader.biPlanes=1;				
		bi.bmiHeader.biBitCount=24;
		bi.bmiHeader.biSizeImage =  ( ( bi.bmiHeader.biWidth * bi.bmiHeader.biBitCount + 31 ) & ~31 ) / 8 * bi.bmiHeader.biHeight;
		bi.bmiHeader.biCompression = 0;		
		switch (bih.biBitCount) {
		case 16:
			strideElementsAligned = strideBytesAligned / 2;
			cm = new DirectColorModel(16, 0x7C00, 0x3E0, 0x1F);
			buffer = new DataBufferUShort(strideElementsAligned * height);
			raster = Raster.createPackedRaster(buffer, bih.biWidth, height, strideElementsAligned,
					((DirectColorModel) cm).getMasks(), null);
			break;
		case 24:
			strideElementsAligned = strideBytesAligned/3 ;
			//image.setData(Raster.createRaster(screenImage.getSampleModel(),
				//new DataBufferByte(imageByte, imageByte.length), new Point()));
			System.out.println("-------------------------");
			cm = new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);
			buffer = new DataBufferByte(strideElementsAligned * height);//DataBufferInt(strideElementsAligned * height);
			System.out.println(bih.biWidth + " " + bih.biHeight + " " + strideElementsAligned);
			raster = Raster.createPackedRaster(buffer, bih.biWidth, height, strideElementsAligned,
					((DirectColorModel) cm).getMasks(), null);
			System.out.println("rrrrrrrrrrrrrrrrrrrrrrrrrrr");
			break;
		case 32:
			strideElementsAligned = strideBytesAligned/4 ;
			//image.setData(Raster.createRaster(screenImage.getSampleModel(),
				//new DataBufferByte(imageByte, imageByte.length), new Point()));			
			cm = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF);
			buffer = new DataBufferInt(strideElementsAligned * height);
			System.out.println(bih.biWidth + " " + bih.biHeight + " " + strideElementsAligned);
			raster = Raster.createPackedRaster(buffer, bih.biWidth, height, strideElementsAligned,
					((DirectColorModel) cm).getMasks(), null);
			break;
		default:
			throw new IllegalArgumentException("Unsupported bit count: " + bih.biBitCount);
		}
		boolean ok;
		System.out.println(buffer.getDataType());
		switch (buffer.getDataType()) {	
		case DataBuffer.TYPE_BYTE: {
			byte[] pixels = ((DataBufferByte) buffer).getData();
			ok = GDI.GetDIBits(blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0);
			break;
		}
		case DataBuffer.TYPE_INT: {
			int[] pixels = ((DataBufferInt) buffer).getData();
			ok = GDI.GetDIBits(blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0);
		}
			break;
		case DataBuffer.TYPE_USHORT: {
			short[] pixels = ((DataBufferUShort) buffer).getData();
			ok = GDI.GetDIBits(blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0);
		}
			break;
		default:
			throw new AssertionError("Unexpected buffer element type: " + buffer.getDataType());
		}
		if (ok) {
			return new BufferedImage(cm, raster, false, null);
		} else {
			return null;
		}
	}

	private static final User32 USER = User32.INSTANCE;

	private static final GDI32 GDI = GDI32.INSTANCE;

}

interface GDI32 extends com.sun.jna.platform.win32.GDI32 {
	GDI32 INSTANCE = (GDI32) Native.loadLibrary(GDI32.class);

	boolean BitBlt(HDC hdcDest, int nXDest, int nYDest, int nWidth, int nHeight, HDC hdcSrc, int nXSrc, int nYSrc,
			int dwRop);

	HDC GetDC(HWND hWnd);

	boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines, byte[] pixels, BITMAPINFO bi, int usage);

	boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines, short[] pixels, BITMAPINFO bi, int usage);

	boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines, int[] pixels, BITMAPINFO bi, int usage);

	int SRCCOPY = 0xCC0020;
}

interface User32 extends com.sun.jna.platform.win32.User32 {
	User32 INSTANCE = (User32) Native.loadLibrary(User32.class, W32APIOptions.UNICODE_OPTIONS);

	HWND GetDesktopWindow();
}