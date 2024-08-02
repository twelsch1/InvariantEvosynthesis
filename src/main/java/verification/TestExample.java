package verification;

import java.util.ArrayList;

/**
 * A class that holds input/output examples, and contains utility functions for handling them. 
 * @author Thomas Welsch
 *
 */
public class TestExample {
	/**
	 * Array that holds function inputs.
	 */
	private long[] inputs;
	
	
	private long[] primeInputs;
	
	private long[] globalVariables;
	/**
	 * The output corresponding to that input obtained through verification.
	 */
	private int output;
	
	private String counterexampleType;
	
	
	
	public TestExample(long[] inputs, int output, long[] globalVariables) {
		this.inputs = inputs;
		this.output = output;
		this.counterexampleType = "PosNeg";
		this.globalVariables = globalVariables;
	}
	
	public TestExample(long[] inputs, long[] primeInputs, long[] globalVariables) {
		this.inputs = inputs;
		this.primeInputs = primeInputs;
		this.counterexampleType = "ICE";
		this.globalVariables = globalVariables;
	//	System.out.println("Guten tag");
		
	}
	
	public long[] getInputs() {
		return inputs;
	}
	public void setInputs(long[] inputs) {
		this.inputs = inputs;
	}
	
	public int getOutput() {
		return output;
	}
	
	public void setOutput(int output) {
		this.output = output;
	}
	
	
	
	public long[] getPrimeInputs() {
		return primeInputs;
	}

	public void setPrimeInputs(long[] primeInputs) {
		this.primeInputs = primeInputs;
	}
	
	
	/**
	 * 
	 * @return Returns "PosNeg" for counterexamples that aren't implications, "ICE" otherwise
	 */
	public String getCounterexampleType() {
		return counterexampleType;
	}

	@Override
	/**
	 * Creates a String Mapping that is unique to each I/O configuration. Can be used to check for duplicates.
	 */
	public String toString() {
		
		
		String retVal = "Input: ";
		for(int i = 0; i < inputs.length; i++) {
			retVal += inputs[i] + "\n";
		}
		
		retVal += "Global Variables: ";
		for (int i = 0; i < globalVariables.length; i++) {
			retVal += globalVariables[i] + "\n";
		}
		
		if (primeInputs != null) {
			retVal += "Prime Input: ";
			for(int i = 0; i < inputs.length; i++) {
				retVal += primeInputs[i] + "\n";
			}
		}
		retVal += "Output: " + output;
		
		
		return retVal;
	}
	
	
	/**
	 * A helper function that converts an ArrayList of CounterExample objects to a 2D double array. Potentially useful
	 * for working with external libraries.
	 * @param counterExamples
	 * @return double[][] - 2D array of doubles representing the counter
	 */
	public static double[][] examplesToPoints(ArrayList<TestExample> counterExamples) {
		
		int cols = counterExamples.get(0).getInputs().length;
		double[][] points = new double[counterExamples.size()][cols];
		
		for (int i = 0; i < counterExamples.size(); i++) {
			long[] exampleInputs = counterExamples.get(i).getInputs();
			
			for (int j = 0; j < cols; j++) {
				points[i][j] = exampleInputs[j];
			}
		}
		
		
		return points;
	}
	
	@Override
	public boolean equals(Object o) {
		TestExample comp = (TestExample) o;
		return this.toString().equals(comp.toString());
	}
}
