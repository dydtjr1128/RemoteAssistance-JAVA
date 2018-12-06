
public class DebugMessage {
	public static void printDebugMessage(String message) {
		System.out.println(message);
	}

	public static void printDebugMessage(Exception e) {
		e.getStackTrace();
		System.out.println(e.getMessage());
	}
}
