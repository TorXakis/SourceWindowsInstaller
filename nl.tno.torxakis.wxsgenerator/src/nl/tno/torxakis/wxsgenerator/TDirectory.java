package nl.tno.torxakis.wxsgenerator;

import java.util.ArrayList;
import java.util.List;

public class TDirectory {
	private String name;
	private List<TFile> files = new ArrayList<TFile>();
	private List<TDirectory> directories = new ArrayList<TDirectory>();
	
	TDirectory( String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TFile> getFiles() {
		return files;
	}

	public void setFiles(List<TFile> files) {
		this.files = files;
	}

	public List<TDirectory> getDirectories() {
		return directories;
	}

	public void setDirectories(List<TDirectory> directories) {
		this.directories = directories;
	}

	@Override
	public String toString() {
		return "TDirectory [name=" + name + ", files=" + files + ", directories=" + directories + "]";
	}
	
	public String getNameOnly() {
		String[] dirParts = this.name.split("\\\\");
		
		return dirParts[ dirParts.length - 1];
	}
	
	public String getRelativeName( TDirectory root ) {
		return name.replace( root.getName() + "\\", "" );
	}
	
	
}
