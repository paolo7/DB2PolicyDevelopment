package logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface ConversionTriple {

	public Binding getSubject();
	public Binding getPredicate();
	public Binding getObject();
	public String toGPPGSPARQL(Binding[] bindings);
	public String toSPARQL(Binding[] bindings);
	public String toSPARQL(Binding[] bindings, Map<Integer,Integer> varsExpansion);
	public Set<Integer> getNoLitVariables(Binding[] bindings);
}
