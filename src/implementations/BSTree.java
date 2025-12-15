package implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import utilities.BSTreeADT;
import utilities.Iterator;

/**
 * The implementation stores comparable elements, disallows duplicates, and
 * provides basic operations such as insertion, search, removal of min/max and
 * traversal iterators (in-order, pre-order, post-order).
 *
 * @param <E> element type (must implement {@link Comparable})
 */
public class BSTree<E extends Comparable<? super E>> implements BSTreeADT<E>
{
	private static final long serialVersionUID = 1L;

	/** Root node of the tree (null when empty). */
	private BSTreeNode<E> root;

	/** Number of elements stored in the tree. */
	private int size;

	/** Constructs an empty BST. */
	public BSTree()
	{
		root = null;
		size = 0;
	}

	/**
	 * Convenience constructor to create a tree with a single root element.
	 *
	 * @param element initial root element (ignored if null)
	 */
	public BSTree( E element )
	{
		this();
		if( element != null )
		{
			root = new BSTreeNode<E>( element );
			size = 1;
		}
	}

	/** Returns the root node, or throws NullPointerException when empty. */
	@Override
	public BSTreeNode<E> getRoot() throws NullPointerException
	{
		if( root == null )
		{
			throw new NullPointerException( "Tree is empty" );
		}
		return root;
	}

	/** Returns the height (number of levels) of the tree. */
	@Override
	public int getHeight()
	{
		return height( root );
	}

	/** Helper: recursively computes subtree height. */
	private int height( BSTreeNode<E> node )
	{
		if( node == null )
			return 0;
		int lh = height( node.getLeft() );
		int rh = height( node.getRight() );
		return 1 + Math.max( lh, rh );
	}

	/** Returns the number of elements in the tree. */
	@Override
	public int size()
	{
		return size;
	}

	/** Returns true when tree contains no elements. */
	@Override
	public boolean isEmpty()
	{
		return root == null;
	}

	/** Clears the tree, removing all nodes. */
	@Override
	public void clear()
	{
		root = null;
		size = 0;
	}

	/** Checks whether the tree contains the specified entry. */
	@Override
	public boolean contains( E entry ) throws NullPointerException
	{
		if( entry == null )
			throw new NullPointerException( "Null entry" );
		return search( entry ) != null;
	}

	/**
	 * Searches for a node containing the specified entry.
	 *
	 * @param entry element to find
	 * @return node containing the element or null when not found
	 */
	@Override
	public BSTreeNode<E> search( E entry ) throws NullPointerException
	{
		if( entry == null )
			throw new NullPointerException( "Null entry" );
		BSTreeNode<E> current = root;
		while( current != null )
		{
			int cmp = entry.compareTo( current.getElement() );
			if( cmp == 0 )
				return current;
			else if( cmp < 0 )
				current = current.getLeft();
			else
				current = current.getRight();
		}
		return null;
	}

	/** Inserts a new element into the BST. Duplicates are not allowed. */
	@Override
	public boolean add( E newEntry ) throws NullPointerException
	{
		if( newEntry == null )
			throw new NullPointerException( "Null entry" );
		if( root == null )
		{
			root = new BSTreeNode<E>( newEntry );
			size = 1;
			return true;
		}

		BSTreeNode<E> parent = null;
		BSTreeNode<E> current = root;
		while( current != null )
		{
			parent = current;
			int cmp = newEntry.compareTo( current.getElement() );
			if( cmp == 0 )
			{
				return false; // duplicate, not inserted
			}
			else if( cmp < 0 )
			{
				current = current.getLeft();
			}
			else
			{
				current = current.getRight();
			}
		}

		int cmp = newEntry.compareTo( parent.getElement() );
		if( cmp < 0 )
			parent.setLeft( new BSTreeNode<E>( newEntry ) );
		else
			parent.setRight( new BSTreeNode<E>( newEntry ) );

		size++;
		return true;
	}

	/** Removes and returns the node containing the smallest element in the tree. */
	@Override
	public BSTreeNode<E> removeMin()
	{
		if( root == null )
			return null;
		BSTreeNode<E> parent = null;
		BSTreeNode<E> current = root;
		while( current.getLeft() != null )
		{
			parent = current;
			current = current.getLeft();
		}
		// current is min
		if( parent == null )
		{
			// root is min
			root = current.getRight();
		}
		else
		{
			parent.setLeft( current.getRight() );
		}
		size--;
		// detach children from returned node
		current.setLeft( null );
		current.setRight( null );
		return current;
	}

	/** Removes and returns the node containing the largest element in the tree. */
	@Override
	public BSTreeNode<E> removeMax()
	{
		if( root == null )
			return null;
		BSTreeNode<E> parent = null;
		BSTreeNode<E> current = root;
		while( current.getRight() != null )
		{
			parent = current;
			current = current.getRight();
		}
		// current is max
		if( parent == null )
		{
			// root is max
			root = current.getLeft();
		}
		else
		{
			parent.setRight( current.getLeft() );
		}
		size--;
		current.setLeft( null );
		current.setRight( null );
		return current;
	}

	/** Returns an iterator that traverses the tree in in-order (sorted) order. */
	@Override
	public Iterator<E> inorderIterator()
	{
		List<E> list = new ArrayList<>();
		inorder( root, list );
		return new SimpleIterator( list );
	}

	/** Helper: collects elements from the subtree in in-order. */
	private void inorder( BSTreeNode<E> node, List<E> list )
	{
		if( node == null )
			return;
		inorder( node.getLeft(), list );
		list.add( node.getElement() );
		inorder( node.getRight(), list );
	}

	/** Returns an iterator that traverses the tree in pre-order (root first). */
	@Override
	public Iterator<E> preorderIterator()
	{
		List<E> list = new ArrayList<>();
		preorder( root, list );
		return new SimpleIterator( list );
	}

	/** Helper: collects elements from the subtree in pre-order. */
	private void preorder( BSTreeNode<E> node, List<E> list )
	{
		if( node == null )
			return;
		list.add( node.getElement() );
		preorder( node.getLeft(), list );
		preorder( node.getRight(), list );
	}

	/** Returns an iterator that traverses the tree in post-order (root last). */
	@Override
	public Iterator<E> postorderIterator()
	{
		List<E> list = new ArrayList<>();
		postorder( root, list );
		return new SimpleIterator( list );
	}

	/** Helper: collects elements from the subtree in post-order. */
	private void postorder( BSTreeNode<E> node, List<E> list )
	{
		if( node == null )
			return;
		postorder( node.getLeft(), list );
		postorder( node.getRight(), list );
		list.add( node.getElement() );
	}

	/**
	 * Simple iterator implementation used by the traversal methods. The
	 * iterator makes a defensive copy of the traversal list so it is safe to
	 * use after the tree is modified.
	 */
	private class SimpleIterator implements Iterator<E>
	{
		private final List<E> data;
		private int index = 0;

		public SimpleIterator( List<E> data )
		{
			// defensive copy
			this.data = new ArrayList<>( data );
		}

		/** Returns true if there are remaining elements in the iteration. */
		@Override
		public boolean hasNext()
		{
			return index < data.size();
		}

		/** Returns the next element in the iteration. */
		@Override
		public E next() throws NoSuchElementException
		{
			if( !hasNext() )
				throw new NoSuchElementException();
			return data.get( index++ );
		}
	}
}
