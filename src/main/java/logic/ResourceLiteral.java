package logic;

public class ResourceLiteral extends ResourceAbstr {

	private String literalValue;
	
	public ResourceLiteral(String literalValue) {
		this.literalValue = literalValue;
	}
	
	@Override
	public String getLexicalValue() {
		return literalValue;
	}

/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceLiteral other = (ResourceLiteral) obj;
		if (literalValue == null) {
			if (other.literalValue != null)
				return false;
		} else if (!literalValue.equals(other.literalValue))
			return false;
		return true;
	}*/

}
