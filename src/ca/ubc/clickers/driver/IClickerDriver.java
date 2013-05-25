package ca.ubc.clickers.driver;

import java.io.IOException;
import java.util.List;

import ca.ubc.clickers.Vote;
import ca.ubc.clickers.driver.exception.ClickerException;
import ca.ubc.clickers.enums.FrequencyEnum;

public interface IClickerDriver {

	/**
	 * Start base station.
	 * @throws InterruptedException, IOException, ClickerException
	 */
	public abstract void startBaseStation(FrequencyEnum freq1, FrequencyEnum freq2, String instructorID) throws InterruptedException, IOException, ClickerException;

	/**
	 * Enable voting.
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws ClickerException 
	 * @throws InterruptedException, IOException, ClickerException
	 */
	public abstract void startAcceptingVotes() throws InterruptedException, IOException, ClickerException;

	/**
	 * Disable voting.
	 * @return ArrayList of votes.
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws ClickerException
	 */
	public abstract List<Vote> stopAcceptingVotes() throws InterruptedException, IOException, ClickerException;

	/**
	 * Request votes.
	 * @return ArrayList of votes.
	 * @throws InterruptedException, IOException, ClickerException
	 */
	public abstract List<Vote> requestVotes() throws InterruptedException, IOException, ClickerException;

	/**
	 * Write on first row of LCD screen.
	 * @param str String needs to be write, must be shorter than or equal to 16 characters.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public abstract void updateLCDRow1(String str) throws IOException, InterruptedException;

	/**
	 * Write on second row of LCD screen.
	 * @param str String needs to be write, must be shorter than or equal to 16 characters.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public abstract void updateLCDRow2(String str) throws IOException, InterruptedException;

}