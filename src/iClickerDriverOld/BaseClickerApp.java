package iClickerDriverOld;

import java.io.IOException;

import com.codeminders.hidapi.HIDManager;

public class BaseClickerApp {
	
	private static final String MAC_OS = "mac os x";
	private static final String WINDOWS = "windows";
	private static final String MAC_32 = "i386";
	private static final String WINDOWS_32 = "x86";
	
	public static final FrequencyEnum DEFAULT_CHANNEL_1 = FrequencyEnum.A;
	public static final FrequencyEnum DEFAULT_CHANNEL_2 = FrequencyEnum.A;
	
	protected IClickerDriverOld driver;
	
	protected String instructorId = null;
	protected FrequencyEnum channel1 = DEFAULT_CHANNEL_1;
	protected FrequencyEnum channel2 = DEFAULT_CHANNEL_2;
	protected String LCDRow1 = " Custom Clicker ";
	protected String LCDRow2 = "----------------";
	
	protected boolean acceptingVotes = false;
	
	public BaseClickerApp() {
		this(null);
	}
	
	public BaseClickerApp(String instructorId) {
		this(instructorId, null, null);
	}
	
	public BaseClickerApp(String instructorId, FrequencyEnum channel1, FrequencyEnum channel2) {
		this.instructorId = instructorId;
		if (channel1 != null) this.channel1 = channel1;
		if (channel2 != null) this.channel2 = channel2;
	}
	
	
	public IClickerDriverOld getDriver() {
		return driver;
	}
	
	public String getInstructorId() {
		return instructorId;
	}
	
	protected synchronized void initLCD() throws IOException, InterruptedException {
		driver.updateLCDRow1(LCDRow1);
		driver.updateLCDRow2(LCDRow2);		
	}
	
	protected synchronized void init(HIDManager hidManager) throws IOException, InterruptedException {
		initOS();
		initDriver(hidManager);
	}
	
	public synchronized void initDriver(HIDManager hidManager) throws IOException, InterruptedException {
		driver = new IClickerDriverOld(channel1, channel2, instructorId, false, hidManager);
		initLCD();
	}
	
	public synchronized void startAcceptingVotes() throws InterruptedException, IOException, ClickerException {
		if (!acceptingVotes) {
			acceptingVotes = true;
			if (driver != null) {
				driver.startAcceptingVotes();
			}
		}
	}
	
	public synchronized void stopAcceptingVotes() throws InterruptedException, IOException, ClickerException {
		if (acceptingVotes) {
			acceptingVotes = false;
			if (driver != null) {
				driver.stopAcceptingVotes();
			}
		}
	}
	
	public boolean isAcceptingVotes() {
		return acceptingVotes;
	}
	
	protected static void initOS() {
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();
		
		
		
		if (os.startsWith(WINDOWS)) {
			if (arch.equals(WINDOWS_32)) {
				initWin32();
			} else {
				initWin64();
			}
		} else if (os.startsWith(MAC_OS)) {
			if (arch.equals(MAC_32)) {
				initMac32();
			} else {
				initMac64();
			}
		}
	}
	
	private static void initWin32() {
		System.err.println("Initializing for Windows 32-bit...");
		System.loadLibrary("lib/hidapi-jni");
	}
	
	private static void initWin64() {
		System.err.println("Initializing for Windows 64-bit...");
		System.loadLibrary("lib/hidapi-jni-64");
	}
	
	private static void initMac32() {
		System.err.println("Initializing for Mac OS X 32-bit...");
		System.loadLibrary("hidapi-jni");	
	}
	
	private static void initMac64() {
		System.err.println("Initializing for Mac OS X 64-bit...");
		System.loadLibrary("hidapi-jni-64");
	}
	
}
