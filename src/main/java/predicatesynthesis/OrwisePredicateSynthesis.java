package predicatesynthesis;

import benchmark.Benchmark;
import predicatesynthesis.helpers.Reduction;
import com.microsoft.z3.Status;
import synthesizer.SynthesisResult;
import synthesizer.Synthesizer;
import verification.VerificationCallParameters;
import verification.VerificationException;
import verification.VerificationResult;
import verification.Verifier;

import java.util.ArrayList;

/**
 * 
 * @author Thomas Welsch
 *
 */
public class OrwisePredicateSynthesis {

	//we should restore to 20 at some point, but let's check it at 5 for now.
/*	private int restrictionsCap = 20;

	private int resetCounter = 0;
	private int maxBeforeReset = 1000000;*/

	private ArrayList<String> restrictions = new ArrayList<>();
	/**
	 * Set to true when we have a correct mapping to targetPartial.
	 */
	private boolean synthesisFinished = false;

	private String correctMapping = "";

	private ArrayList<String> clauses = new ArrayList<>();

	private int numRuns = 0;

	private int firstPriority = 0;



	public int getFirstPriority() {
		return firstPriority;
	}

	public void setFirstPriority(int firstPriority) {
		this.firstPriority = firstPriority;
	}



	public void run(Benchmark benchmark, Synthesizer predicateSynthesizer, boolean verifySuccess,
String branchwiseMode) throws Exception {
		Verifier verifier = new Verifier(benchmark);
		//setUpVerifier(verifier);
		runOrWiseSynthesis(verifier, predicateSynthesizer, verifySuccess);
		
	}


	//OK let's think this through, let's say we can describe it as
	// A OR B OR C
	//We start with A AND X, what if we negate then?
	//Well, we wind up with ¬(A AND X) which is the same as ¬A OR ¬X
	//So, if we assert the negation we are saying that ¬A OR ¬X must be true. Well, if we do it write, this
	//SHOULD eliminate A.

	//As implemented, false almost always is an invariant after adding a restriction. So...
	//yeah I don't know, this needs to be revisited. It's sound, anyway, but it isn't very useful.
	public void runOrWiseSynthesis(Verifier verifier, Synthesizer predicateSynthesizer, boolean verifySuccess) throws Exception {
		//For now, forget about the restricitons cap.
		/*
		if (restrictions.size() > restrictionsCap) {
			restrictions.remove(1);
		}*/

		do {

				//This bit is kinda silly but whatever.
				String[] localRestrictions = localRestrictionsAsArray();

				//Note in the clauses are presented as being included in localRestrictions directly. Programatically
				//this is not the case, but the outcome is logically equivalent.
				if (!clauses.isEmpty()) {
					verifier.setClauses(clauses.toArray(new String[clauses.size()]));
				}
				verifier.setLocalRestrictions(localRestrictions);
				System.out.println("Restrictions size is : " + restrictions.size());
				//System.out.println("Inductive Lemmas found: " + positiveMappings.size());

					// Run synthesis with latest Restrictions+PositiveMappings

					SynthesisResult sr = predicateSynthesizer.synthesize(verifier);

					if (sr.isSuccessful()) {

						//	System.out.println("Had some success " + sr.getProgramFound());

						// Verify that synthesis actually was successful if required
						if (verifySuccess) {
							//////System.out.println(verifier.getTargetPartial());
							VerificationResult vr = verifier.verify(sr.getProgramFound());

							if (vr.getStatus() != Status.UNSATISFIABLE) {
								////System.out.println(sr.getProgramFound());
								throw new Exception(
										"Synthesizer returned successful SynthesisResult when programFound is incorrect");
							}
						}
						System.out.println("Adding clause " + sr.getProgramFound());
						clauses.add(sr.getProgramFound());


						if (!restrictions.isEmpty()) {
							restrictions.removeLast();
						}
						//	resetRestrictions();
					} else {
						// add the most recently synthesized predicate to restrictions, then
						// check that we still have reachability. If unreachable, and the state
						// of the job is unchanged from the previous loop. Note the paper describes this slightly
						//differently i.e. there it was never added but rather checked beforehand. This change
						//is logically equivalent and is slightly cleaner in the context of the Verifier
						//treating the restrictions as an array.

						//String extracted = ExtractInductiveLemmas.extractFirstComp(sr.getProgramFound());
						restrictions.add("(not " + sr.getProgramFound() + ")");


						System.out.println("Attempting restriction add " + sr.getProgramFound());
						//We need to revisit this logic for the orWise case.
						/*
						if (!isUseful(verifier, buildLocalRestrictions())) {
							System.out.println("Aborted restriction add");
							restrictions.removeLast();
							resetCounter ++;

							if (resetCounter >= maxBeforeReset) {
								resetRestrictions();
								resetCounter = 0;
							}
						} else {
							resetCounter = 0;
						}*/

						//System.out.println("Check Complete");

						return;
					}


			verifier.setClauses(null);
			verifier.setLocalRestrictions(null);

		} while (checkProgamIncorrect(verifier));

		// we have found a CompleteMapping, we set this as correctMapping and signal success.
		correctMapping = buildInvariantCandidateFromClauses();
		this.synthesisFinished = true;
	}


	//return true when program is incorrect.
	private boolean checkProgamIncorrect(Verifier verifier) {
		if (clauses.isEmpty()) {
			return true;
		}

		return !verifier.isProgramCorrect(buildInvariantCandidateFromClauses());
	}

	private String buildInvariantCandidateFromClauses() {
		String retVal = "";
		if (clauses.size() == 1) {
			retVal = clauses.get(0);
		} else {
			//////System.out.println("Multiple clauses were needed");
			String closingParens = "";
			for (int i = 0; i < clauses.size() - 1; i++) {
				String clause = clauses.get(i);
				retVal += "(or " + clause + " ";
				closingParens += ")";
			}
			retVal += clauses.getLast() + closingParens;
		}

		return retVal;
	}	


	private String[] localRestrictionsAsArray() {
		ArrayList<String> extraAssertions = new ArrayList<>();
		extraAssertions.addAll(restrictions);
		return extraAssertions.toArray(new String[extraAssertions.size()]);
	}

	private void resetRestrictions() {
		restrictions.clear();
	}


	
	private boolean isUseful(Verifier verifier, String[] extraAssertions) throws VerificationException {
		VerificationCallParameters vcp = new VerificationCallParameters();
		vcp.setTimeout(500);
		verifier.setLocalRestrictions(extraAssertions);
		VerificationResult vr = verifier.verify("true", vcp);
		if (vr.getStatus() == Status.UNSATISFIABLE ) {
			System.out.println("Interesting");
			return false;
		}
		
		if (vr.getStatus() == Status.UNKNOWN) {
			System.out.println("Timed out on isUseful check");
			return false;
		}
		vr = verifier.verify("false", vcp);
		if (vr.getStatus() == Status.UNSATISFIABLE ) {
			return false;
		}
		
		if (vr.getStatus() == Status.UNKNOWN) {
			System.out.println("Timed out on isUseful check");
			return false;
		}

		return true;
	}

	public String getCorrectMapping() {
		return correctMapping;
	}

	public boolean isSynthesisFinished() {
		return synthesisFinished;
	}


	public int getNumRuns() {
		return numRuns;
	}
	
	
	

}
