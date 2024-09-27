package fitness;

import ec.gp.koza.KozaFitness;

public class VerifiableFitness extends KozaFitness {

	private static final long serialVersionUID = 1111476393375127150L;
	
	private boolean verified = false;
	
	private float negativeSum;
	private float positiveSum;
	
	@Override
	public boolean isIdealFitness() {
		return verified;
	}
	
	public void setVerified(boolean verified) {
		this.verified = verified;
	}



	public float getNegativeSum() {
		return negativeSum;
	}

	public void setNegativeSum(float negativeSum) {
		this.negativeSum = negativeSum;
	}

	public float getPositiveSum() {
		return positiveSum;
	}

	public void setPositiveSum(float positiveSum) {
		this.positiveSum = positiveSum;
	}


	
	
	
	
	
	
	
	

}
