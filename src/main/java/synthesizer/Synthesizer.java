package synthesizer;


import verification.Verifier;

/**
 * 
 * @author Thomas Welsch
 *
 */
public abstract class Synthesizer {
	public abstract SynthesisResult synthesize(Verifier verifier);
}
