package logic;

public interface ConversionTriple {

	public Binding getSubject();
	public Binding getPredicate();
	public Binding getObject();
	public String toSPARQL(Binding[] bindings);
}
