package nl.tno.torxakis.wxsgenerator;

import java.util.ArrayList;
import java.util.List;

public class FeatureInfo {
	private FeatureInfo parent = null;
	private String id = "";
	private String title = "";
	private String description = "";
	private String display = "";
	private int level = -1;
	private String configurableDirectory = "";
	private Boolean allowAbsent = true;
	private List<String> componentIds = new ArrayList<>();
	private List<FeatureInfo> children = new ArrayList<>();

	FeatureInfo(FeatureInfo parent, String id, String title, String description, String display, int level, String configurableDirectory, boolean allowAbsent) {
		this.parent = parent;
		this.id = id;
		this.title = title;
		this.description = description;
		this.display = display;
		this.level = level;
		this.configurableDirectory = configurableDirectory;
		this.allowAbsent = allowAbsent;
	}

	FeatureInfo(FeatureInfo parent, String id, String title, String description, int level, boolean allowAbsent) {
		this.parent = parent;
		this.id = id;
		this.title = title;
		this.description = description;
		this.level = level;
		this.allowAbsent = allowAbsent;
	}

	FeatureInfo(FeatureInfo parent, String id, String title, String description, int level) {
		this.parent = parent;
		this.id = id;
		this.title = title;
		this.description = description;
		this.level = level;
	}

	List<FeatureInfo> getChildren() {
		return children;
	}

	List<String> getComponentIds() {
		return componentIds;
	}

	@Override
	public String toString() {
		return "FeatureInfo [parent=" + parent + ", id=" + id + ", title=" + title + ", description=" + description
				+ ", display=" + display + ", level=" + level + ", configurableDirectory=" + configurableDirectory
				+ ", componentIds=" + componentIds + ", allowAbsent=" + allowAbsent + "]";
	}

	String getFeatureHeader(String indent) {
		String out = indent + "<Feature ";

		if ( !id.equals("") ) {
			out += "Id='" + id + "' ";
		}

		if ( !title.equals( "" ) ) {
			out += "Title='" + title + "' ";
		}

		if ( !description.equals( "" ) ) {
			out += "Description='" + description + "' ";
		}

		if ( !display.equals( "" ) ) {
			out += "Display='" + display + "' ";
		}

		if ( level != -1) {
			out += "Level='" + level + "' ";
		}

		if ( !configurableDirectory.equals( "" ) ) {
			out += "ConfigurableDirectory='" + configurableDirectory + "' ";
		}

		if ( !allowAbsent ) {
			out += "Absent='disallow' ";
		}

		out += "AllowAdvertise='no' >\n";

		return out;
	}

	String getFeatureFooter(String indent) {
		return indent + "</Feature>\n";
	}
}
