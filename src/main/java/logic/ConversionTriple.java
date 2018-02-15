package logic;

import java.util.Map;

public interface ConversionTriple {

	public Binding getSubject();
	public Binding getPredicate();
	public Binding getObject();
	public String toSPARQL(Binding[] bindings);
	public String toSPARQL(Binding[] bindings, Map<Integer,Integer> varsExpansion);
}
