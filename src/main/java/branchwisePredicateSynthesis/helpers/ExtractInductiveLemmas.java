package branchwisePredicateSynthesis.helpers;

import java.util.HashSet;

import utils.Utils;
import verification.Verifier;


public class ExtractInductiveLemmas {



	public static HashSet<String> extractInductiveLemmas(Verifier verifier, String programToModify) {
		
//		////System.out.println("Removing cons and tauts");
		String program = programToModify;
		String[] operators = {">", "<", "<=", ">=", "=", "distinct"};
		
		HashSet<String> inductiveLemmas = new HashSet<>();
		
		//we check every comparison operator. If there were boolean variables we'd need to check those as well, but currently no CLIA problems
		//in Sygus have these.
		for (int i = 0; i < operators.length; i++) {

			//j is the index we use to track where we are in the program String, starts at 0 to initially include whole String
			int j = 0;
			while (true) {
				//looks for the next instance of the operator, if one exists gives the start and end indices so we can replace,
				//if it is does not exist returns null and we break this inner loop 
				int[] nextPredicateInstance = Utils.findNextPredicateInstance(program, operators[i],
						j);

				if (nextPredicateInstance == null) {
					//operator wasn't found, so break this inner loop and move to the next operator
					break;
				} else {
					
					//Using the nextPredicateInstance array, we get the substrings from the full program that are immediately before the
					//operator function and immediately after.
					String predicate = program.substring(nextPredicateInstance[0], nextPredicateInstance[1]+1);
					//System.out.println("Checking program  " + predicate);
					if (verifier.verifyIsInductiveLemma(predicate)) {
						//System.out.println("Adding program " + predicate);
						inductiveLemmas.add(predicate);
					}
					
					j = nextPredicateInstance[1];
					

				}

			}
			

		}
		
		//////System.out.println("Escaped");
		return inductiveLemmas;
		
	}
	
	
	
public static String extractFirstComp(String programToExtract) {
		
//		////System.out.println("Removing cons and tauts");
		String program = programToExtract;
		String[] operators = {">", "<", "<=", ">=", "=", "distinct"};
	
		
		//we check every comparison operator. If there were boolean variables we'd need to check those as well, but currently no CLIA problems
		//in Sygus have these.
		for (int i = 0; i < operators.length; i++) {

			while (true) {
				//looks for the next instance of the operator, if one exists gives the start and end indices so we can replace,
				//if it is does not exist returns null and we break this inner loop 
				int[] nextPredicateInstance = Utils.findNextPredicateInstance(program, operators[i],
						0);

				if (nextPredicateInstance == null) {
					//operator wasn't found, so break this inner loop and move to the next operator
					break;
				} else {
					String predicate = program.substring(nextPredicateInstance[0], nextPredicateInstance[1]+1);
					return predicate;

					

				}

			}
			

		}
		
		//////System.out.println("Escaped");
		return null;
		
	}


}
