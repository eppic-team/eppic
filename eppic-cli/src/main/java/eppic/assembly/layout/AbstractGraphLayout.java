package eppic.assembly.layout;

public abstract class AbstractGraphLayout<V,E> implements GraphLayout<V,E> {
	protected final VertexPositioner<V> vertexPositioner;

	public AbstractGraphLayout( VertexPositioner<V> vertexPositioner) {
		this.vertexPositioner = vertexPositioner;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VertexPositioner<V> getVertexPositioner() {
		return vertexPositioner;
	}

}
