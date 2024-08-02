package synthesizer;

/**
 * 
 * @author Thomas Welsch
 *
 */
public class SynthesisParameters {
	
	//These are the default values, only constructor is the default one

	private long timeout = 60;
	private int maxThreads = 1;
	private boolean verifySuccess = true;
	private String branchwiseMode = "RBPS";

	


	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}



	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public boolean isVerifySuccess() {
		return verifySuccess;
	}

	public void setVerifySuccess(boolean verifySuccess) {
		this.verifySuccess = verifySuccess;
	}

	public String getBranchwiseMode() {
		return branchwiseMode;
	}

	public void setBranchwiseMode(String branchwiseMode) {
		this.branchwiseMode = branchwiseMode;
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
