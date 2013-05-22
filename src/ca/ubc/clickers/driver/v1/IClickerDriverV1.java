package ca.ubc.clickers.driver.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.ubc.clickers.Vote;
import ca.ubc.clickers.driver.IClickerDriver;
import ca.ubc.clickers.driver.exception.ClickerException;
import ca.ubc.clickers.enums.FrequencyEnum;
import ca.ubc.clickers.util.StringProcess;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;
import com.codeminders.hidapi.HIDManagerTest;

/**
 * IClickerDriverOld: allow control of old iClicker 
 * base station, including starting base station, 
 * starting/stopping accepting votes, requesting 
 * votes, updating LCD. 
 * @author Junhao
 * 
 * Updated by pbeshai May 2013: synchronized methods, added ClickerException 
 *
 */
public class IClickerDriverV1 implements IClickerDriver {
	// i>Clicker base station vendor and product id.
	private static final int VENDOR_ID  = 6273;
	private static final int PRODUCT_ID = 336;
	
	// Delays happen between various packets. Do not change these magic numbers.
	// For starting base station.
	private static final long AFTER_FREQUENCY_DELAY_MS  = 140L;
	private static final long AFTER_INSTRUCTOR_DELAY_MS = 240L;
	private static final long BEFORE_NONAME_DELAY_MS    =  70L;
	private static final long AFTER_NONAME_DELAY_MS     = 210L;
	
	// For requesting votes.
	private static final long BEFORE_READ_DELAY_MS      = 100L;
	
	// For starting accepting votes.
	private static final long BEFORE_RESET_DELAY_MS     = 160L;
	private static final long AFTER_RESET_DELAY_MS      = 180L;
	
	// For stopping accepting votes.
	private static final long BEFORE_NONACCEPT_DELAY_MS = 210L;
	private static final long AFTER_NONACCEPT_DELAY_MS  = 130L;
	private static final long BEFORE_SUMMARY_DELAY_MS   = 470L;
	
	// For updating LCD.
	private static final long LCD_DELAY_MS = 5L;

	// Channel.
	private FrequencyEnum freq1, freq2;
	
	// Id of instructor's remote.
	private String instructorID;
	
	private static int BUFSIZE = 64;
	private int voteIndex = 0;
	
	private HIDDevice device;
	
	// Whether to print out packet received from base station
	private boolean ifPrintPacket = false;
	
	/**
	 * Constructor.
	 * @param freq1 first frequency code.
	 * @param freq2 second frequency code.
	 * @param instructorID id of instructor's remote which contains eight characters.
	 * @param ifPrintPacket whether to print out the packet received from the base station.
	 * @param hidManager specific HIDManager to use to interact with base station
	 * @throws IOException
	 */
	public IClickerDriverV1(FrequencyEnum freq1, FrequencyEnum freq2, String instructorID, boolean ifPrintPacket, HIDManager hidManager) throws IOException {
		if (hidManager == null) {
			new HIDManagerTest();
		}
		
		device = HIDManager.openById(VENDOR_ID, PRODUCT_ID, null);
		device.disableBlocking();
		
		this.freq1 = freq1;
		this.freq2 = freq2;
		this.instructorID = instructorID;
		this.ifPrintPacket = ifPrintPacket;
	}
	
	/**
	 * Start base station.
	 * @throws InterruptedException, IOException, ClickerException
	 */
	@Override
	public synchronized void startBaseStation() throws InterruptedException, IOException, ClickerException {
		byte[] buf = new byte[BUFSIZE];
		
		{
			//  Set frequency.
			device.write(BuildInstructions.setFreq(freq1, freq2));
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isSetFreqAck(buf) == false) {
				throw new ClickerException("Fail to receive Set Frequency Ack");
			}
			
			Thread.sleep(AFTER_FREQUENCY_DELAY_MS);
		}
		
		if(instructorID.isEmpty() == false) {
			// Set instructor remote.
			device.write(BuildInstructions.setInstructor(this.instructorID));
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isSetInstructorAck(buf) == false) {
				throw new ClickerException("Fail to receive Set Instructor Ack");
			}
			
			Thread.sleep(AFTER_INSTRUCTOR_DELAY_MS);
		}
		
		{
			// Disable voting.
			device.write(BuildInstructions.disableVoting());
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isDisableVotingAck(buf) == false) {
				throw new ClickerException("Fail to receive Disable Voting Ack");
			}
			
			Thread.sleep(BEFORE_NONAME_DELAY_MS);
		}
		
		{
			// No name packet.
			device.write(BuildInstructions.startSessionNoName());
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isStartSessionNoNameAck(buf) == false) {
				throw new ClickerException("Fail to receive Start Session No Name Ack");
			}
			
			Thread.sleep(AFTER_NONAME_DELAY_MS);
		}
	}

	/**
	 * Enable voting.
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws ClickerException 
	 * @throws InterruptedException, IOException, ClickerException
	 */
	@Override
	public synchronized void startAcceptingVotes() throws InterruptedException, IOException, ClickerException {
		byte[] buf = new byte[BUFSIZE];
		
		voteIndex = 0;
		
		{
			// Reset Counter
			Thread.sleep(BEFORE_RESET_DELAY_MS);
			
			device.write(BuildInstructions.resetCounter());
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isResetCounterAck(buf) == false) {
				throw new ClickerException("Fail to receive reset counter Ack");
			}
			
			Thread.sleep(AFTER_RESET_DELAY_MS);
		}
		
		{
			// Enable voting.
			device.write(BuildInstructions.enableVoting());
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isEnableVotingAck(buf) == false) {
				throw new ClickerException("Fail to receive Enable Voting Ack");
			}
		}
	}
	
	/**
	 * Disable voting.
	 * @return ArrayList of votes.
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws ClickerException
	 */
	@Override
	public synchronized List<Vote> stopAcceptingVotes() throws InterruptedException, IOException, ClickerException {
		byte[] buf = new byte[BUFSIZE];
		
		{
			// Disable voting.
			Thread.sleep(BEFORE_NONACCEPT_DELAY_MS);
			
			device.write(BuildInstructions.disableVoting());
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isDisableVotingAck(buf) == false) {
				throw new ClickerException("Fail to receive Disable Voting Ack");
			}
			
			Thread.sleep(AFTER_NONACCEPT_DELAY_MS);
		}
		
		{
			// Request votes.
			List<Vote> voteArray = requestVotes();
			List<Vote> voteArray2 = requestVotes();
			voteArray.addAll(voteArray2);
			
			Thread.sleep(BEFORE_SUMMARY_DELAY_MS);

			// Request summary
			device.write(BuildInstructions.requestSummary());
			Thread.sleep(BEFORE_READ_DELAY_MS);
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if(ParseInstruction.isRequestSummaryAck(buf) == false) {
				throw new ClickerException("Fail to receive Request Summary Ack");
			}
			
			device.read(buf);
			
			if (this.ifPrintPacket) {
				printBuf(buf);
			}
			
			if (ParseInstruction.isRequestSummaryRes(buf) == false) {
				throw new ClickerException("Fail to receive Request Summary Response");
			}
			
			// TODO: interpret request summary.
			
			return voteArray;
		}
	}

	/**
	 * Request votes.
	 * @return ArrayList of votes.
	 * @throws InterruptedException, IOException, ClickerException
	 */
	@Override
	public synchronized List<Vote> requestVotes() throws InterruptedException, IOException, ClickerException {
		byte[] buf = new byte[BUFSIZE];
		ArrayList<Vote> voteArray = new ArrayList<Vote>(14);

		device.write(BuildInstructions.polling(voteIndex));
		Thread.sleep(BEFORE_READ_DELAY_MS);
		
		device.read(buf);
		
		if (this.ifPrintPacket) {
			printBuf(buf);
		}
		
		if (ParseInstruction.isPollingAck(buf) == false) {
			throw new ClickerException("Fail to receive Polling Ack");
		}
		
		processResults(voteArray);

		return voteArray;
	}
	
	/**
	 * Recursively fetch packet that belongs to polling response.
	 * @param voteArray: ArrayList stores the votes.
	 * @throws InterruptedException, IOException, ClickerException
	 */
	private synchronized void processResults(ArrayList<Vote> voteArray) throws InterruptedException, IOException, ClickerException {
		byte[] buf = new byte[BUFSIZE];
				
		Thread.sleep(BEFORE_READ_DELAY_MS);

		device.read(buf);

		if (this.ifPrintPacket) {
			printBuf(buf);
		}
				
		if(ParseInstruction.isPollingRes(buf) == false) {
			throw new ClickerException("Fail to receive Polling Res");
		}
		else {
			int voteAmountLeft = ParseInstruction.getPollingResVoteAmountLeft(buf);
			int voteIndexEnd = ParseInstruction.getPollingResVoteIndexEnd(buf);
			
			voteIndex = voteIndexEnd;
			
			if(voteAmountLeft != 0) {
				ParseInstruction.getPollingResVotes(buf, voteArray);
				
				if(ParseInstruction.isPollingResLastInstruction(buf) == false) {
					processResults(voteArray);
				}			
			}
		}
	}

	/**
	 * Write on first row of LCD screen.
	 * @param str String needs to be write, must be shorter than or equal to 16 characters.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
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
	@Override
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
}
