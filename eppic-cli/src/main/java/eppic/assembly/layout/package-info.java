/**
 * Classes for graph layout. Implementations of {@link eppic.assembly.layout.GraphLayout}
 * modify the 3D coordinates of a graph such that they give a visually appealing
 * 2D layout with z=0 for all positions. Thus, they are really 2D layouts, even
 * though they may still use {@link eppic.assembly.ChainVertex3D} objects to
 * store positions.
 * 
 * The {@link eppic.assembly.layout.mxgraph} subpackage provides versions which
 * store the positions in mxGraph (aka jgraphx) cells. This is generally less
 * flexible than using the {@link eppic.assembly.layout.GraphLayout} versions.
 * @author Spencer Bliven
 *
 */
package eppic.assembly.layout;