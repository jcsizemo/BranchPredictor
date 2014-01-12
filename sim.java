// John Sizemore jcsizem2 ECE 463
// Branch Predictor Project


import java.io.*;
import java.math.*;

public class sim
{	
	private int numberOfCommands = 0;		// declare instance variables for this simulation
	private int[] BranchTable;			   // declare BTB array
	private boolean isTaken = false;		// set depending on whether n or t in the trace
	private int index = 0;					// used in referencing BTB
	private int numberOfMispredictions = 0;	// counts mispredictions 
	private BigDecimal mispredictionRate;   // variable for calculating rate of mispredictions
	private String GBHR = "";		  		// the GBHR, representing in this program as a string
	
	static boolean bimodal = false;			// the static variables are all of the inputs to the console
	static boolean gshare = false;		    // upon initial startup of the program
	static boolean hybrid = false;
	static boolean XORindexing = false;
	static int lowOrderIndexBits = 0;
	static String filename = "";
	static String branchPredictorType = "";
	static int GBHRbits = 0;
	static String indexingType = "";
	
	public sim()
		{
		try
			{
			if (gshare)
				for (int i = 0; i < GBHRbits; i++)	// create GBHR if gshare using the specified number of bits
					GBHR += "0";
			int tableSize = 1;
			for (int i  = 0; i < lowOrderIndexBits; i++)	// build the Branch Table in powers of 2 of the
				tableSize *= 2;							    // value of m
			BranchTable = new int[tableSize];
			for (int i = 0; i < tableSize; i++)
				BranchTable[i] = 2;
			BufferedReader br = new BufferedReader(new FileReader(filename));	// read trace
			try
				{
				String command = br.readLine();
				while (command != null)
					{
						isTaken = false;
						if (command.endsWith("t"))	// checking whether branch taken or not
							isTaken = true;
						command = command.substring(0,command.length()-2).trim();
						numberOfCommands++;
						BigInteger bi = new BigInteger(command, 16);
						String binaryCommand = bi.toString(2);
						if (binaryCommand.length() != 32)
							{
							for (int i = 32 - binaryCommand.length(); i > 0; i--)	// getting full branch address
								{													// here, this is important
								binaryCommand = "0" + binaryCommand;				// when pulling tags/indices out
								}
							}
						binaryCommand = binaryCommand.substring(0,binaryCommand.length()-2);
						
						if (bimodal)
							{
							if (XORindexing)		// XOR the 2 pieces of the branch instruction together 
								index = binaryToDecimal(binaryCommand.substring(binaryCommand.length()-lowOrderIndexBits)) 
								^ binaryToDecimal(binaryCommand.substring(binaryCommand.length()-2*lowOrderIndexBits,binaryCommand.length()-lowOrderIndexBits));
							else
								index = binaryToDecimal(binaryCommand.substring(binaryCommand.length()-lowOrderIndexBits));
							}						// if not using XOR, then simply pull out the bits of the instruction
						
						if (gshare)
							{
							if (XORindexing)
								{					// for gshare, the XOR operates in the same fashion except after XORing the instruction to itself,
													// the resulting instruction must then have a piece of it XOR'ed with the GBHR
								index = binaryToDecimal(binaryCommand.substring(binaryCommand.length()-lowOrderIndexBits)) 
								^ binaryToDecimal(binaryCommand.substring(binaryCommand.length()-2*lowOrderIndexBits,binaryCommand.length()-lowOrderIndexBits));
								String work = Integer.toBinaryString(index);
									while (work.length() != lowOrderIndexBits)
										work = "0" + work;
								int PCindex = binaryToDecimal(work.substring(0,GBHRbits));
								//String str = Integer.toBinaryString(PCindex ^ binaryToDecimal(GBHR));
								index = binaryToDecimal(Integer.toBinaryString((PCindex ^ binaryToDecimal(GBHR))) + work.substring(GBHRbits));
								}
							else
								{				// if not using the XOR option simply XOR the piece of the PC with the GBHR
								String work = binaryCommand.substring(binaryCommand.length()-lowOrderIndexBits);
								int PCindex = binaryToDecimal(work.substring(0,GBHRbits));
								index = binaryToDecimal(Integer.toBinaryString((PCindex ^ binaryToDecimal(GBHR))) + work.substring(GBHRbits));
								}
							}
						
						if (BranchTable[index] >= 2 && !isTaken)	// checking values at the branch table and doing
							{										// predictions based on values: predicted taken
							numberOfMispredictions++;				// if greater than or equal to 2, predicted not
							}										// taken if less than or equal to 1
						if (BranchTable[index] <= 1 && isTaken)
							{
							numberOfMispredictions++;
							}
						
						if (gshare)
						{
						if (isTaken)			// this if statement is adding bits to the GBHR depending on branch outcomes
							{
							GBHR = "1" + GBHR;
							if (GBHR.length() > GBHRbits)
								GBHR = GBHR.substring(0,GBHRbits);
							}
						else
							{
							GBHR = "0" + GBHR;
							if (GBHR.length() > GBHRbits)
								GBHR = GBHR.substring(0,GBHRbits);
							}
						}
						
						if (isTaken && BranchTable[index] != 3)		// if statements controlling the variable range
							BranchTable[index]++;					// of items in the BHB
						if (!isTaken && BranchTable[index] != 0)
							BranchTable[index]--;
							
					command = br.readLine();
					}
				br.close();			// close file
				printStuff();		// print statistics and BHB contents
				}
			
			catch (IOException ioe)
				{
				System.out.println(ioe.getMessage());
				}
			}
		
		catch (FileNotFoundException fnfe)
			{
			System.out.println("Trace file not found.");
			return;
			}
		}
	
	
	public static void main(String[] args) 		// obtain variables from console
	{
	if (args.length != 4 && args.length != 5)
		{
		System.out.println("Invalid number of command line arguments for predictor configuration.");
		return;
		}
	branchPredictorType = args[0];
	if (args.length == 4 && args[0].equals("bimodal"))	// bimodal mode
		{
		bimodal = true;
		lowOrderIndexBits = Integer.valueOf(args[1]);
		if (args[2].equals("1"))
			XORindexing = true;
		filename = args[3];
		indexingType = args[2];
		}
	else if (args.length == 5 && args[0].equals("gshare"))	// gshare mode
		{
		gshare = true;
		lowOrderIndexBits = Integer.valueOf(args[1]);
		GBHRbits = Integer.valueOf(args[2]);
		if (args[3].equals("1"))
			XORindexing = true;
		indexingType = args[3];
		filename = args[4];
		}
	else
		{
		System.out.println("Invalid parameters for testing.");
		}
			
	new sim();	// instantiate sim object

	}
	
	BigDecimal getMispredictionRate()		// function calculating misprediction rate using commands and mispredictions
		{
		Integer commandsI = new Integer(numberOfCommands);
		Integer mispredictionsI = new Integer(numberOfMispredictions);
		double commandsD = commandsI.doubleValue();
		double mispredictionsD = mispredictionsI.doubleValue();
		double mispredictionRateD = (mispredictionsD/commandsD) * 100;
		BigDecimal mispredictionRate = new BigDecimal(mispredictionRateD,MathContext.DECIMAL32);
		mispredictionRate = mispredictionRate.setScale(2,BigDecimal.ROUND_HALF_DOWN);
		return mispredictionRate;
		}
	int binaryToDecimal(String binary)		// various number formatting functions
		{
		BigInteger bi = new BigInteger(binary,2);
		String decString = bi.toString(10);
		int decimal = Integer.parseInt(decString);
		return decimal;
		}
	
	long binaryToLong(String binary)
		{
		BigInteger bi = new BigInteger(binary,2);
		String decString = bi.toString(10);
		long decimal = Long.parseLong(decString);
		return decimal;
		}
	
	String decimalToBinary(int decimal)
		{
		Integer decI = new Integer(decimal);
		String binary = decI.toString();
		BigInteger bi = new BigInteger(binary,10);
		binary = bi.toString(2);
		return binary;
		}
	
	String binaryToHex(String binary)
		{
		BigInteger bi = new BigInteger(binary,2);
		String hex = bi.toString(16);
		return hex;
		}
	
	String hexToBinary(String hex)
		{
		BigInteger bi = new BigInteger(hex,16);
		String binary = bi.toString(2);
		return binary;
		}
	
	void printStuff()			// print statements putting statistics and BHB contents on the console
	{
	System.out.println("COMMAND");
	if (bimodal)
		System.out.println("./sim " + branchPredictorType + " " + lowOrderIndexBits + " " + indexingType + " " + filename);
	if (gshare)
		System.out.println("./sim " + branchPredictorType + " " + lowOrderIndexBits + " " + GBHRbits + " " + indexingType + " " + filename);
	System.out.println("OUTPUT");
	System.out.println("Number of predictions: " + numberOfCommands);
	System.out.println("Number of mispredictions: " + numberOfMispredictions);
	mispredictionRate = getMispredictionRate();
	System.out.println("Misprediction rate: " + mispredictionRate + "%");
	if (bimodal)
		System.out.println("FINAL BIMODAL CONTENTS");
	if (gshare)
		System.out.println("FINAL GSHARE CONTENTS");
	for (int i = 0; i < BranchTable.length; i++)
		{
		System.out.println(i + "	" + BranchTable[i]);
		}
	}
	
}
	



