package functions.longint;

import datatypes.LongData;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

@SuppressWarnings("serial")
public class LTE extends GPNode {

	public String toString() {
		return "<=";
	}

	public int expectedChildren() {
		return 2;
	}
	
	

	public void eval(final EvolutionState state, final int thread, final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {
		long left, right;
		LongData rd = ((LongData) (input));

		children[0].eval(state, thread, input, stack, individual, problem);
		left = rd.x;
		children[1].eval(state, thread, input, stack, individual, problem);
		right = rd.x;
		if (left <= right) {
			rd.x = 1;
		} else {
			rd.x = 0;
		}
	}
}
