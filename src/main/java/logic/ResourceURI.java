package logic;

public class ResourceURI extends ResourceAbstr {

	private String URI;
	
	public ResourceURI(String URI) {
		this.URI = URI;
	}
	
	@Override
	public String getLexicalValue() {
		return URI;
	}


/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceURI other = (ResourceURI) obj;
		if (URI == null) {
			if (other.URI != null)
				return false;
		} else if (!URI.equals(other.URI))
			return false;
		return true;
	}*/

}