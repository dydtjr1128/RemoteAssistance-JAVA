import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;

import java.util.HashMap;

public class KeyBoardHook {	
	static HHOOK hhk = null;
	public static void main(String[] args) {		
		HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
		User32 lib = User32.INSTANCE;
		int result = 0;
		//HashMap<Character,Integer> codeMap = new HashMap<>();

		LowLevelKeyboardProc rr = new LowLevelKeyboardProc() {			
			@Override
			public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
				System.out.println(info.flags + "  " + info.scanCode + "  " + info.vkCode + " " + nCode + " " + wParam.intValue() + " " + wParam.doubleValue() );
				try {
					if (nCode >= 0) {						
						switch (wParam.intValue()) {
						case WinUser.WM_KEYUP:							
							System.out.println(info.vkCode +"����" );
							break;
						case WinUser.WM_KEYDOWN:
							System.out.println(info.vkCode +"����");
							break;
						case WinUser.WM_SYSKEYUP:
							System.out.println(info.vkCode +"����----"); 
							break;
						case WinUser.WM_SYSKEYDOWN:
							System.out.println(info.vkCode +"����----");
							break;
						}

					}
				} catch (Exception e) {
					
				}

				Pointer ptr = info.getPointer();

				long peer = Pointer.nativeValue(ptr);

				return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(peer));
			}
		};
		hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, rr, hMod, 0);
		MSG msg = new MSG();
		while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
			if (result == -1) {
				System.err.println("error in get message");
				break;
			} else {
				System.err.println("got message");
				lib.TranslateMessage(msg);
				lib.DispatchMessage(msg);
			}
		}
	}
}
