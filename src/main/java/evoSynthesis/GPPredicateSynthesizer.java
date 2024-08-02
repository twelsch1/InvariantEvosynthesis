package evoSynthesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import benchmark.Benchmark;
import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ecjSimple.SimpleEvolutionStateWithVerification;
import fitness.VerifiableFitness;
import synthesizer.SynthesisResult;
import synthesizer.Synthesizer;
import verification.TestExample;
import verification.Verifier;

public class GPPredicateSynthesizer extends Synthesizer {
	
	private String paramFile;
	private Benchmark benchmark;
	private String[] runConfig;
	private int maxAttempts = 1000;
	private int timeout = 5;
	
	
	public GPPredicateSynthesizer(String paramFile, Benchmark benchmark) {
		this.paramFile = paramFile;
		this.benchmark = benchmark;
		String[] constantsStrings = new String[benchmark.getConstants().length];
		for (int i = 0; i < benchmark.getConstants().length; i++) {
			constantsStrings[i] = Integer.toString(benchmark.getConstants()[i]);
		}
 		
		String joinedConstants = String.join(",,,",constantsStrings);
		runConfig = new String[] { Evolve.A_FILE, paramFile, 
				"-p", ("jobs=" + 1), "-p", ("constantsString=" + joinedConstants)};
	}
	
	public GPPredicateSynthesizer(String paramFile, Benchmark benchmark, int timeout) {
		this.paramFile = paramFile;
		this.benchmark = benchmark;
		String[] constantsStrings = new String[benchmark.getConstants().length];
		for (int i = 0; i < benchmark.getConstants().length; i++) {
			constantsStrings[i] = Integer.toString(benchmark.getConstants()[i]);
		}
 		
		String joinedConstants = String.join(",,,",constantsStrings);
		runConfig = new String[] { Evolve.A_FILE, paramFile, 
				"-p", ("jobs=" + 1), "-p", ("constantsString=" + joinedConstants)};
		
		this.timeout = timeout;
		
	}
	
	private ParameterDatabase setupDatabase(String paramFile, String[] runConfig, Benchmark benchmark) {
		ParameterDatabase dbase = null;
		try {
			dbase = new ParameterDatabase(new File(paramFile), runConfig);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String evalthreadsString = dbase.getString(new Parameter("evalthreads"), new Parameter("evalthreads"));
		int evalThreads = 1;
		if (evalthreadsString != null) {
			evalThreads = Integer.parseInt(evalthreadsString);
		}
		for (int i = 0; i < evalThreads; i++) {
			dbase.set(new Parameter("seed." + i), "time");
		}

		//Right now we assume the types are all int, but we'll change that here in a bit.
		ParamsHelper.setFunctionSet(dbase, benchmark.getFunctionVariables(), benchmark.getFunctionVariableTypes());
		
		
		return dbase;
	}

	@Override
	public SynthesisResult synthesize(Verifier verifier) {

		//boolean synthSuccessful = false;
		
		//////System.out.println("Running GP");
		ParameterDatabase dbase = setupDatabase(paramFile,runConfig,benchmark);
		
		ArrayList<TestExample> ces = null;
		int initFrequency = 2;
		
		Instant start = Instant.now();
		
		for (int i = 0; i < maxAttempts; i++) {
			
		//System.out.println("GP Attempt " + (i+1));
		SimpleEvolutionStateWithVerification evaluatedState = (SimpleEvolutionStateWithVerification) Evolve.initialize(dbase, 0);
		//evaluatedState.numGenerations
		
		int freq = Integer.MAX_VALUE;
		
		//System.out.println(freq)
		
		if (i == 0) {
			freq = initFrequency;
		}
		
		evaluatedState.startFresh(verifier, benchmark.getConstants(), ces, freq);


		int result = EvolutionState.R_NOTDONE;
		while( result == EvolutionState.R_NOTDONE ) {
			result = evaluatedState.evolve();
		}
		
    	ArrayList<Individual> individuals = new ArrayList<>();
	    individuals.addAll(evaluatedState.population.subpops.get(0).individuals);	    
		if (result == EvolutionState.R_SUCCESS) {
			return new SynthesisResult(true, extractCorrectProgram(individuals));
		}
		
		Instant end = Instant.now();
		if (Duration.between(start, end).toMinutes() >= timeout) {
			return new SynthesisResult(false, "");
		}
		
		
		if (i == maxAttempts-1) {
		    Collections.sort(individuals);
			GPIndividual ind = (GPIndividual) individuals.get(0);
			return new SynthesisResult(false,ind.trees[0].child.makeLispTree());
		}
		
		ces = evaluatedState.getPreviousCounterExamples();
		
		Evolve.cleanup(evaluatedState);
		}
		
		
		//shouldn't be reached
		return null;
		

	}
	
	
	private String extractCorrectProgram(ArrayList<Individual> individuals) {
		String retVal = "";
		for (int i = 0; i < individuals.size(); i++) {
			GPIndividual ind = (GPIndividual) individuals.get(i);
			VerifiableFitness f = (VerifiableFitness) ind.fitness;
			if (f.isIdealFitness()) {
				retVal = ind.trees[0].child.makeLispTree();
				break;

			}

		}
		
		return retVal;
	}

}
