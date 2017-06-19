package nl.tno.torxakis.wxsgenerator;

public class TFile {
	private String name;
	
	TFile( String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "File [name=" + name + "]";
	}
	
	public String getNameOnly() {
		String[] fileParts = this.name.split("\\\\");
		
		return fileParts[ fileParts.length - 1];
	}
	
	public String getRelativeName( TDirectory root ) {
		return name.replace( root.getName() + "\\", "" );
	}
	
}
