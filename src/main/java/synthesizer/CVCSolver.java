package synthesizer;

import benchmark.Benchmark;
import verification.Verifier;

import java.io.*;

public class CVCSolver extends Synthesizer {

	private String cvcLocation;
	private Benchmark benchmark;

	public CVCSolver(Benchmark benchmark, String cvcLocation) {
		this.benchmark = benchmark;
		this.cvcLocation = cvcLocation;
	}


	//"C:\\Users\\twels\\Documents\\CVC4\\cvc4-1.7-win64-opt.exe"
	public SynthesisResult synthesize(Verifier verifier) {
		// SynthesisResult result = new SynthesisResult()
		String program = "";
		boolean success = false;

		Process process = null;
		try {
			
			
			
			//process = new ProcessBuilder(cvcLocation, "-L", "sygus", "--sygus-si", "all", "sygus-out"
				//	.start();
			
			process = new ProcessBuilder(cvcLocation, "-L", "sygus2", "--sygus-out", "status-and-def", "--tlimit", "3600000")
					.start();
			
			//process = new ProcessBuilder(cvcLocation, "-L", "sygus2", "--sygus-out", "status-and-def", "--tlimit", "1000")
				//	.start();


			//////System.out.println(buildCVCQuery(verifier.getTargetPartial(), benchmark));
			//////System.out.println("Partial " + verifier.getTargetPartial());
			//String query = buildCVCQuery(verifier.getTargetPartial(), benchmark, verifier.getRemainingPartials(), verifier.getGlobalConstraints());
			String query = benchmark.getFullFile();

			////System.out.println(query);
			try (Writer w = new OutputStreamWriter(process.getOutputStream(), "UTF-8")) {
			//	//System.out.println("FIRE!");
				w.write(query);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw e;
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			
			

			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			//isr.
			
			

			try (BufferedReader br = new BufferedReader(isr)) {
				String line;
				line = br.readLine();
				////System.out.println(line);
				String cvcResponse = "";
				while ((line = br.readLine()) != null) {
				    cvcResponse += line;
				}
				
				////System.out.println(cvcResponse);
				
				if (!cvcResponse.isEmpty()) {

				program = cvcResponse.substring(cvcResponse.lastIndexOf("Bool") + 5, cvcResponse.length() - 2).trim();
				success = true;
				
				} else {
					program = "";
					success = false;
				}
				
				

				/*
				////System.out.println(cvcResponse);
				String status = cvcResponse;
				////System.out.println("Did we get anything? " + status);
				////System.out.println(status);
				String returnedFunction = "";
				if (status.equals("unsat")) {
					
					returnedFunction = br.readLine();
					////System.out.println("Returned function is " + returnedFunction);
					success = true;
					program = returnedFunction.substring(returnedFunction.lastIndexOf("Bool") + 4, returnedFunction.length() - 1).trim();
					
					////System.out.println("Returned program was " + program);
				}*/



			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			process.destroy();
		}

		return new SynthesisResult(success, program);
	}
}
