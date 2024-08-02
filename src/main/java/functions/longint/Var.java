package functions.longint;

import datatypes.LongData;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ecjSimple.SimpleEvolutionStateWithVerification;
import evoSynthesis.CLIAProblem;

@SuppressWarnings("serial")
public class Var extends GPNode {

	int index = -1;
	public String toString() { return "var"+ (index+1) + ";"; }

    public int expectedChildren() { return 0; }
    
	@Override
	public void resetNode(EvolutionState state, int thread) {
		SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
		//index = state
		index = state.random[thread].nextInt(st.getVerifier().acquireSizeOfFunctionVariablesList());

	}
	
    @Override
    public void eval(final EvolutionState state,
            final int thread,
            final GPData input,
            final ADFStack stack,
            final GPIndividual individual,
            final Problem problem) {
    	LongData rd = ((LongData)(input));
    	rd.x = ((CLIAProblem)problem).currentInputs[index];
    }
}
