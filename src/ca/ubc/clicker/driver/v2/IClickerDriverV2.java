package ca.ubc.clicker.driver.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.ubc.clicker.Vote;
import ca.ubc.clicker.driver.IClickerDriver;
import ca.ubc.clicker.driver.exception.ClickerException;
import ca.ubc.clicker.enums.FrequencyEnum;
import ca.ubc.clicker.util.StringProcess;

import com.codeminders.hidapi.HIDDevice;

/**
 * IClickerDriverNew: allow control of new iClicker 
 * base station including starting base station, 
 * starting/stopping accepting votes, requesting 
 * votes, updating LCD. 
 * @author Junhao
 * Updates by pbeshai May 2013: synchronized functions, IClickerDriver interface, HID support reworking
 */
public class IClickerDriverV2 implements IClickerDriver {
	// i>Clicker base station vendor and product id.
	public static final int VENDOR_ID  = 6273;
	public static final int PRODUCT_ID = 336;
	
	// For updating LCD.
	private static long LCD_DELAY_MS = 5L;

	private static int BUFSIZE = 64;
	
	private HIDDevice device;
	
	// Whether to print out packet received from base station
	private boolean ifPrintPacket = false;
	
	/**
	 * Constructor.
	 * @param device the HID device representing the clicker base station
	 * @throws ClickerException, IOException, InterruptedException
	 */
	public IClickerDriverV2(HIDDevice device) throws IOException {
		this.device = device;
		device.disableBlocking();
	}
	
	/**
	 * Start base station.
	 * @param freq1 first frequency code.
	 * @param freq2 second frequency code.
	 * @param instructorID id of instructor's remote which contains eight characters.
	 * @throws ClickerException, IOException, InterruptedException
	 */
	@Override
	public synchronized void startBaseStation(FrequencyEnum freq1, FrequencyEnum freq2, String instructorID) throws InterruptedException, IOException, ClickerException {
		byte[] buf = new byte[BUFSIZE];
		
		{
			device.write(BuildInstructions.PCC1(freq1, freq2));
			Thread.sleep(22L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}			
		}
		
		{
			device.write(BuildInstructions.PCC2(freq1, freq2));
			Thread.sleep(5071L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{
			device.write(BuildInstructions.PCC3());
			Thread.sleep(16L);

			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{
			device.write(BuildInstructions.PCC4());
			Thread.sleep(17L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		if(instructorID.isEmpty() == false) {
			device.write(BuildInstructions.PCC5(instructorID));
			Thread.sleep(15);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{		
			device.write(BuildInstructions.PCC6());
			Thread.sleep(16L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}

		{		
			device.write(BuildInstructions.PCC7());
			Thread.sleep(15L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{		
			device.write(BuildInstructions.PCC8());
			Thread.sleep(17L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{		
			device.write(BuildInstructions.PCC9());
			Thread.sleep(94L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{		
			device.write(BuildInstructions.PCC10());
			Thread.sleep(304L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{		
			device.write(BuildInstructions.PCC11());
			Thread.sleep(100L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{
			// To deal with BSR X
			device.read(buf);
			
			Thread.sleep(100L);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
	}

	/**
	 * Enable voting.
	 * @throws ClickerException, IOException, InterruptedException
	 */
	public synchronized void startAcceptingVotes() throws ClickerException, IOException, InterruptedException {
		byte[] buf = new byte[BUFSIZE];
		
		{	
			device.write(BuildInstructions.PCC21());
			Thread.sleep(282L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC22());
			Thread.sleep(156L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC23());
			Thread.sleep(94L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC24());
			Thread.sleep(78L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC25());
			Thread.sleep(100L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		

		{
			// To deal with BSR X
			device.read(buf);
			
			Thread.sleep(100L);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
	}
	
	/**
	 * Disable voting.
	 * @return ArrayList of votes.
	 * @throws ClickerException, IOException, InterruptedException
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public synchronized List<Vote> stopAcceptingVotes() throws ClickerException, IOException, InterruptedException {
		byte[] buf = new byte[BUFSIZE];
		ArrayList<Vote> votes = new ArrayList<Vote>();
		
		{	
			device.write(BuildInstructions.PCC31());
			Thread.sleep(16L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC32());
			Thread.sleep(16L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC33());
			Thread.sleep(94L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC33());
			Thread.sleep(94L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC34());
			Thread.sleep(95L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		{	
			device.write(BuildInstructions.PCC35());
			Thread.sleep(100L);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
		}
		
		do {
			device.read(buf);
			Thread.sleep(100L);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if (ParseInstruction.isVote(buf) == true) {
				Vote vote = ParseInstruction.getVote(buf);
				vote.setId(vote.getId().toUpperCase());
				votes.add(vote);
			}
		}
		while (ParseInstruction.isSummary(buf) == false);
		
		return votes;
	}

	/**
	 * Request vote.
	 * @return A piece of vote if there is vote, or null if there is no vote.
	 * @throws ClickerException, IOException, InterruptedException
	 */
	public synchronized Vote requestVote() throws ClickerException, IOException, InterruptedException {
		byte[] buf = new byte[BUFSIZE];
		Vote vote = null;
		
		device.read(buf);
		
		if (this.ifPrintPacket) {
			printBuf(buf);
		}
		
		vote = ParseInstruction.getVote(buf);
		
		if (vote != null) {
			vote.setId(vote.getId().toUpperCase());
		}
		
		return vote;
	}
	
	/**
	 * Requests multiple votes. 
	 * @return
	 * @throws ClickerException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized List<Vote> requestVotes() throws ClickerException, IOException, InterruptedException {
		List<Vote> votes = new ArrayList<Vote>(14);
		
		Vote v = requestVote();
		
		while (v != null) {
			votes.add(v);
			v = requestVote();
		} 
	
		return votes;
	}

	/**
	 * Write on first row of LCD screen.
	 * @param str String needs to be write, must be shorter than or equal to 16 characters.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized void updateLCDRow1(String str) throws IOException, InterruptedException {
		if(str.length() > 16) {
			System.err.println("Bad string length");
			return;
		}
		
		device.write(BuildInstructions.LCDPrint(str, 1));
		
		Thread.sleep(LCD_DELAY_MS);
	}

	/**
	 * Write on second row of LCD screen.
	 * @param str String needs to be write, must be shorter than or equal to 16 characters.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized void updateLCDRow2(String str) throws IOException, InterruptedException {
		if(str.length() > 16) {
			System.err.println("Bad string length");
			return;
		}
		
		device.write(BuildInstructions.LCDPrint(str, 2));
		
		Thread.sleep(LCD_DELAY_MS);
	}
	
	/**
	 * Print buffer.
	 * @param buf buffer to be printed.
	 */
	private static void printBuf(byte[] buf) {
		System.out.println(StringProcess.byte2HexString(buf, 0, BUFSIZE-1));
	}

	public boolean isIfPrintPacket() {
		return ifPrintPacket;
	}

	public void setIfPrintPacket(boolean ifPrintPacket) {
		this.ifPrintPacket = ifPrintPacket;
	}
	
	
}
