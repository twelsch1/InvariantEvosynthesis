package functions.longint;

import datatypes.LongData;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

@SuppressWarnings("serial")
public class Or extends GPNode {

	public String toString() {
		return "or";
	}

	public int expectedChildren() {
		return 2;
	}

	
	public void eval(final EvolutionState state, final int thread, final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {

		
		LongData id = ((LongData) (input));

		children[0].eval(state, thread, input, stack, individual, problem);
		
		long lhs = id.x;
		children[1].eval(state, thread, input, stack, individual, problem);
		if (lhs == 1 || id.x == 1) {
			id.x = 1;
		} else {
			id.x = 0;
		}
	}

}
