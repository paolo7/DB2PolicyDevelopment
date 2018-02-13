package logic;

public interface Binding {

	public boolean isVar();
	public boolean isConstant();
	public int getVar();
	public Resource getConstant();
}
