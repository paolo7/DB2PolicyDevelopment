package logic;

public abstract class BindingAbstr implements Binding{

	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Binding)) {
            return false;
        }
        Binding p = (Binding) o;
        if(this.isVar() != p.isVar()) return false;
        if(this.isVar()) {
        	if(this.getVar() == p.getVar()) return true;
        } else {
        	if(this.getConstant().equals(p.getConstant())) return true;
        }
        return false;
    }
	
	@Override
	public String toString() {
		if(this.isVar()) return "?v"+this.getVar();
		else return this.getConstant().toString();
	}
	
	@Override
	public int hashCode() {
		Resource resource = null;
		int var = -1;
		if(this.isConstant()) resource = this.getConstant();
		else var = this.getVar();
		final int prime = 31;
		int result = 7;
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + var;
		return result;
	}
}
