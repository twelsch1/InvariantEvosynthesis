package evoSynthesis;


import com.microsoft.z3.Status;
import datatypes.LongData;
import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ecjSimple.SimpleEvolutionStateWithVerification;
import fitness.VerifiableFitness;
import optional.ICEManager;
import verification.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


@SuppressWarnings("serial")
public class CLIAProblem extends GPProblem implements SimpleProblemForm  {

	public static final String P_DATA = "data";
	//private String synthType;

	//private boolean doVerification = true;
	
	//public static String[] variableNames;
	//used by ecjvar's to know what to substitute in on eval...
	public long currentInputs[];
	//maximum number of tests allowed, helps us to avoid things getting out of control 
	private int maxTests = 7500;
	private boolean checkICE = true;
	private boolean allowDuplicates = false;
	private int initialTests = 50;
	//private int initialTests = 25;
	//private int initialTests = 500;
	private int verificationNumThreads = 1;
	private boolean silent;
	
	private HashSet<String> encounteredPrograms = new HashSet<>();
	
	private int maxAttemptsFactor = 1;
	private int maxCalls = 10;
	
	//maximum number of tests allowed per generation
	private int maxTestsPerGeneration = 15;
	
	//runs a full verification every verificationFrequency generations
	private int verificationFrequency = 2; 
	
	private Verifier verifier;
	
	ICEManager ceManager = null;
			
	
	
    public void setup(final EvolutionState state,
                      final Parameter base) {

        super.setup(state,base);
        SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
        if (st.getVerifier() != null) {
        	verifier = st.getVerifier();
        }
        
        silent = state.parameters.getBoolean(new Parameter("silent"), new Parameter("silent"), false);
        //checkICE = state.parameters.getBoolean(new Parameter("checkice"), new Parameter("checkice"), false);

        //if (!checkICE) {
        	//System.out.println("Hello");
        //}
        
		verificationNumThreads = state.parameters.getInt(new Parameter("verificationthreads"), new Parameter("verificationthreads"), 1);

		
		
		ceManager = new ICEManager(allowDuplicates,maxTests);
        
    }
    
	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation,
			final int threadnum) {
		if (!ind.evaluated) { // don't bother reevaluating if evaluated on a previous generation, wasn't
								// perfect anyway and would add overhead.

			//String checkMe = ((GPIndividual) ind).trees[0].child.makeLispTree();
			
			/*if (checkMe.contains("mod")) {
				System.out.println(checkMe);
			}*/
			LongData input = new LongData();
			float sum = 0;
			float positiveSum = 0;
			float negativeSum = 0;
			float impsSum = 0;
			int hits = 0;
			int hit;

			final ArrayList<Fitness> trials = new ArrayList<Fitness>();
			ArrayList<TestExample> counterExamples = ceManager.retrieveActiveCounterExamples();
			try {
				for (int i = 0; i < counterExamples.size(); i++) {
					TestExample ce = counterExamples.get(i);

					if (ce.getCounterexampleType().equals("ICE")) {
						// Checks that Program(inputs) -> Program(PrimeInputs).
						// If Program(Inputs) is false, we don't care.
						currentInputs = ce.getInputs();

						((GPIndividual) ind).trees[0].child.eval(null, 0, input, null, null, this);
						long check = input.x;

						// assume it is correct, unless proven otherwise below.
						hit = 1;

						// if it is false i.e. input.x is 0, we don't care'
						if (check == 1) {

							currentInputs = ce.getPrimeInputs();
							((GPIndividual) ind).trees[0].child.eval(null, 0, input, null, null, this);

							check = input.x;
							// only add to impsSum indicating incorrect if Program(PrimeInputs) is false
							// i.e. input.x is 0.
							if (check == 0) {
								impsSum++;
								hit = 0;
							}
						}

					} else {
						currentInputs = ce.getInputs();
						int expectedResult = ce.getOutput();
						((GPIndividual) ind).trees[0].child.eval(null, 0, input, null, null, this);
						long check = input.x;
						// int check = progNode.evaluate(currentInputs);
						// hit being 1 indicates it was equal to the expected result
						hit = expectedResult == check ? 1 : 0;

						// Math.abs(hit-1) will add 0 when correct i.e. that's good as 0 is ideal
						// fitness. When incorrect, adds 1
						if (expectedResult == 1) {
							positiveSum += Math.abs(hit - 1);
						} else {
							negativeSum += Math.abs(hit - 1);
						}
					}

					final SimpleFitness trialFitness = new SimpleFitness();
					trialFitness.setFitness(state, hit, false);
					trials.add(trialFitness);
					hits += hit;

				}
			} catch (ArithmeticException ae) {
				//When this is hit it means we have overflowed integers with some arithmetic operation in the program.
				//We ensure with the below that it is a program with the worst possible fitness and does not get 
				//credit for solving any examples. We then carry on.
				for (int i = 0; i < counterExamples.size(); i++) {
					final SimpleFitness trialFitness = new SimpleFitness();
					trialFitness.setFitness(state, 0, false);
					trials.add(trialFitness);
				}
				positiveSum = ceManager.countPositives();
				negativeSum = ceManager.countNegatives();
				impsSum = ceManager.countImplications();
				//illegalPrograms++;
				//System.out.println("Encountered a wrong'un");
			}

			VerifiableFitness f = ((VerifiableFitness) ind.fitness);
			f.trials = trials;

			int numNegatives = ceManager.countNegatives();
			int numPositives = ceManager.countPositives();
			int numImps = ceManager.countImplications();
			


			// if perfect, (numPositives - positiveSum)/numPositives = 1, so we subtract
			// that from 1
			// as 0 means ideal while higher means worse for standardizedFitness.

			//Note, at the end of these processes these "sums" are fractions, but whatever.
			if (numPositives != 0) {
				positiveSum = 1 - ((numPositives - positiveSum) / numPositives);
			}
			if (numNegatives != 0) {
				negativeSum = 1 - ((numNegatives - negativeSum) / numNegatives);
			}

			//System.out.println(checkICE);
			if (numImps != 0) {
				impsSum = 1 - ((numImps - impsSum) / numImps);
			} else {
				impsSum = 0;
			}

			// we take the fitness as the highest of the three i.e. the worst
			// performing

			// sum becomes worst of pos/neg
			sum = positiveSum > negativeSum ? positiveSum : negativeSum;

			// sum becomes worst of pos/neg/impsSum
			sum = sum > impsSum ? sum : impsSum;

			f.setStandardizedFitness(state, sum);
			f.hits = hits;
			ind.evaluated = true;

			if (Thread.interrupted()) {
				SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
				st.setInterrupted(true);
			}
		}
	}

    public void verifyPopulation(ArrayList<Individual> individuals, boolean add, int maxTestsAllowed,
			int threads) throws InterruptedException  {
    	
    	//if (threads == 1 ) {
    		verifyPopulationSerial(individuals, add, maxTestsAllowed);
    	//} else {
    		//verifyPopulationParallelStreaming(individuals, add, maxTestsAllowed, threads);
    	//}
    	
    }
    
    public boolean verifyPerfectIndividuals(ArrayList<Individual> individuals, int maxCalls) {
    	
    	int i = 0;
    	//int callsMade = 0;
    	Collections.sort(individuals);
    	VerificationCallParameters vcp = new VerificationCallParameters();
    	vcp.setTimeout(1000);
    	int checks = 0;
    	//maxCalls = 500;
    	while (i < individuals.size() && checks < maxCalls) {
    		
    		Individual ind = individuals.get(i);
    		String program = ((GPIndividual) ind).trees[0].child.makeLispTree();
    		VerifiableFitness f = (VerifiableFitness) individuals.get(i).fitness;
    		//System.out.println("Hits " + f.hits + "Examples " + ceManager.countCounterExamples());
    		int counterExamplesBeforeAdding = ceManager.countTests();
    		if (f.hits == counterExamplesBeforeAdding) {
    			
				try {
			//		callsMade++;
				//	System.out.println("Verifying Perfect");
					VerificationResult vr = verifier.verify(program, vcp);
					checks++;
					//System.out.println("Verification complete");
					if (vr.getStatus() == Status.UNSATISFIABLE) {
						//System.out.println("Found perfect program");
						f.setVerified(true);
						return true;
					} else if (vr.getStatus() == Status.SATISFIABLE) {
						//System.out.println("Hello?");
						
						int added = ceManager.appendToCounterExamples(vr.getCounterExamples());
						
						//System.out.println("Added " + added);
					} else {
						//vr.getException().printStackTrace();
						throw vr.getException();
					}
				} catch (VerificationException e) {
					//////System.out.println(e.toString());
				} catch (Exception e) {
					//////System.out.println(e.toString());
				}
    		}
    		i++;
    		
    	}
    	return false;
    }
    /*
	@SuppressWarnings("unchecked")
	
	public void verifyPopulationParallelStreaming(ArrayList<Individual> individuals, boolean add, int maxTestsAllowed,
			int threads) throws InterruptedException {

		Collections.sort(individuals);

		int i = 0;
		int numCounterExamplesFound = 0;
		int activeThreads = 0;
		int attempts = 0;
		 
		ArrayList<CounterExample> initialCounterExamples = ceManager.retrieveActiveCounterExamples();

		int maxAttempts = maxTestsAllowed * maxAttemptsFactor;
		// get counterexamples from the programs that have already been verified and are
		// perfect.
		// breaks loop once a non-perfect program found
    	while (i < individuals.size()) {
    		VerifiableFitness f = (VerifiableFitness) individuals.get(i).fitness;
    		ArrayList<CounterExample> ces = f.getCounterExamples();
    		i++;
    		if (ces == null || ces.isEmpty()) {
    			break;
    		}
    		numCounterExamplesFound += ceManager.appendToCounterExamples(ces) ? 1 : 0;
    		
    	}

		ExecutorService exec = Executors.newFixedThreadPool(threads);
		CompletionService<VerificationResult> ecs = new ExecutorCompletionService<VerificationResult>(exec);

		try {

			// if add is true, then we go until we have generated the max tests allowed per
			// generation or hit our maximum attempts
			while (add && i < individuals.size() && attempts < maxAttempts && numCounterExamplesFound < maxTestsAllowed) {
				//////System.out.println("hi");
				if (activeThreads == threads) {
					VerificationResult vr = null;
					try {
						vr = ecs.take().get();
						
						if (vr.getStatus() == Status.SATISFIABLE) {
							numCounterExamplesFound += ceManager.appendToCounterExamples(vr.getCounterExample()) ? 1 : 0;

						} else if (vr.getStatus() == Status.UNKNOWN && vr.getException() != null) {
							//vr.getException().printStackTrace();
							//////System.out.println(vr.getException().getMessage());
						}
						
					} catch (InterruptedException e) {
						throw e;
					} catch (ExecutionException e) {
						e.printStackTrace();
						////System.out.println(e.getMessage());
					}
					

					
					activeThreads--;

				} else {

					String program = ((GPIndividual) individuals.get(i)).trees[0].child.makeLispTree();
					if (!encounteredPrograms.contains(program)) {
						if (Thread.interrupted()) {
							throw new InterruptedException("Interrupted");
						}
						ecs.submit(new VerificationCallable(program, verifier, initialCounterExamples,incorrectVerificationTimeout));
						encounteredPrograms.add(program);
						attempts++;
						activeThreads++;
					}

					i++;

				}
			}

		} finally {
			exec.shutdownNow();
		}

	}*/
    
    @SuppressWarnings("unchecked")
	public void verifyPopulationSerial(ArrayList<Individual> individuals, boolean add, int maxTestsAllowed) {
    	Collections.sort(individuals);
    	int i = 0;
    	int numCounterExamplesFound = 0;
    	
    	
    	
    	int maxAttempts = maxTestsAllowed*maxAttemptsFactor;
    	//get counterexamples from the programs that have already been verified, we always do this
    	//why did we comment this out?
    	/*int perfectChecked = 0;
    	while (i < individuals.size()) {
    		VerifiableFitness f = (VerifiableFitness) individuals.get(i).fitness;
    		ArrayList<CounterExample> ces = f.getCounterExamples();
    		i++;
    		perfectChecked++;
    		if (ces == null || ces.isEmpty()) {
    			break;
    		}
    	//	System.out.println("I guess we are here, but why isn't this adding counterexamples?");
    		numCounterExamplesFound += ceManager.appendToCounterExamples(ces);
    		//System.out.println("NumCounterexamplesFound " + numCounterExamplesFound);
    		f.clearCounterExamples();
    		
    	}*/
    	
    //	System.out.println("Now i is " + i + "And the maxattempts+perfectChecked is " + (maxAttempts+perfectChecked));
    	    	
    	//if add is true, then we go until we have verified all programs or generated the max tests allowed per generation.
    	while (add && i < individuals.size() && i < maxAttempts && numCounterExamplesFound < maxTestsAllowed) {
    		String program = ((GPIndividual)individuals.get(i)).trees[0].child.makeLispTree();
    		if (!encounteredPrograms.contains(program)) {
    			encounteredPrograms.add(program);
    		} else {
    			i++;
    			continue;
    		}
    	
 
    		try {
    			VerificationCallParameters vcp = new VerificationCallParameters(ceManager.retrieveActiveCounterExamples(),1000,verificationNumThreads,null);
    			//System.out.println("Begin ver");
    			VerificationResult vr = verifier.verify(program, vcp);
    			//System.out.println("End ver");
    			if (vr.getStatus() == Status.UNKNOWN && vr.getException() != null) {
    				throw vr.getException();
    			}
    			
				//System.out.println("Yes we are here");
    			
				if (vr.getStatus() == Status.SATISFIABLE) {

	    			numCounterExamplesFound += ceManager.appendToCounterExamples(vr.getCounterExamples());
	    		} 
			} catch (VerificationException e) {
				//////System.out.println(e.toString());
			} catch (Exception e) {
				//////System.out.println(e.toString());
			}
    		
    		
    		  
    		i++;
    	}	
    }
    
    
    public void preEvaluation(final EvolutionState state, final int threadnum) {
    	
		SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
		//if (doVerification) {
			if (state.generation == 0) {
				//////System.out.println("Kick off for new GP process, let's get some CounterExamples");

				

				
				if (st.getPreviousCounterExamples() != null) {
					ceManager.appendToCounterExamples(st.getPreviousCounterExamples());
				} else {
					ceManager.appendToCounterExamples(verifier.generateInitialExamples(initialTests));
					/*ArrayList<Individual> individuals = new ArrayList<>();
					individuals.addAll(state.population.subpops.get(0).individuals);
					try {
						verifyPopulation(individuals, true, initialTests,verificationNumThreads);
					} catch (InterruptedException e) {
						////System.out.println("Apparently here");
						st.setInterrupted(true);
					}*/
				}
				
			//	verificationFrequency = st.getVerificationFrequency(); 
				//System.out.println("Verification frequency " + verificationFrequency);
			}
			verificationFrequency = st.getVerificationFrequency(); 
			//System.out.println("Verification frequency " + verificationFrequency);
		//}
	
    }
    

	public void postEvaluation(final EvolutionState state, final int threadnum)  {


    	//if (doVerification) {
    		boolean perfectFound = verifyPerfectIndividuals(state.population.subpops.get(0).individuals, maxCalls);
    		ArrayList<Individual> individuals = state.population.subpops.get(0).individuals;
			if (state.generation == state.numGenerations - 1) {

				if (!perfectFound) {
					
					/*for (int i = 0; i < individuals.size(); i++) {
						((VerifiableFitness) ((GPIndividual) individuals
								.get(i)).fitness).setFoundCounterExamples(ceManager.retrieveActiveAndDiscardedCounterExamples());
					}*/
					
					SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
					st.setPreviousCounterExamples(ceManager.retrieveActiveCounterExamples());

				}

			} else if (state.generation > 0) {
							
				if (!perfectFound) {
					try {
						verifyPopulation(individuals, state.generation % verificationFrequency == 0, maxTestsPerGeneration,
								verificationNumThreads);
					} catch (InterruptedException e) {
						//////System.out.println("Got here");
						SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
						//st.
						st.setInterrupted(true);
					}

					for (Individual individual : individuals) {
						individual.evaluated = false;
					}
					
					if (!silent) {
						System.out.println("Negative Examples: " + ceManager.countNegatives());
						System.out.println("Positive Examples: " + ceManager.countPositives());
						System.out.println("ICE Examples: " + ceManager.countImplications());
					}
					
				}

			}

			/*if (synthType.equals("predicate")) {
				////System.out.println("Positive Tests: " + ceManager.countPositives());
				////System.out.println("Negative Tests: " + ceManager.countNegatives());
			} else {
				////System.out.println("Total Tests: " + ceManager.countCounterExamples());
			}*/
		//}

		if (Thread.interrupted()) {
			SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
			st.setInterrupted(true);
		}
    	
	}

    
}
