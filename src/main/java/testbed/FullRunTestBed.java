package testbed;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import benchmark.Benchmark;
import branchwisePredicateSynthesis.BranchwisePredicateSynthesis;
import evoSynthesis.GPPredicateSynthesizer;
import synthesizer.SynthesisParameters;
import synthesizer.Synthesizer;

public class FullRunTestBed {

	public static void runExperiments(String outputName) throws Exception {
		
		//String directory = "src/main/resources/SMTBenchmarks/";


		String directory = "src/main/resources/EasyInvariantBenchmarks/";

		ArrayList<String> benchmarkNames = new ArrayList<>();
		File[] files = new File(directory).listFiles();
		for (File file : files) {
			benchmarkNames.add(file.getPath());
		}

		int numTrials = 1;
		int start = 1;

		String paramFile = "src/main/resources/booleanchildsilent.params";

		int sz = benchmarkNames.size();
		String unsolved = "";
		
		// int sz = 5;

		for (int i = start; i <= numTrials; i++) {
			//String results = "Benchmark,Successful,Time Taken,Program Found,Program Length\n";

			//System.out.println("Number of benchmarks to synthesize: " + sz);
			for (int j = 0; j < sz; j++) {

				String benchmarkName = benchmarkNames.get(j);
				//System.out.println(benchmarkName);
				//if (!benchmarkName.contains("93.c.smt")) {
					//continue;
				//}
				System.out.println("Solving " + benchmarkName);
				Benchmark benchmark = Benchmark.parseBenchmark(benchmarkName);
				
				Synthesizer predicateSynthesizer = new GPPredicateSynthesizer(paramFile,benchmark);
				SynthesisParameters sp = new SynthesisParameters();
				sp.setMaxThreads(1);
				//sp.setSkipToRepair(true);
				//sp.setTimeout(2);
				//SynthesisResult result = SynthesisMethods.CEGIS(partialsSynthesizer, benchmark);
				BranchwisePredicateSynthesis job = new BranchwisePredicateSynthesis();
				
				//Right, they each get 5 minutes, we look and see what was solved later this evening
				int timeout = 5;
				Instant currentStart = Instant.now();
				Instant currentEnd = null;
				boolean failed = false;
				while (!job.isSynthesisFinished()) {
					job.run(benchmark, predicateSynthesizer, true, "CDGP");
					currentEnd = Instant.now();
					if (Duration.between(currentStart, currentEnd).toMinutes() >= timeout) {
						failed = true;
						break;
					}
				}

				
				//if (!failed) {
					/*System.out.println("We win?");
					System.out.println(job.getCorrectMapping());

					Verifier ver = new Verifier(benchmark);

					VerificationResult vr = ver.verifySanity(job.getCorrectMapping());

					if (vr.getStatus() == Status.UNSATISFIABLE) {
						System.out.println("WE WIN!");
					}*/


				if (failed) {
					unsolved += benchmarkName + "\n";
				} else {
					String program = job.getCorrectMapping();
					
					for (int k = 0; k < benchmark.getFunctionVariables().length; k++) {
						program = program.replace("var" + (k+1)+";", benchmark.getFunctionVariables()[k]);
					}
					System.out.println(program);
					System.out.println("Elapsed " + Duration.between(currentStart, currentEnd).toMinutes());
				}
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputName));
				writer.write(unsolved);

				writer.close();
			}

		}
	}
	public static void main(String[] args) throws Exception {
		
		runExperiments("unsolved.txt");
	}

}
