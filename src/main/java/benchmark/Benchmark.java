package benchmark;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * 
 * @author Thomas Welsch
 *
 */
public class Benchmark {

	private String fullFile;
	private String functionName;
	private String transAssertionString;
	private String preConAssertionString;
	private String postConAssertionString;
	private String funString;
	private String[] functionVariables;
	private String[] functionVariableTypes;
	private String[] primedFunctionVariables;
	private String[] globalVariables;
	private String[] globalVariableTypes;
	private String[] synthesisVariableNames;
	private String[] definedFunctions;
	private String[] definedFunctionNames;
	private int[] constants;
	private String logic;



	public Benchmark(String functionName, String preConAssertionString, String postConAssertionString, String transAssertionString,
			String funString, String[] globalVariables,
			String[] globalVariableTypes, 
			String[] definedFunctions, String[] definedFunctionNames, String logic, int[] constants, String[] synthesisVariableNames, String[] functionVariables,
			String[] primedFunctionVariables, String[] functionVariableTypes) {
		this.functionName = functionName;
		this.preConAssertionString = preConAssertionString;
		this.postConAssertionString = postConAssertionString;
		this.transAssertionString = transAssertionString;
		this.funString = funString;
		this.functionVariables = functionVariables;
		this.definedFunctions = definedFunctions;
		this.definedFunctionNames = definedFunctionNames;
		this.logic = logic;
		this.constants = constants;
		this.synthesisVariableNames = synthesisVariableNames;
		this.globalVariables = globalVariables;
		this.globalVariableTypes = globalVariableTypes;
		this.primedFunctionVariables = primedFunctionVariables;
		this.functionVariableTypes = functionVariableTypes;
		
	//	if (this.globalVariables != null) {
		//	System.out.println("Not null");
		//}
	
	}
	
	public String getFunctionName() {
		return functionName;
	}


	public String getFunString() {
		return funString;
	}


	public String[] getDefinedFunctions() {
		return definedFunctions;
	}

	public String[] getDefinedFunctionNames() {
		return definedFunctionNames;
	}
	
	public String getLogic() {
		return logic;
	}
	
	
	public String[] getSynthesisVariableNames() {
		return synthesisVariableNames;
	}

	public void setSynthesisVariableNames(String[] synthesisVariableNames) {
		this.synthesisVariableNames = synthesisVariableNames;
	}
	
	
	public String[] getFunctionVariables() {
		return functionVariables;
	}

	public void setFunctionVariables(String[] functionVariables) {
		this.functionVariables = functionVariables;
	}

	public String[] getGlobalVariableTypes() {
		return globalVariableTypes;
	}

	

	public String getFullFile() {
		return fullFile;
	}

	public void setFullFile(String fullFile) {
		this.fullFile = fullFile;
	}
	
	
	
	public static Benchmark parseBenchmark(String fileName) throws Exception {
		if (fileName.contains(".sl")) {
			return parseBenchmarkSyGuS(fileName);
		} else {
			return parseBenchmarkSMT(fileName);
		}
	}
	
	public static Benchmark parseBenchmarkSMT(String fileName) throws Exception {
		File file = new File(fileName);
		String fileContent = Files.readString(file.toPath());
		
		
		fileContent = fileContent.replaceAll("\\(\s+", "(");
		int[] constants = extractConstants(fileContent);
		
	/*	System.out.println("Constants");
		for (int i = 0; i < constants.length; i++) {
			System.out.println(constants[i]);
		}*/
		ArrayList<String> subStrings = new ArrayList<>();

		int parenCounter = 0;
		String subString = "";
		for (int i = 0; i < fileContent.length(); i++) {
			subString += fileContent.charAt(i);
			if (fileContent.charAt(i) == '(') {
				parenCounter++;
			} else if (fileContent.charAt(i) == ')') {
				parenCounter--;
			}

			if (parenCounter == 0 && !subString.isBlank()) {
				subStrings.add(subString.strip());
				subString = "";
			}
		}

		String functionName = "inv-f";
		String funString = "";
		String logic = "";
		String transFuncName = "trans-f";
		String preConName = "pre-f";
		String postConName = "post-f";
		String preConAssertionString = "";
		String postConAssertionString = "";
		String transAssertionString = "";
		ArrayList<String> globalVariables = new ArrayList<>();
		ArrayList<String> primeFunctionVariables = new ArrayList<>();
		ArrayList<String> globalVariableTypes = new ArrayList<>();
		ArrayList<String> functionVariableTypes = new ArrayList<>();
		ArrayList<String> definedFunctions = new ArrayList<>();
		ArrayList<String> definedFunctionNames = new ArrayList<>();
		ArrayList<String> synthInvVariables = new ArrayList<>();
		for (String s : subStrings) {
			if (s.contains("set-logic")) {
				try (Scanner scan = new Scanner(s)) {
					scan.next(); // it's directly after synth-fun, just discard it
					logic = scan.next();
					int toStrip = logic.lastIndexOf(")");
					logic = logic.substring(0,toStrip);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			} else if (s.contains("declare-const")) {
				try (Scanner scan = new Scanner(s)) {
					scan.next(); //gets declare-const
					globalVariables.add(scan.next());
					globalVariableTypes.add(scan.next());
					
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			}
				else if (s.contains("define-fun")) {
				//System.out.println(s);
				try (Scanner scan = new Scanner(s)) {
					scan.next(); // function name directly after define-fun, for now we don't need to worry about
									// everything afterwards
					
					
					String dfn = scan.next();
					//System.out.println(dfn);
					//System.out.println(dfn);
					if (dfn.contains(functionName)) {
						funString = s.replace(functionName, functionName + " ");
						
						funString = funString.substring(0, funString.lastIndexOf("Bool")-1) + " Bool funToken;)";
						//System.out.println(funString);
						
						String funVariablesString = funString.substring(funString.indexOf(" " +functionName + " ") + functionName.length()+2, funString.lastIndexOf("funToken;"));
						funVariablesString = funVariablesString.substring(0, funVariablesString.lastIndexOf(")")).replace("(", "").replace(")", "");
						
						//System.out.println("fvString: " + funVariablesString);
						try(Scanner scanTwo = new Scanner(funVariablesString)) {
							while(scanTwo.hasNext()) {
								String varName = scanTwo.next();
								String type = scanTwo.next();
								//System.out.println(varName + " " + type);
								synthInvVariables.add(varName);
								functionVariableTypes.add(type);
							}
						} catch(Exception e) {
							throw e;
						}
						
						
					} else {
						definedFunctions.add(s);
						definedFunctionNames.add(dfn);
					}

				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			} else if (s.contains("assert")) {
				if (s.contains(preConName)) {
					preConAssertionString  = "(not " + s.substring("(assert".length(), s.length()-1).strip() + ")";
				//	System.out.println(preConAssertionString);
				} else if (s.contains(transFuncName)) {
					transAssertionString = "(not " + s.substring("(assert".length(), s.length()-1).strip() + ")";
					//System.out.println(transAssertionString);
				} else {
					postConAssertionString = "(not " + s.substring("(assert".length(), s.length()-1).strip() + ")";
					//System.out.println(postConAssertionString);
				}
			}
		}

		
		
		
		String[] definedFunctionsArr;
		String[] definedFunctionNamesArr;
		if (definedFunctions.isEmpty()) {
			definedFunctionsArr = null;
			definedFunctionNamesArr = null;
		} else {
			definedFunctionsArr = definedFunctions.toArray(new String[definedFunctions.size()]);
			definedFunctionNamesArr = definedFunctionNames.toArray(new String[definedFunctionNames.size()]);

		}
		


		for (int i = 0; i < globalVariables.size(); i++) {
			if (globalVariables.get(i).contains("!")) {
				//System.out.println("adding " + globalVariables.get(i));
				primeFunctionVariables.add(globalVariables.get(i));
			}
		}
		

		
		
	//	//System.out.println(funString);
		//funString = MIUtils.transformProgramFromInvocations(funString, functionVariables, variables);
		////System.out.println(funString);
		
		String[] synthesisVariableNames = new String[globalVariables.size()];
		for (int i = 0; i < synthInvVariables.size(); i++) {
			synthesisVariableNames[i] = "var" + (i+1) + ";";
		}
		
		
		Benchmark b = new Benchmark(functionName, preConAssertionString, postConAssertionString, transAssertionString,
				funString, globalVariables.toArray(new String[globalVariables.size()]),
				globalVariableTypes.toArray(new String[globalVariableTypes.size()]),
				definedFunctionsArr, definedFunctionNamesArr, logic, constants, synthesisVariableNames, 
				synthInvVariables.toArray(new String[synthInvVariables.size()]), primeFunctionVariables.toArray(new String[primeFunctionVariables.size()]),
				functionVariableTypes.toArray(new String[functionVariableTypes.size()]));
		
		

		
		return b;
	}

	public static Benchmark parseBenchmarkSyGuS(String fileName) throws Exception {
		File file = new File(fileName);
		String fileContent = Files.readString(file.toPath());
		
		String fullFile = fileContent;
		
		
		fileContent = fileContent.replaceAll("\\(\s+", "(");
		int[] constants = extractConstants(fileContent);
		
	/*	System.out.println("Constants");
		for (int i = 0; i < constants.length; i++) {
			System.out.println(constants[i]);
		}*/
		ArrayList<String> subStrings = new ArrayList<>();

		int parenCounter = 0;
		String subString = "";
		for (int i = 0; i < fileContent.length(); i++) {
			subString += fileContent.charAt(i);
			if (fileContent.charAt(i) == '(') {
				parenCounter++;
			} else if (fileContent.charAt(i) == ')') {
				parenCounter--;
			}

			if (parenCounter == 0 && !subString.isBlank()) {
				subStrings.add(subString.strip());
				subString = "";
			}
		}

		String functionName = "";
		String funString = "";
		String logic = "";
		String transFuncName = "";
		String preConName = "";
		String postConName = "";
		ArrayList<String> globalVariables = new ArrayList<>();
		ArrayList<String> primeFunctionVariables = new ArrayList<>();
		ArrayList<String> globalVariableTypes = new ArrayList<>();
		ArrayList<String> functionVariableTypes = new ArrayList<>();
		ArrayList<String> definedFunctions = new ArrayList<>();
		ArrayList<String> definedFunctionNames = new ArrayList<>();
		ArrayList<String> synthInvVariables = new ArrayList<>();
		for (String s : subStrings) {
			if (s.contains("set-logic")) {
				try (Scanner scan = new Scanner(s)) {
					scan.next(); // it's directly after synth-fun, just discard it
					logic = scan.next();
					int toStrip = logic.lastIndexOf(")");
					logic = logic.substring(0,toStrip);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			} else if (s.contains("synth-inv")) {
				try (Scanner scan = new Scanner(s)) {
					scan.next(); // it's directly after synth-fun, just discard it
					functionName = scan.next();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}

				funString = s.replace("synth-inv", "define-fun").trim();

				
				
				funString = funString.substring(0, funString.length() - 1) + " Bool funToken;)";
				
				//System.out.println("Fun String " + funString);
				
				String funVariablesString = funString.substring(funString.indexOf(" " +functionName + " ") + functionName.length()+2, funString.lastIndexOf("funToken;"));
				funVariablesString = funVariablesString.substring(0, funVariablesString.lastIndexOf(")")).replace("(", "").replace(")", "");
				
				//System.out.println("Fun vars string: " + funVariablesString);

				try(Scanner scan = new Scanner(funVariablesString)) {
					while(scan.hasNext()) {
						String varName = scan.next();
						String type = scan.next();
						globalVariables.add(varName);
						globalVariables.add(varName + "!");
						synthInvVariables.add(varName);
						globalVariableTypes.add(type);
						globalVariableTypes.add(type);
					}
				} catch(Exception e) {
					e.printStackTrace();
					throw e;
				}
				
				
				
			}
			
			
			else if (s.contains("define-fun")) {
				definedFunctions.add(s);
				//System.out.println(s);
				try (Scanner scan = new Scanner(s)) {
					scan.next(); // function name directly after define-fun, for now we don't need to worry about
									// everything afterwards
					//System.out.println(dfn);
					definedFunctionNames.add(scan.next());
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			} else if (s.contains("inv-constraint")) {
				// ////System.out.println(s);
				int idx = 16;
				//constraints.add(s.substring(idx, s.length() - 1).trim());
				String[] funcNames = s.substring(idx, s.length() - 1).trim().split(" ");

				
				preConName = funcNames[1];
				transFuncName = funcNames[2];
				postConName = funcNames[3];
//				System.out.println(s.substring(idx, s.length() - 1).trim());
			}
		}



		
		String preConAssertionString = "(=> (" + preConName + " " + produceVarsString(synthInvVariables) + ") ";
		preConAssertionString += " (" + functionName + " " + produceVarsString(synthInvVariables) + "))";
		
	String 	transAssertionString = "(=> (and (" + functionName + " " + produceVarsString(synthInvVariables) + ") ";
		transAssertionString += " (" + transFuncName + " " + produceTransVarsString(synthInvVariables) + ")) ";
		transAssertionString += " (" + functionName + " " + producePrimeVarsString(synthInvVariables) + "))";
		
		
	
	String	postConAssertionString = "(=> (" + functionName + " " + produceVarsString(synthInvVariables) + ") ";
		postConAssertionString += " (" + postConName + " " + produceVarsString(synthInvVariables) + "))";
		
		
		//System.out.println(preConAssertionString);
		
		//System.out.println(transAssertionString);
		
		//System.out.println(postConAssertionString);
		
		
		
		String[] definedFunctionsArr;
		String[] definedFunctionNamesArr;
		if (definedFunctions.isEmpty()) {
			definedFunctionsArr = null;
			definedFunctionNamesArr = null;
		} else {
			definedFunctionsArr = definedFunctions.toArray(new String[definedFunctions.size()]);
			definedFunctionNamesArr = definedFunctionNames.toArray(new String[definedFunctionNames.size()]);

		}
		
		
		String transFunc = null;
		ArrayList<String> otherFuncs = new ArrayList<>();
		
		for (int i = 0; i < definedFunctionNames.size(); i++) {
			String funName = definedFunctionNames.get(i);
			String tmp = definedFunctions.get(i);
			tmp = tmp.substring(tmp.lastIndexOf("Bool"));
			if (funName.equals(transFuncName)) {
				transFunc = tmp;
			} else if (funName.equals(preConName) || funName.equals(postConName)) {
				otherFuncs.add(tmp);
			}
		}
		
		//System.out.println(transChecker);
		
		ArrayList<String> occurringVariables = new ArrayList<>();
		for (int i = 0; i < globalVariables.size(); i++) {
			String pv = globalVariables.get(i) + "!";

			if (transFunc.contains(" " + pv + " ") || transFunc.contains("(" +pv + " ") || transFunc.contains(" " + pv + ")")) {
				occurringVariables.add(globalVariables.get(i));
				primeFunctionVariables.add(pv);
				//System.out.println(globalVariableTypes.get(i));
				functionVariableTypes.add(globalVariableTypes.get(i));
				
			}
		}
		
		for (String otherFunc : otherFuncs) {
			for (int i = 0; i < globalVariables.size(); i++) {
				String pv = globalVariables.get(i);
				if (occurringVariables.contains(pv)) {
					continue;
				}

				if (otherFunc.contains(" " + pv + " ") || otherFunc.contains("(" + pv + " ")
						|| otherFunc.contains(" " + pv + ")")) {
					occurringVariables.add(globalVariables.get(i));
					// System.out.println(globalVariableTypes.get(i));
					functionVariableTypes.add(globalVariableTypes.get(i));

				}
			}
		}
		
	//	//System.out.println(funString);
		//funString = MIUtils.transformProgramFromInvocations(funString, functionVariables, variables);
		////System.out.println(funString);
		
		String[] synthesisVariableNames = new String[globalVariables.size()];
		for (int i = 0; i < occurringVariables.size(); i++) {
			synthesisVariableNames[i] = "var" + (i+1) + ";";
		}
		
		

		
		/*
		 * 
	public InvariantBenchmark(String functionName, String preConAssertionString, String postConAssertionString, String transAssertionString,
			String funString, String[] functionVariables,
			String[] functionVariableTypes, 
			String[] definedFunctions, String[] definedFunctionNames, String logic) {
		 * 
		 */
		
		//System.out.println("Logic is " + logic);
		Benchmark b = new Benchmark(functionName, preConAssertionString, postConAssertionString, transAssertionString,
				funString, globalVariables.toArray(new String[globalVariables.size()]),
				globalVariableTypes.toArray(new String[globalVariableTypes.size()]),
				definedFunctionsArr, definedFunctionNamesArr, logic, constants, synthesisVariableNames, 
				occurringVariables.toArray(new String[occurringVariables.size()]), primeFunctionVariables.toArray(new String[primeFunctionVariables.size()]),
				functionVariableTypes.toArray(new String[functionVariableTypes.size()]));
		
		/*InvariantBenchmark b = new InvariantBenchmark(functionName, null, funString, variables.toArray(new String[variables.size()]),
				definedFunctionsArr, definedFunctionNamesArr,logic, null);*/
		//b.setSynthesisVariableNames(variables.toArray(new String[variables.size()]));

	//	b.setConstants(constants);
		b.setFullFile(fullFile); //For testing with CVC5 later I reckon
		//b.setSynthesisVariableNames(synthesisVariableNames);
		

		
		return b;
	}
	
	private static String produceVarsString(ArrayList<String> variables) {
		String retVal = "";
		for (int i = 0; i < variables.size()-1; i++) {
			retVal += variables.get(i) + " ";
		}
		
		retVal += variables.get(variables.size()-1);
		
		return retVal;
	}
	
	private static String produceTransVarsString(ArrayList<String> variables) {
		ArrayList<String> transVariables = new ArrayList<>();
		
		transVariables.addAll(variables);
		
		for (String s : variables) {
			transVariables.add(s+"!");
		}
		
		return produceVarsString(transVariables);
	}
	
	private static String producePrimeVarsString(ArrayList<String> variables) {
		ArrayList<String> primeVariables = new ArrayList<>();
		
		
		for (String s : variables) {
			primeVariables.add(s+"!");
		}
		
		return produceVarsString(primeVariables);
		
	}
	
	public int[] getConstants() {
		return constants;
	}

	public void setConstants(int[] constants) {
		this.constants = constants;
	}
	
	

	public String getTransAssertionString() {
		return transAssertionString;
	}

	public String getPreConAssertionString() {
		return preConAssertionString;
	}

	public String getPostConAssertionString() {
		return postConAssertionString;
	}
	
	public String[] getGlobalVariables() {
		return globalVariables;
	}
	
	
	

	public String[] getFunctionVariableTypes() {
		return functionVariableTypes;
	}

	public String[] getPrimedFunctionVariables() {
		return primedFunctionVariables;
	}

	private static int[] extractConstants(String benchmarkString) {
		HashSet<Integer> constants = new HashSet<>();
		String scanString = benchmarkString;
		constants.add(0);
		constants.add(1);
	//	constants.add(-1);
		
		scanString = scanString.replace("(", " ");
		scanString = scanString.replace(")", " ");
		
		
		try (Scanner scan = new Scanner(scanString)) {

			while (scan.hasNext()) {
				if (scan.hasNextInt()) {
					Integer c = scan.nextInt();
					constants.add(c);
					constants.add(c * -1);
					
				} else {
					scan.next();
				}

			}
		}
		
		int[] constantsArray = new int[constants.size()];
		int i = 0;
		for (Integer c : constants) {
			constantsArray[i] = c;
			//System.out.println(c);
			i++;
		}
		
		
		return constantsArray;
		
	}
	public static void main(String[] args) throws Exception {
		String benchmarkFile = "src/main/resources/smt_benchmarks/1.c.smt";
		//String benchmarkFile = "src/main/resources/benchmarks/fg_max4.sl";
		Benchmark benchmark = Benchmark.parseBenchmark(benchmarkFile);
		
		String[] gv = benchmark.getGlobalVariables();
		
		for (int i = 0; i < gv.length; i++) {
			System.out.println(gv[i]);
		}
		
		//int[] constants = benchmark.getConstants();
		
		//for (int c : constants) {
		//	System.out.println(c);
		//}
	}
	

}
