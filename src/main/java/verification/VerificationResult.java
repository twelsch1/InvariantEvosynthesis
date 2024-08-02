package verification;

import java.util.ArrayList;

import com.microsoft.z3.Status;

/**
 * A POJO used to convey the outcome of verification. Contains the Z3 status of the verification call, a CounterExample if one was obtained, and an Exception if one was encountered. 
 * @author Thomas Welsch
 *
 */
public class VerificationResult {

	/**
	 * The Z3 status obtained through verification, can be three values. UNSAT corresponds to a correct program, SAT incorrect with a CounterExample, and UNKNOWN that an Exception was
	 * encountered or error occurred during verification preventing ascertainment of correctness.
	 */
	private Status status;
	
	/**
	 * If status is SAT, a corresponding CounterExample is obtained that represents input values on which the program is correct and an output value that is correct on those inputs.
	 */
	private ArrayList<TestExample> counterExamples;
	
	/**
	 * If status is UNKNOWN and an Exception was encountered, said Exception is included 
	 */
	private Exception exception;
	
	
	/**
	 * Standard constructor, just sets internal private variables status and exception to reflect the parameters.
	 * @param status The Z3 status obtained through verification, can be three values. UNSAT corresponds to a correct program, SAT incorrect with a CounterExample, and UNKNOWN that an Exception was
	 * encountered or error occurred during verification preventing ascertainment of correctness.
	 * @param exception If status is UNKNOWN and an Exception was encountered, said Exception is included 
	 */
	public VerificationResult(Exception exception, Status status) {
		this.status = status;
		this.exception = exception;
	}
	
	/**
	 * Standard constructor, just sets internal private variables counterExample and status to reflect the parameters.
	 * @param counterExample If status is SAT, a corresponding CounterExample is obtained that represents input values on which the program is correct and an output value that is correct on those inputs.
	 * @param status The Z3 status obtained through verification, can be three values. UNSAT corresponds to a correct program, SAT incorrect with a CounterExample, and UNKNOWN that an Exception was
	 * encountered or error occurred during verification preventing ascertainment of correctness.
	 */
	public VerificationResult(Status status, ArrayList<TestExample> counterExamples) {
		this.counterExamples = counterExamples;
		this.status = status;
	}
	
	public String retrieveStatus() {
		if (this.status == Status.SATISFIABLE) {
			return "SAT";
		} else if(this.status == Status.UNSATISFIABLE) {
			return "UNSAT";
		} else {
			return "UNKNOWN";
		}
	}
	
	public Status getStatus() {
		return status;
	}

	public Exception getException() {
		return exception;
	}


	public ArrayList<TestExample> getCounterExamples() {
		return counterExamples;
	}

	
	
	
	

}
