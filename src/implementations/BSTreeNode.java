package implementations;

import java.io.Serializable;

/**
 * Node used by the {@link BSTree} implementation.
 *
 * @param <E> element type stored in the node
 */
public class BSTreeNode<E> implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** The value stored in this node. */
	private E element;

	/** Left child (values &lt; element). */
	private BSTreeNode<E> left;

	/** Right child (values &gt; element). */
	private BSTreeNode<E> right;

	/**
	 * Constructs a new node containing the specified element. Children are
	 * initially null.
	 *
	 * @param element the element to store in the node
	 */
	public BSTreeNode( E element )
	{
		this.element = element;
		this.left = null;
		this.right = null;
	}

	/** Returns the element stored in this node. */
	public E getElement()
	{
		return element;
	}

	/** Replaces the element stored in this node. */
	public void setElement( E element )
	{
		this.element = element;
	}

	/** Returns the left child (may be null). */
	public BSTreeNode<E> getLeft()
	{
		return left;
	}

	/** Sets the left child reference. */
	public void setLeft( BSTreeNode<E> left )
	{
		this.left = left;
	}

	/** Returns the right child (may be null). */
	public BSTreeNode<E> getRight()
	{
		return right;
	}

	/** Sets the right child reference. */
	public void setRight( BSTreeNode<E> right )
	{
		this.right = right;
	}
}
