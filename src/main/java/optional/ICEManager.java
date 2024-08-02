package optional;

import java.util.ArrayList;

import verification.TestExample;

/**
 * 
 * @author Thomas Welsch
 *
 */
public class ICEManager {


	/**
	 * List of active positive examples for predicate synthesis.
	 */
	private ArrayList<TestExample> examples = new ArrayList<>();
	
	

	/**
	 * List of active counterexamples for predicate synthesis.
	 */
	private ArrayList<TestExample> counterExamples = new ArrayList<>();
	
	
	/**
	 * List of active implication counterexamples
	 */
	private ArrayList<TestExample> implicationCounterExamples = new ArrayList<>();
	
	/**
	 * List of all discarded TestExamples.
	 */
	private ArrayList<TestExample> discardedTestExamples = new ArrayList<>();

	
	//private String synthesisType;
	private boolean allowDuplicates;
	private int maxTests;
	
	/**
	 * Standard constructor that sets internal class values based off parameters.
	 * @param allowDuplicates Determines whether there can be duplicates among active CounterExamples.
	 * @param maxTests Cap on the number of active CounterExamples allowed.
	 * @param synthesisType The synthesis type, there are two options, "predicate" and "program", if another string is given defaults to the latter
	 */
    public ICEManager(boolean allowDuplicates, int maxTests) {
		this.allowDuplicates = allowDuplicates;
		this.maxTests = maxTests;
	
    }
    
    public int appendToCounterExamples(ArrayList<TestExample> ces) {
    	int numAdded = 0;
    	for (TestExample ce : ces) {
    		numAdded += appendToCounterExamples(ce) ? 1 : 0;
    	}
    	
    	return numAdded;
    }
    
	/**
     * Attempts to add CounterExample ce to the list of CounterExamples being tracked. 
     * If duplicates are not allowed and the counterexample is contained in list already, ce is not added.
     * Otherwise, ce is appended to the list. If the list size is over maxTests, the oldest test from the list is discarded.
     * @param ce An I/O CounterExample.
     * @return true if ce is appended, false otherwise.
    */
    public boolean appendToCounterExamples(TestExample ce) {
    	
    	
    		return predicateAppendToCounterExamples(ce);

    }


    
    /**
     * Internal function that attempts to append to active counterExamples. Works similarly to as described in <strong> appendToCounterExample(CounterExample ce) </strong>.
     * The key difference is there are in fact two lists, negative and positive, so we first determine to which list ce should attempt to be added.
     * @param ce An I/O CounterExample.
     * @return true if ce is appended, false otherwise.
     */
    private boolean predicateAppendToCounterExamples(TestExample ce) {
    	
    	//determine which list we should attempt to append
    	boolean positive = ce.getOutput() == 1;
    	if (ce.getCounterexampleType().equals("ICE")) {
    		if (implicationCounterExamples.contains(ce) && !allowDuplicates) {
    		//	System.out.println("Duplicate");
    			return false;
    		}
    		
    	//	System.out.println(ce.toString());
    		implicationCounterExamples.add(ce);
    		
    		//if over the cap, dictated by maxTests/2, remove oldest example and add to discard pile
    		if (implicationCounterExamples.size() > maxTests) {
    			discardedTestExamples.add(implicationCounterExamples.remove(0));
    		}
    	}
    	else if (positive) {
    		//if example contained and no duplciates, return false
    		if (examples.contains(ce) && !allowDuplicates) {
    			//System.out.println("Duplicate");
    			return false;
    		}
    		
    		//System.out.println(ce.toString());
    		
    		//add to list
    		examples.add(ce);
    		
    		//if over the cap, dictated by maxTests/2, remove oldest example and add to discard pile
    		if (examples.size() > maxTests) {
    			discardedTestExamples.add(examples.remove(0));
    		}
    	} else {
    		//As above so below
    		
    		if (counterExamples.contains(ce) && !allowDuplicates) {
    			//System.out.println("Duplicate");
    			return false;
    		}
    		
    		//System.out.println(ce.toString());
    		
    		counterExamples.add(ce);
    		
    		if (counterExamples.size() > maxTests) {
    			discardedTestExamples.add(counterExamples.remove(0));
    		}
    	}
    	
    	return true;
    }
    
    
    
    /**
     * Retrieves the list of active CounterExamples.
     * @return The list of CounterExamples found that haven't been discarded (i.e. due to going over cap). 
     */
	public ArrayList<TestExample> retrieveActiveCounterExamples() {
			//concatenates positive and negative active CounterExamples into a single list
			ArrayList<TestExample> predicateCounterExamples = new ArrayList<>();
			predicateCounterExamples.addAll(examples);
			predicateCounterExamples.addAll(counterExamples);
			predicateCounterExamples.addAll(implicationCounterExamples);
			return predicateCounterExamples;


	}
	
	/**
	 * Retrieves the list of any CounterExample found since construction. This includes examples that were discarded after maxTests cap was exceeded.
	 * @return The list of all CounterExamples found since construction.
	 */
	public ArrayList<TestExample> retrieveActiveAndDiscardedCounterExamples() {
		//returns concatenation of active CounterExamples with discarded ones.
		ArrayList<TestExample> retVal = new ArrayList<>();
		retVal.addAll(discardedTestExamples);
		retVal.addAll(retrieveActiveCounterExamples());
		return retVal;
	}
	
	/**
	 * Counts the number of active positive CounterExamples.
	 * @return The number of active positive CounterExamples.
	 */
	public int countPositives() {
		return examples.size();
	}
	
	/**
	 * Counts the number of active negative CounterExamples.
	 * @return The number of active negative CounterExamples.
	 */
	public int countNegatives() {
		return counterExamples.size();
	}
	
	public int countImplications() {
		return implicationCounterExamples.size();
	}
	/**
	 * Counts the number of active tests.
	 * @return The number of active TestExamples.
	 */
	public int countTests(boolean checkICE) {
		if (checkICE) {
		return examples.size() + counterExamples.size() + implicationCounterExamples.size();
		} else {
			//System.out.println("This one");
			return examples.size() + counterExamples.size();
		}

	}
	
	public int countTests() {
		return examples.size() + counterExamples.size() + implicationCounterExamples.size();

	}

    
    
    
    
    
    
}
