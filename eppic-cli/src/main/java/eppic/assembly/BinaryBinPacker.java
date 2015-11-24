/*
 * This code is derived from Jake Gordon's packer.growing.js. 
 * https://github.com/jakesgordon/bin-packing/
 * Ported to Java by Spencer Bliven.
 *
 * The original code was provided under the MIT license:
 *
 *   Copyright (c) 2011, 2012, 2013 Jake Gordon and contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */


package eppic.assembly;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout 2D rectangles. Specifically, layout rectangles without rotation in an
 * expandable area. Tries to keep the container roughly square.
 * 
 * Uses a binary-split algorithm for adding bins, automatically increasing the
 * rectangle size if they don't fit. This heuristic seems to work fairly well
 * for practical problems.
 * 
 * See http://codeincomplete.com/posts/2011/5/7/bin_packing/
 * 
 * @author blivens
 *
 */
public class BinaryBinPacker<T> {

	private static final Logger logger = LoggerFactory.getLogger(BinaryBinPacker.class);

	/**
	 * Node in the tree of containers. Bins can have an optional contents, which
	 * is assumed to be in the top-left corner, and up to two children.
	 * The bin's contents extend to the right child along the x and to the down
	 * child along the y. Children should fill any area not taken by the contents.
	 */
	private class Bin {
		Rectangle2D bounds;
		T contents;
		Bin right;
		Bin down;
		public Bin(double x, double y, double w, double h, T contents) {
			this.bounds = new Rectangle2D.Double(x, y, w, h);
			this.contents = contents;
			right = down = null;
		}

		/**
		 * Finds an empty spot in this bin or its children which can accommodate the box.
		 * @param box
		 * @return the empty bin, or null if the box doesn't fit
		 */
		private Bin findNode( Dimension2D box) {

			if(contents != null || right != null || down != null) {
				// First check children
				if(right != null) {
					Bin child = right.findNode(box);
					if(child != null)
						return child;
				}
				if(down != null) {
					Bin child = down.findNode(box);
					if(child != null)
						return child;
				}
				return null; // both children are full
			} else if( box.getWidth() <= bounds.getWidth() && box.getHeight() <= bounds.getHeight() ) {
				// Fits here
				return this;
			} else {
				// Doesn't fit
				return null;
			}
		}


		/**
		 * Splits a container horizontally with the box in the top left and
		 * adds the contents.
		 * Assumes that the box fits.
		 * @param node
		 * @param box
		 * @param contents
		 * @return
		 */
		private void splitNode(Dimension2D box, T contents) {
			assert(this.contents == null);
			assert(this.right == null);
			assert(this.down == null);
			// Create children if they have non-zero area
			if(box.getHeight() < bounds.getHeight()) {
				down = new Bin( bounds.getX(), bounds.getY()+box.getHeight(),
						bounds.getWidth(), bounds.getHeight()-box.getHeight(), null);
			}
			if(box.getWidth() < bounds.getWidth()) {
				right = new Bin( bounds.getX() + box.getWidth(), bounds.getY(),
						bounds.getWidth() - box.getWidth(), box.getHeight(), null );
			}
			this.contents = contents;
		}

		public void getPlacements(List<Entry<T, Rectangle2D>> positions) {
			if(right == null && down == null) {
				// Contents fill bin (shouldn't happen due to splitState logic)
				if( contents != null) {
					positions.add( new SimpleEntry<T,Rectangle2D>(contents,bounds) );
				}
				return;
			}
			// Add this
			if(contents != null) {
				//add contents
				double w = bounds.getWidth();
				double h = bounds.getHeight();
				if(right != null) {
					w = Math.min(w, right.bounds.getX()-bounds.getX());
				}
				if(down != null) {
					h = Math.min(h, down.bounds.getY()-bounds.getY());
				}
				Rectangle2D.Double r = new Rectangle2D.Double(bounds.getX(),bounds.getY(),w,h);
				positions.add( new SimpleEntry<T,Rectangle2D>(contents,r) );
			}
			// Recurse
			if(right != null)
				right.getPlacements(positions);
			if(down != null)
				down.getPlacements(positions);
		}

		@Override
		public String toString() {
			return "Bin [bounds=" + bounds + ", contents=" + contents + "]";
		}

	}

	private Bin root;

	/**
	 * The arguments will be sorted by dimension
	 * @param boxes
	 * @param contents
	 */
	public BinaryBinPacker(List<Entry<Dimension2D, T>> boxes) {
		this(boxes,0,0);
	}

	public BinaryBinPacker(List<Entry<Dimension2D, T>> boxes, double w, double h) {
		if(w>0 && h>0)
			root = new Bin(0, 0, w, h, null);

		addAll(boxes);
	}

	/**
	 * @param boxes
	 */
	public void addAll(List<Entry<Dimension2D, T>> boxes) {
		List<Entry<Dimension2D, T>> sortedBoxes = sortByMaxDim(boxes);
		for(Entry<Dimension2D, T> entry : sortedBoxes) {
			add(entry.getKey(),entry.getValue());
		}
	}

	private List<Entry<Dimension2D, T>> sortByMaxDim(List<Entry<Dimension2D, T>> boxes) {
		List<Entry<Dimension2D, T>> keys = new ArrayList<Entry<Dimension2D, T>>( boxes );
		// Reverse sort maximum dimension
		Collections.sort(keys, new Comparator<Entry<Dimension2D, T>>() {
			@Override
			public int compare(Entry<Dimension2D, T> o1, Entry<Dimension2D, T> o2) {
				Dimension2D dim1 = o1.getKey();
				Dimension2D dim2 = o2.getKey();
				int c = Double.compare(
						Math.max(dim2.getHeight(),dim2.getWidth()),
						Math.max(dim1.getHeight(),dim1.getWidth())
						);
				if(c==0) {
					c = Double.compare(dim2.getHeight(), dim1.getHeight());
				}
				if(c==0) {
					c = Double.compare(dim2.getWidth(), dim1.getWidth());
				}
				return c;
			}
		});
		return keys;
	}

	/**
	 * Add a box to the first available bin.
	 * This is most effective if boxes are added in decreasing order, as with {@link #addAll(Map)}.
	 * @param box
	 * @param contents
	 */
	public void add(Dimension2D box, T contents) {
		logger.debug("Adding {} with size {},{}",contents, box.getWidth(),box.getHeight());
		if(root == null) {
			// first box
			root = new Bin(0, 0, box.getWidth(), box.getHeight(), null);
			root.splitNode(box, contents);
			return;
		}
		Bin empty = root.findNode( box);
		if( empty == null ) {
			// Expand container to fit
			empty = growNode(box);
		}
		empty.splitNode(box, contents);
	}
	/**
	 * Resizes the root to make a place for the box. Doesn't add anything.
	 * @param box
	 * @param contents
	 * @return the bin with room for the box
	 */
	private Bin growNode(Dimension2D box) {
		// Calculate dimensions of new container for vertical or horizontal layout
		Dimension2D vertical = new eppic.assembly.Dimension2D.Double(Math.max(root.bounds.getWidth(),box.getWidth()),
				root.bounds.getHeight()+box.getHeight());
		Dimension2D horizontal = new eppic.assembly.Dimension2D.Double( root.bounds.getWidth()+box.getWidth(),
				Math.max(root.bounds.getHeight(),box.getHeight()));
		// Use whichever is closer to square
		boolean useHorizontal = Math.abs( vertical.getWidth()-vertical.getHeight() ) > 
		Math.abs(horizontal.getWidth()-horizontal.getHeight());

		logger.debug("Growing root {} to {}",useHorizontal?"horizontally":"vertically",useHorizontal?horizontal:vertical);

		if( useHorizontal ) {
			Bin newRoot = new Bin(0,0,
					horizontal.getWidth(), horizontal.getHeight(), null);
			if( root.bounds.getHeight() > box.getHeight()) {
				// Expand to the right, root fills the height
				newRoot.down = root;
				newRoot.right = new Bin(root.bounds.getWidth(),0, box.getWidth(),horizontal.getHeight(), null);
			} else {
				// Expand to the right and down
				newRoot.down = new Bin(0,0,root.bounds.getWidth(),horizontal.getHeight(),null);
				newRoot.down.right = root;
				newRoot.down.down = new Bin(0,root.bounds.getHeight(),root.bounds.getWidth(),box.getHeight()-root.bounds.getHeight(),null);
				newRoot.right = new Bin(root.bounds.getWidth(),0, box.getWidth(),horizontal.getHeight(), null);
			}
			root = newRoot;
			return root.right;
		} else {
			Bin newRoot = new Bin(0,0, vertical.getWidth(),vertical.getHeight(),null);
			if( root.bounds.getWidth() > box.getWidth()) {
				// Expand down
				newRoot.right = root;
				newRoot.down = new Bin(0,root.bounds.getHeight(),vertical.getWidth(),box.getHeight(),null);
			} else {
				// Expand down and right
				newRoot.right = new Bin(0,0,vertical.getWidth(),root.bounds.getHeight(),null);
				newRoot.right.down = root;
				newRoot.right.right = new Bin(root.bounds.getWidth(),0,box.getWidth()-root.bounds.getWidth(),root.bounds.getHeight(),null);
				newRoot.down = new Bin(0,root.bounds.getHeight(),vertical.getWidth(),box.getHeight(),null);
			}
			root = newRoot;
			return root.down;
		}
	}

	public Rectangle2D getBounds() {
		return root.bounds;
	}

	public List<Entry<T, Rectangle2D>> getPlacements() {
		List<Entry<T, Rectangle2D>> positions = new ArrayList<Entry<T, Rectangle2D>>();
		root.getPlacements(positions);
		return positions;
	}



}
