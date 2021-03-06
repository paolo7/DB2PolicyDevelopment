package logic;

public class VariableImpl extends VariableAbstr{



	private int varNum;	
	private boolean areLiteralsAllowed;
	// flag to determine whether the literal allowed flag was set or not
	private boolean isSimpleVar;
	
	/**
	 * 
	 * @param varNum an int < 1000000
	 */
	public VariableImpl(int varNum) {
		if(varNum < 0) throw new RuntimeException("ERROR, variable numbers cannot be negative");
		this.varNum = varNum;
		this.areLiteralsAllowed = false;
		isSimpleVar = true;
	}
	
	/**
	 * 
	 * @param varNum an int < 1000000
	 */
	public VariableImpl(int varNum, boolean areLiteralsAllowed) {
		if(varNum < 0) throw new RuntimeException("ERROR, variable numbers cannot be negative");
		this.varNum = varNum;
		this.areLiteralsAllowed = areLiteralsAllowed;
		isSimpleVar = false;
	}

	@Override
	public int getVarNum() {
		return varNum;
	}

	@Override
	public boolean areLiteralsAllowed() {
		if(isSimpleVar) throw new RuntimeException("ERROR, this flag was not set on this instance.");
		return areLiteralsAllowed;
	}

	@Override
	public boolean isSimpleVar() {
		// TODO Auto-generated method stub
		return isSimpleVar;
	}
	
	private static int newVar = 1000000;
	public static int getNewVarNum() {
		if(newVar == Integer.MAX_VALUE) newVar = 1000000;
		return newVar++;
	}
	
	
}
