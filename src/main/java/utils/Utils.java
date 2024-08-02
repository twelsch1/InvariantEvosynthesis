package utils;

/**
 * A utilities class containing string manipulation, modification and extraction methods.
 * @author Thomas Welsch
 *
 */
public class Utils {
	




/**
 * Takes the fullProgram String and substrings it from startIndex, then checks this substring to see if the 
 * targetOperator is present. If it is, we determine where the boolean operator begins as a function to where it ends
 * i.e. the opening and closing parentheses. 
 * @param fullProgram The program to check for next predicate instance
 * @param targetOperator The boolean operator (e.g. =>, <) we are searching for
 * @param startIndex The index in fullProgram from which to start the search
 * @return If the targetOperator is found, an int array containing the indices of the opening and closing brackets of this operator. If it not
 * found returns null.
 */
public static int[] findNextPredicateInstance(String fullProgram, String targetOperator, int startIndex) {
		
		//The rest of the program after the startIndex
		String remainingProgram = fullProgram.substring(startIndex);
		
		//searches for the targetOperator, returns the index of the first instance it is found
		int targetIndex = remainingProgram.indexOf(targetOperator);
		

		//if the targetOperator isn't found, return null. This is used as a signal to
		//tell that we no longer need to check for this operator
		if (targetIndex == -1) {
			return null;
		}
		
		
		
		//use this to find the enclosing parentheses e.g. goes from >= back to (>= or ( >=, etc.
		for (int i = targetIndex; i >= 0; i-- ) {
			if (remainingProgram.charAt(i) == '(') {
				targetIndex = i;
				break;
			}
		}
		
		
		int enclosingIndex = -1;
		int parenCounter = 0;
		//The parenCounter ensures that we don't quit until we reach the closing bracket that matches the initial opening bracket
		//at the targetIndex.
		for (int i = targetIndex; i < remainingProgram.length(); i++) {
			if (remainingProgram.charAt(i) == '(') {
				parenCounter++;
			} else if (remainingProgram.charAt(i) == ')') {
				parenCounter--;
			}
			
			//when encountered, set the enclosingIndex and end the loop.
			if (parenCounter == 0) {
				enclosingIndex = i;
				i = remainingProgram.length();
			}
			
		}
		
		
		//return the indices in fullProgram where function begins and ends
		int[] retVal = new int[2];
		retVal[0] = startIndex+targetIndex;
		retVal[1] = startIndex+enclosingIndex;
		
		return retVal;
	}



/**
 * Extracts the first function OR constant/variable from the programSubstring. 
 * @param programSubstring The substring of a program that we are checking.
 * @return If the programSubstring starts with an open bracket, will return the remaining string up to and inclusive of the matching
 * closing bracket. If it does not start with an open bracket, it will build out the string until white space is encountered.
 */
public static String extractNextFunction(String programSubstring) {
	//if program is not well formed this function can get stuck in an infinite loop, so be careful
	
	
	//If the first character is not an open bracket, go until we find a space.
	//This can be encountered for constants and variables.
	if (programSubstring.charAt(0) != '(') {
		int x = 0;
		StringBuilder buffer = new StringBuilder("");
		//build out string until space is encountered or we have reached closing bracket of enclosing function
		// e.g. on (ite (>= x y) x y) this would be encountered on the closing bracket after y. 
		//We don't want to include either of these, so we break and return what we've got.
		//The final stopping condition is for if the whole string is itself a constant/variable. 
		//Not encountered in the way our internal functions currently
		//use this function i.e. in the context of ite's, but plausibly could be if used in other circumstances.

		while (x < programSubstring.length() && programSubstring.charAt(x) != ' ' && programSubstring.charAt(x) != ')') {


			//append most recent character and move index up 1
			buffer.append(programSubstring.charAt(x));
			x++;

		}
		//return the built out string
		return buffer.toString();
	}
	
	//If we have reached this point, first char was an open bracket, set this as the start of the buffer
	//and start parenCounter and index at 1
	StringBuilder retValBuffer = new StringBuilder("(");
	
	//represents (number of open brackets) - (number of closed brackets), starts at 1 because we add in the open bracket above
	int parenCounter = 1;
	
	//index starts at 1, as we've already checked first counter
	int i = 1;
	
	//While there are still unclosed brackets keep building out the string
	while (parenCounter > 0) {
		//get most recent character and append
		char c = programSubstring.charAt(i);
		retValBuffer.append(c);
		//if we encounter a closing decrement parenCounter and if we encounter a closing increment
		if (c == ')') {
			parenCounter--;
		} else if (c == '(') {
			parenCounter++;
		}
		
		
		i++;
	}
	
	return retValBuffer.toString();
}


public static String scanToSpace(String str) {
	StringBuffer buf = new StringBuffer();
	
	for (int i = 0; i < str.length(); i++) {
		char c = str.charAt(i);
		if (c == ' ') {
			return buf.toString();
		} 
		
		buf.append(c);
	}
	
	return buf.toString();
}



}
