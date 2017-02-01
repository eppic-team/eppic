package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import eppic.assembly.gui.LatticeGUIMustache3D;

/**
 * Small bean to mimic elements of {@link LatticeGUIMustache3D} without
 * the overhead of calculating the graph
 * @author blivens
 *
 */
public class LazyLatticeGUIMustache3D {
	private String webappRoot; // the root of the webapp (ewui in our usual setup)
	private String strucURL; //path to the structure
	private String libURL; // path to the library (if any)
	private String dataURL; // path to the json (or other) data file
	private String title; // Title for HTML page
	private String size; // Target size for content
	public LazyLatticeGUIMustache3D() {}
	public LazyLatticeGUIMustache3D(String strucURI, String dataURI,String libURL,
			String title, String size) {
		this.strucURL = strucURI;
		this.dataURL = dataURI;
		this.libURL = libURL;
		this.title = title;
		this.size = size;
	}
	public String getStrucURL() {
		return strucURL;
	}
	public void setStrucURL(String strucURI) {
		this.strucURL = strucURI;
	}
	public String getLibURL() {
		return libURL;
	}
	public void setLibURL(String libURL) {
		this.libURL = libURL;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getDataURL() {
		return dataURL;
	}
	public void setDataURL(String dataURL) {
		this.dataURL = dataURL;
	}
	public String getWebappRoot() {
		return webappRoot;
	}
	public void setWebappRoot(String webappRoot) {
		this.webappRoot = webappRoot;
	}
}