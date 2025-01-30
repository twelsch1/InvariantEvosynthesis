package testbed;

import benchmark.Benchmark;
import predicatesynthesis.BranchwisePredicateSynthesis;
import evosynthesis.GPPredicateSynthesizer;
import predicatesynthesis.OrwisePredicateSynthesis;
import synthesizer.SynthesisParameters;
import synthesizer.SynthesisResult;
import synthesizer.Synthesizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class FullRunTestBed {

	public static void runExperiments(String outputName) throws Exception {
		
		String directory = "src/main/resources/SMTBenchmarks/";


		//String directory = "src/main/resources/NLBenchmarks/";

		ArrayList<String> benchmarkNames = new ArrayList<>();
		File[] files = new File(directory).listFiles();
		for (File file : files) {
			benchmarkNames.add(file.getPath());
		}

		int numTrials = 1;
		int start = 1;

		String paramFile = "src/main/resources/booleanchildsilent.params";
		//String paramFile = "src/main/resources/standardsilent.params";
		//String paramFile = "src/main/resources/lexicasechild.params";

		int sz = benchmarkNames.size();
		String unsolved = "";
		
		// int sz = 5;
		ArrayList<SynthesisResult> results = new ArrayList<>();
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
				//sp.setTimeout(5);
				//SynthesisResult result = SynthesisMethods.CEGIS(partialsSynthesizer, benchmark);
				BranchwisePredicateSynthesis job = new BranchwisePredicateSynthesis();
				//OrwisePredicateSynthesis job = new OrwisePredicateSynthesis();
				//Right, they each get 5 minutes, we look and see what was solved later this evening
				int timeout = 5;
				Instant currentStart = Instant.now();
				Instant currentEnd = null;
				boolean failed = false;
				while (!job.isSynthesisFinished()) {
					SynthesisResult sr = job.run(benchmark, predicateSynthesizer, true, "CDGP");
					currentEnd = Instant.now();

					if (Duration.between(currentStart, currentEnd).toMinutes() >= timeout) {
						failed = true;
						//if unsuccessful here we add sr to the list
						//add to the list as a failure.
						sr.setTimeTaken(-1);
						sr.setBenchmark(benchmarkName);
						results.add(sr);
						break;
					} else if (sr.isSuccessful()) {
						//add to the list
						//System.out.println(job.getCorrectMapping());
						sr.setTimeTaken(Duration.between(currentStart,currentEnd).toSeconds());
						sr.setBenchmark(benchmarkName);
						//System.out.println(sr.getProgramFound());
						//System.out.println(sr.getNumGenerations());
						results.add(sr);

						String program = job.getCorrectMapping();
						for (int k = 0; k < benchmark.getFunctionVariables().length; k++) {
							program = program.replace("var" + (k+1)+";", benchmark.getFunctionVariables()[k]);
						}
										System.out.println(program);
					}
				}
				

			}



		}



		String data = "benchmark, success, time, generations\n";
		//int maxTime = Integer.MIN_VALUE;
		//int maxGenerations = Integer.MIN_VALUE;
		int successes = 0;
		double totalGenerations = 0;
		double totalTime = 0;
		for (SynthesisResult sr : results) {
			data += sr.asResultString();

			if (sr.isSuccessful()) {
				successes++;
				totalGenerations += sr.getNumGenerations();
				totalTime += sr.getTimeTaken();
			}
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter("data.csv"));
		writer.write(data);

		writer.close();

		String stats = "Successess: " + successes + "\n Average Time: " + totalTime/successes + "\n Average Generations: " + totalGenerations/successes;

		writer = new BufferedWriter(new FileWriter("stats.txt"));
		writer.write(stats);

		writer.close();
		//Right, I now need to write to file the results, and also calculate the mean, max, and min of successful.
		//Let's do them all at once, because the code should be easyish.

		System.out.println(results.size());
	}
	public static void main(String[] args) throws Exception {
		
		runExperiments("unsolved.txt");
	}

}
