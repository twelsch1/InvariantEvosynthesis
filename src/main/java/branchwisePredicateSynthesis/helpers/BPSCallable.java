package branchwisePredicateSynthesis.helpers;

import benchmark.Benchmark;
import branchwisePredicateSynthesis.BranchwisePredicateSynthesis;
import synthesizer.Synthesizer;

import java.util.concurrent.Callable;

/**
 * 
 * @author Thomas Welsch
 *
 */
public class BPSCallable implements Callable<BPSJobResult> {

	private BranchwisePredicateSynthesis job;
	private Synthesizer synthesizer;
	private Benchmark benchmark;
	private boolean verifySuccess;
	private String branchwiseMode;

	public BPSCallable(BranchwisePredicateSynthesis job, Synthesizer synthesizer,
			Benchmark benchmark, String globalConstraintsString, boolean verifySuccess, String branchwiseMode) {
		this.job = job;
		this.synthesizer = synthesizer;
		this.benchmark = benchmark;
		this.verifySuccess = verifySuccess;
		this.branchwiseMode = branchwiseMode;
	}

	@Override
	public BPSJobResult call() throws Exception {

		job.run(benchmark, synthesizer,verifySuccess, branchwiseMode);
		return new BPSJobResult(job);
	}

}
