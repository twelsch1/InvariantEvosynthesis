package functions.longint;

//import java.util.Random;

import datatypes.LongData;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import ec.util.Code;
import ec.util.DecodeReturn;
import ecjSimple.SimpleEvolutionStateWithVerification;

@SuppressWarnings("serial")
public class Ephemeral extends ERC {

	long value;
	
	public String toStringForHumans() { return "" + value; }
	
    @Override
    public void eval(final EvolutionState state,
            final int thread,
            final GPData input,
            final ADFStack stack,
            final GPIndividual individual,
            final Problem problem) {
    	((LongData)(input)).x = value;
    }

	@Override
	public void resetNode(EvolutionState state, int thread) {
		
		
		 SimpleEvolutionStateWithVerification st = (SimpleEvolutionStateWithVerification) state;
		
		int coinFlip = state.random[thread].nextInt(2);
		if (coinFlip == 1 || coinFlip == 0) {	 
		int idx = state.random[thread].nextInt(st.getConstants().length);

			value = st.getConstants()[idx];
		} else {
			value = state.random[thread].nextInt(20);
			
			coinFlip = state.random[thread].nextInt(2);
			
			if (coinFlip == 1) {
				value *= -1;
			}
		}
		
		/*
		coinFlip = state.random[thread].nextInt(2);
		
		if (coinFlip == 1) {
			value *= -1;
		}*/

	}

	@Override
	public boolean nodeEquals(GPNode node) {
		return (node.getClass() == this.getClass() && ((Ephemeral)node).value == value); 
	}

	@Override
	public String encode() {
		return Code.encode(value);
	}
	
	public boolean decode(DecodeReturn dret) {
		int pos = dret.pos;
		String data = dret.data;
		Code.decode(dret);
		if (dret.type != DecodeReturn.T_INT) // uh oh! Restore and signal error.
		{ dret.data = data; dret.pos = pos; return false; }
		value = (int) dret.l;
		return true;
		}

}

