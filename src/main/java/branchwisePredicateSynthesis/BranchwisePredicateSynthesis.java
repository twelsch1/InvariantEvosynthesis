package branchwisePredicateSynthesis;

import java.util.ArrayList;

import com.microsoft.z3.Status;

import benchmark.Benchmark;
import branchwisePredicateSynthesis.helpers.ExtractInductiveLemmas;
import branchwisePredicateSynthesis.helpers.Reduction;
import synthesizer.SynthesisResult;
import synthesizer.Synthesizer;
import verification.VerificationCallParameters;
import verification.VerificationException;
import verification.VerificationResult;
import verification.Verifier;

/**
 * 
 * @author Thomas Welsch
 *
 */
public class BranchwisePredicateSynthesis {

	//we should restore to 20 at some point, but let's check it at 5 for now.
	private int restrictionsCap = 20;
	
	private int resetCounter = 0;
	private int maxBeforeReset = 1000000;

	private ArrayList<String> restrictions = new ArrayList<>();
	private ArrayList<String> positiveMappings = new ArrayList<>();

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


	
	public BranchwisePredicateSynthesis() {
		restrictions.add("true");
		//System.out.println("We added true");
	}

	public void run(Benchmark benchmark, Synthesizer predicateSynthesizer, boolean verifySuccess,
String branchwiseMode) throws Exception {
		Verifier verifier = new Verifier(benchmark);
		//setUpVerifier(verifier);
		if (branchwiseMode.equals("RBPS")) {
			runRBPS(verifier, predicateSynthesizer, verifySuccess);
		} else {

			
			SynthesisResult sr = predicateSynthesizer.synthesize(verifier);
			if (sr.isSuccessful()) {
				//verifier
				//System.out.println("Running Verification Check");
				/*VerificationResult vr = verifier.verify(sr.getProgramFound());
				

				if (vr.getStatus() != Status.UNSATISFIABLE) {
					////System.out.println(sr.getProgramFound());
					throw new Exception(
							"Synthesizer returned successful SynthesisResult when programFound is incorrect");
				}*/
				
				////System.out.println("FML");
				correctMapping = sr.getProgramFound();
				synthesisFinished = true;
			}
		}
		
	}
	
	public void runRBPS(Verifier verifier, Synthesizer predicateSynthesizer, boolean verifySuccess) throws Exception {




		//This way we don't remove the true value, I wonder if that was messing with things...
		if (restrictions.size() > restrictionsCap) {
			restrictions.remove(1);
		}

		do {
			while (!restrictions.isEmpty()) {
				String[] localRestrictions = buildLocalRestrictions();
				
				//Note in the clauses are presented as being included in localRestrictions directly. Programatically
				//this is not the case, but the outcome is logically equivalent.
				if (!clauses.isEmpty()) {
					verifier.setClauses(clauses.toArray(new String[clauses.size()]));
				}
				verifier.setLocalRestrictions(localRestrictions);
				verifier.setPositiveMappings(positiveMappings.toArray(new String[positiveMappings.size()]));
				
				System.out.println("Restrictions size is : " + restrictions.size());
				System.out.println("Inductive Lemmas found: " + positiveMappings.size());
				
				
				// if with the local restrictions true realizes, just remove and move onto next.
				if (verifier.isProgramCorrect("true")) {
					//System.out.println("Restriction wasn't useful, restrictions size is now " + (restrictions.size()-1));
					restrictions.remove(restrictions.size() - 1);
				} else {

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
						
						// reduce and add to positive mappings, then remove last restriction
						String reducedPred = Reduction.reduceToClausalPositiveMapping(verifier, sr.getProgramFound(),
								buildLocalRestrictions());
						System.out.println("Reduced pred " + reducedPred);
						String extracted = ExtractInductiveLemmas.extractFirstComp(reducedPred);
						//System.out.println("Adding Extracted " + extracted);
						positiveMappings.add(reducedPred);
						//positiveMappings.add(extracted);
						//System.out.println("Removing restriction " + restrictions.get(restrictions.size()-1));
						restrictions.remove(restrictions.size() - 1);
						resetCounter = 0;
					//	resetRestrictions();
					} else {
						// add the most recently synthesized predicate to restrictions, then
						// check that we still have reachability. If unreachable, and the state
						// of the job is unchanged from the previous loop. Note the paper describes this slightly
						//differently i.e. there it was never added but rather checked beforehand. This change
						//is logically equivalent and is slightly cleaner in the context of the Verifier
						//treating the restrictions as an array.
						
						//String extracted = ExtractInductiveLemmas.extractFirstComp(sr.getProgramFound());
						restrictions.add(sr.getProgramFound());
						
						//restrictions.add(extracted);
					//	System.out.println("Adding restriction");
					//	System.out.println("Checking reachability");
						//System.out.println("Possible restriction " + sr.getProgramFound());
						/*for (String s : ExtractInductiveLemmas.extractInductiveLemmas(verifier, sr.getProgramFound())) {
							if (!positiveMappings.contains(s)) {
								System.out.println("Adding from extraction " + s);
								positiveMappings.add(s);
							}
						}*/
						
						System.out.println("Attempting restriction add " + sr.getProgramFound());
						
						if (!isUseful(verifier, buildLocalRestrictions())) {
							System.out.println("Aborted restriction add");
							restrictions.remove(restrictions.size() - 1);
							resetCounter ++;
							
							if (resetCounter >= maxBeforeReset) {
								resetRestrictions();
								resetCounter = 0;
							}
						} else {
							resetCounter = 0;
						}
						//System.out.println("Check Complete");
						
						return;
					}
				}
			}
			
			// we have built at least one clause successfully, add it to the list of
			// clauses, clear positiveMappings, and reset restrictions.
			clauses.add(buildClause());
			
			numRuns = 0; //reset number of runs back to 0, this makes it so that jobs where clauses have been found
			//are given significant priority.
			
			//reset the job for next loop and clear verifier of local restrictions
			positiveMappings.clear();
			resetRestrictions();
			verifier.setClauses(null);
			verifier.setLocalRestrictions(null);
			verifier.setPositiveMappings(null);
			
			System.out.println("Building candidate");

		} while (!verifier.isProgramCorrect(buildInvariantCandidateFromClauses()));

		// we have found a CompleteMapping, we set this as correctMapping and signal success.
		correctMapping = buildInvariantCandidateFromClauses();
		this.synthesisFinished = true;
		
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
			retVal += clauses.get(clauses.size() - 1) + closingParens;
		}

		return retVal;
	}	

	private String buildClause() {
		String retVal = "";

		if (positiveMappings.size() == 0) {
			return "true";
		} 
		else if (positiveMappings.size() == 1) {
			retVal = positiveMappings.get(0);
		} else {

			String closingParens = "";
			for (int i = 0; i < positiveMappings.size() - 1; i++) {
				String pm = positiveMappings.get(i);
				retVal += "(and " + pm + " ";
				closingParens += ")";
			}
			retVal += positiveMappings.get(positiveMappings.size() - 1) + closingParens;
		}

		return retVal;
	}

	private String[] buildLocalRestrictions() {
		ArrayList<String> extraAssertions = new ArrayList<>();
		extraAssertions.addAll(restrictions);
		extraAssertions.addAll(positiveMappings);
		return extraAssertions.toArray(new String[extraAssertions.size()]);
	}

	private void resetRestrictions() {
		restrictions.clear();
		restrictions.add("true");
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
