package logic;

public class BindingImpl extends BindingAbstr {

	private int var;
	private Resource resource;
	
	public BindingImpl(Resource resource) {
		this.var = -1;
		this.resource = resource;
	}
	public BindingImpl(int var) {
		if(var < -1) throw new RuntimeException("ERROR: instantiating a Binding with a variable < -1");
		this.var = var;
	}
	
	@Override
	public boolean isVar() {
		return var != -1;
	}

	@Override
	public boolean isConstant() {
		return var == -1;
	}

	@Override
	public int getVar() {
		if(!isVar()) throw new RuntimeException("ERROR: Attempted to read a variable binding from a binding to a constant");
		return var;
	}

	@Override
	public Resource getConstant() {
		if(isVar()) throw new RuntimeException("ERROR: Attempted to read a constant binding from a binding to a variable");
		return resource;
	}
	

/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BindingImpl other = (BindingImpl) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (var != other.var)
			return false;
		return true;
	}*/

}
