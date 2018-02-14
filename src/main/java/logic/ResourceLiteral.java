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

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public boolean isURI() {
		return false;
	}


}
