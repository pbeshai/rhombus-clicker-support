package ca.ubc.clickers;

import java.io.IOException;

import ca.ubc.clickers.driver.HIDManagerClickerApp;
import ca.ubc.clickers.driver.IClickerDriver;
import ca.ubc.clickers.driver.exception.ClickerException;
import ca.ubc.clickers.enums.FrequencyEnum;

import com.codeminders.hidapi.HIDDeviceNotFoundException;

public class BaseClickerApp implements ClickerApp {
	
	private static final String MAC_OS = "mac os x";
	private static final String WINDOWS = "windows";
	private static final String MAC_32 = "i386";
	private static final String WINDOWS_32 = "x86";
	
	public static final FrequencyEnum DEFAULT_CHANNEL_1 = FrequencyEnum.A;
	public static final FrequencyEnum DEFAULT_CHANNEL_2 = FrequencyEnum.A;
	
	protected IClickerDriver driver;
	protected HIDManagerClickerApp hidManager;
	
	protected String instructorId = null;
	protected FrequencyEnum channel1 = DEFAULT_CHANNEL_1;
	protected FrequencyEnum channel2 = DEFAULT_CHANNEL_2;
	protected String LCDRow1 = " Custom Clicker ";
	protected String LCDRow2 = "----------------";
	
	protected boolean acceptingVotes = false;
	protected boolean baseStationConnected = false;
	
	public BaseClickerApp() throws IOException, InterruptedException {
		this(null);
	}
	
	public BaseClickerApp(String instructorId) throws IOException, InterruptedException {
		this(instructorId, null, null);
	}
	
	public BaseClickerApp(String instructorId, FrequencyEnum channel1, FrequencyEnum channel2) throws IOException, InterruptedException {
		this.instructorId = instructorId;
		if (channel1 != null) this.channel1 = channel1;
		if (channel2 != null) this.channel2 = channel2;
		
		loadLibrary();
		hidManager = new HIDManagerClickerApp(this);
		try {
			initDriver();
		} catch (HIDDeviceNotFoundException e) {
			System.err.println("iClicker Base Station not connected.");
			baseStationConnected = false;
		}
	}
	
	public IClickerDriver getDriver() {
		return driver;
	}
	
	public String getInstructorId() {
		return instructorId;
	}
	
	public boolean isBaseStationConnected() {
		return baseStationConnected;
	}
	
	protected synchronized void initLCD() throws IOException, InterruptedException {
		if (driver != null) {
			driver.updateLCDRow1(LCDRow1);
			driver.updateLCDRow2(LCDRow2);
		}
	}
	
	public synchronized void initDriver() throws IOException, InterruptedException {
		//driver = new IClickerDriverV1(channel1, channel2, instructorId, false, hidManager);
		driver = hidManager.getIClickerDriver();
		if (driver != null) {
			initLCD();
			baseStationConnected = true;
		} else {
			baseStationConnected = false;
		}
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
	
	protected static void loadLibrary() {
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();
		
		if (os.startsWith(WINDOWS)) {
			if (arch.equals(WINDOWS_32)) {
				System.err.println("Initializing for Windows 32-bit...");
				System.loadLibrary("lib/hidapi-jni");
			} else {
				System.err.println("Initializing for Windows 64-bit...");
				System.loadLibrary("lib/hidapi-jni-64");
			}
		} else if (os.startsWith(MAC_OS)) {
			if (arch.equals(MAC_32)) {
				System.err.println("Initializing for Mac OS X 32-bit...");
				System.loadLibrary("hidapi-jni");
			} else {
				System.err.println("Initializing for Mac OS X 64-bit...");
				System.loadLibrary("hidapi-jni-64");
			}
		}
	}
	
	protected void startBaseStation() {
		try {
			driver.startBaseStation(channel1, channel2, instructorId);
		} catch (Exception e1) {
			System.err.println("ERROR: Failed to start base station.");
		}
	}
	
	public void baseStationAdded() {
		System.out.println("Base station added.");
		
		try {
			initDriver();
			startBaseStation(); //template method
			acceptingVotes = false;
			baseStationConnected = true;
		} catch(IOException e) {
			System.err.println("Error initializing base station driver");
		} catch(InterruptedException e) {
			System.err.println("Interrupted while trying to initialize base station");
		}
	}
	
	
	public void baseStationRemoved() {
		System.out.println("Base station removed.");
		baseStationConnected = false;
		acceptingVotes = false;
		driver = null;
	}
}
