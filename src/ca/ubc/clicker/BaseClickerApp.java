package ca.ubc.clicker;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.ubc.clicker.driver.HIDManagerClickerApp;
import ca.ubc.clicker.driver.IClickerDriver;
import ca.ubc.clicker.driver.exception.ClickerException;
import ca.ubc.clicker.enums.FrequencyEnum;

import com.codeminders.hidapi.HIDDeviceNotFoundException;

public class BaseClickerApp implements ClickerApp {
	private static Logger log = LogManager.getLogger();
		
	private static final String MAC_OS = "mac os x";
	private static final String WINDOWS = "windows";
	private static final String MAC_32 = "i386";
	private static final String WINDOWS_32 = "x86";
	private static final String LINUX = "linux";
	
	
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
		this(instructorId, channel1, channel2, null, null);
	}
	
	public BaseClickerApp(String instructorId, FrequencyEnum channel1, FrequencyEnum channel2, String LCDRow1, String LCDRow2) throws IOException, InterruptedException {
		this.instructorId = instructorId;
		if (channel1 != null) this.channel1 = channel1;
		if (channel2 != null) this.channel2 = channel2;
		if (LCDRow1 != null) this.LCDRow1 = LCDRow1;
		if (LCDRow2 != null) this.LCDRow2 = LCDRow2;
		
		loadLibrary();
		hidManager = new HIDManagerClickerApp(this);
		try {
			initDriver();
			startBaseStation();
		} catch (HIDDeviceNotFoundException e) {
			log.info("iClicker Base Station not connected.");
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
		log.info("Attempting to load hidapi-jni library (" + os + ", " + arch + ")");
		if (os.startsWith(WINDOWS)) {
			if (arch.equals(WINDOWS_32)) {
				log.info("Initializing for Windows 32-bit...");
				System.loadLibrary("lib/hidapi-jni");
			} else {
				log.info("Initializing for Windows 64-bit...");
				System.loadLibrary("lib/hidapi-jni-64");
			}
		} else if (os.startsWith(MAC_OS)) {
			if (arch.equals(MAC_32)) {
				log.info("Initializing for Mac OS X 32-bit...");
				System.loadLibrary("hidapi-jni");
			} else {
				log.info("Initializing for Mac OS X 64-bit...");
				System.loadLibrary("hidapi-jni-64");
			}
		} else if (os.startsWith(LINUX)) {
			if (arch.contains("64")) {// e.g. amd64
				log.info("Initializing for Linux 64-bit...");
				System.loadLibrary("hidapi-jni-64");
			} else {
				log.info("Initializing for Linux 32-bit...");
				System.loadLibrary("hidapi-jni-32");
			}
		}
	}
	
	protected void startBaseStation() {
		try {
			driver.startBaseStation(channel1, channel2, instructorId);
			log.info("Started base station.");
		} catch (Exception e1) {
			log.warn("Failed to start base station.");
		}
	}
	
	public void baseStationAdded() {
		log.info("Base station added.");
		try {
			// delay slightly to allow the base station to settle, otherwise the channel will not be set properly
			// and it will always use AA.
			Thread.sleep(100);
			
			initDriver();			
			startBaseStation(); //template method
			if (acceptingVotes) {
				log.info("Accepting votes.");
				driver.startAcceptingVotes();
			}
			baseStationConnected = true;
		} catch(IOException e) {
			log.error("Error initializing base station driver");
		} catch(InterruptedException e) {
			log.error("Interrupted while trying to initialize base station");
		} catch (ClickerException e) {
			log.error("Error while trying to start accepting votes: "+e.getMessage());
		}
	}
	
	
	public void baseStationRemoved() {
		log.info("Base station removed.");
		baseStationConnected = false;
		driver = null;
	}
}
