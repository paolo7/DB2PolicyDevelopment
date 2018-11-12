package logic;

import java.util.Set;

public interface ConversionTriple {

	public Binding getSubject();
	public Binding getPredicate();
	public Binding getObject();
	public String toGPPGSPARQL(Binding[] bindings);
	public String toSPARQL(Binding[] bindings);
	public String toSPARQL();
	public String toSPARQL_INSERT(Binding[] bindings, String baseBlank);
	public Set<Integer> getNoLitVariables(Binding[] bindings);
	public ConversionTriple applyBinding(Binding[] bindings);
}
