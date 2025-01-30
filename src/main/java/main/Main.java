package main;

import benchmark.Benchmark;
import predicatesynthesis.BranchwisePredicateSynthesis;
import evosynthesis.GPPredicateSynthesizer;
import predicatesynthesis.OrwisePredicateSynthesis;
import synthesizer.SynthesisParameters;
import synthesizer.Synthesizer;
import verification.Verifier;

public class Main {

	
	public static void main(String[] args) throws Exception {
		/*if (args.length < 1) {
			//System.out.println("No benchmark file provided");
			System.exit(0);
		}*/
		
		String predSynthFile = "src/main/resources/booleanchild.params";
	
		
		/*if (args.length > 0) {
			predSynthFile = args[1];
		}*/
		//String benchmarkFile = args[0];
		//String benchmarkFile = "src/main/resources/SMTBenchmarks/6.c.smt";
		
		//String benchmarkFile = "src/main/resources/EasyInvariantBenchmarks/jmbl_ex11_vars-new.sl";
		//String benchmarkFile = "src/main/resources/EasyInvariantBenchmarks/jmbl_hola.20.sl";
		//String benchmarkFile = "src/main/resources/SolvedHardInvariantBenchmarks/cars.sl";
		//nl-4 caused an issue when trying to retrieve initial examples, was taking a long time and seemed to not converge on 6 and 7. Possibly,
		//nl-4 is trivial but idk. Note they failed to solve two of these, and possibility of unsat.
		//THis is an interesting topic for later.
		String benchmarkFile = "src/main/resources/NLBenchmarks/nl-6.c.smt";
		
		Benchmark benchmark = Benchmark.parseBenchmark(benchmarkFile);

		Synthesizer predicateSynthesizer = new GPPredicateSynthesizer(predSynthFile,benchmark, 60);
		SynthesisParameters sp = new SynthesisParameters();
		sp.setMaxThreads(1);
		//sp.setSkipToRepair(true);
		//sp.setTimeout(2);
		//SynthesisResult result = SynthesisMethods.CEGIS(partialsSynthesizer, benchmark);
		BranchwisePredicateSynthesis job = new BranchwisePredicateSynthesis();
		//OrwisePredicateSynthesis job = new OrwisePredicateSynthesis();
		while (!job.isSynthesisFinished()) {
			job.run(benchmark, predicateSynthesizer, true, "RBPS");
		}


		System.out.println("We win?");
		//System.out.println(job.getCorrectMapping());
		
		String program = job.getCorrectMapping();
		
		for (int i = 0; i < benchmark.getFunctionVariables().length; i++) {
			program = program.replace("var" + (i+1)+";", benchmark.getFunctionVariables()[i]);
		}
		System.out.println(program);
		
		Verifier ver = new Verifier(benchmark);
		
		
		//VerificationResult vr = ver.verifySanity(program);
		
		if (ver.verifySanity(program)) {
			System.out.println("WE WIN!");
		}
		
	}
}
