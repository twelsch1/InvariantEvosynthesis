package evoSynthesis;

import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

public class ParamsHelper {

	
	public static void setFunctionSet(ParameterDatabase dbase, String[] variables, String[] variableTypes) {
	
		
		ArrayList<String> functions = new ArrayList<>();
		ArrayList<String> functionTypes = new ArrayList<>();
		
		//add standard LIA operators
		
		functions.add("functions.longint.Add");
		functionTypes.add("nc2");
		
		functions.add("functions.longint.Sub");
		functionTypes.add("nc2");
		
		functions.add("functions.longint.Mul");
		functionTypes.add("ncmult");
		
		//functions.add("functions.longint.Mod");
		//functionTypes.add("ncmod");
		
		functions.add("functions.longint.GT");
		functionTypes.add("nccomp");
		
		functions.add("functions.longint.GTE");
		functionTypes.add("nccomp");
		
		functions.add("functions.longint.LT");
		functionTypes.add("nccomp");
		
		functions.add("functions.longint.LTE");
		functionTypes.add("nccomp");
		
		functions.add("functions.longint.Equals");
		functionTypes.add("nccomp");
		
		functions.add("functions.longint.Distinct");
		functionTypes.add("nccomp");
		
		functions.add("functions.longint.And");
		functionTypes.add("ncandor");
		
		//if we aren't going to have the not, we need the OR to be propositionally complete.
		//but if we are propositionally complete, we wind up with these crazy ORs that lead us down a wayward path.
		//Hmm...
		
		functions.add("functions.longint.Or");
		functionTypes.add("ncandor");
		
		//Wfunctions.add("functions.longint.Not");
		//functionTypes.add("ncnot");
		
		/*functions.add("functions.longint.ITE");
		functionTypes.add("ncite");*/
		
		functions.add("functions.longint.Ephemeral");
		functionTypes.add("ncephem");
		
		functions.add("functions.longint.Ephemeral");
		functionTypes.add("const");
		
		functions.add("functions.longint.EphemeralBoolean");
		functionTypes.add("nc0bool");
		
		functions.add("functions.longint.Var");
		functionTypes.add("nc0");
		//Add variables
		
		//for (int i = 1; i <= variables.length; i++ ) {
			//functions.add("variables.Var"+i);
			//functionTypes.add("nc0");
		//}
		
		//Add function set to the database
		dbase.set(new Parameter("gp.fs.0.size"), Integer.toString(functions.size()));
		for (int i = 0; i < functions.size(); i++) {
			//////System.out.println(functions.get(i));
			dbase.set(new Parameter("gp.fs.0.func."+i), functions.get(i));
			dbase.set(new Parameter("gp.fs.0.func."+i+".nc"), functionTypes.get(i));
		}
		
	}
}

