package eppic.assembly;



/**
 * Double implementation of Dimension2D, which is oddly omitted from java.awt.geom
 */
public final class Dimension2D  {
	public static class Double extends java.awt.geom.Dimension2D {
		private double width;
		private double height;
		public Double() {
			this(0.,0.);
		}
		public Double(double w, double h) {
			setSize(w,h);
		}
		public Double(java.awt.geom.Dimension2D dim) {
			this(dim.getWidth(),dim.getHeight());
		}
		@Override
		public void setSize(double width, double height) {
			this.width = width;
			this.height = height;
		}
		@Override
		public double getWidth() {
			return width;
		}
		public void setWidth(double width) {
			this.width = width;
		}
		@Override
		public double getHeight() {
			return height;
		}
		public void setHeight(double height) {
			this.height = height;
		}
		@Override
		public String toString() {
			return getClass().getName() + "[width=" + width + ",height=" + height + "]";
		}
	}
}