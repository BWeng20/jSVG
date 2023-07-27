

package com.bw.jtools.svg.css;

/**
 * Helper to simplify calculation of CSS Selector Specificity.
 */
public final class Specificity
{
	private int A;
	private int B;
	private int C;

	public static final Specificity MAX;
	public static final Specificity MIN;

	static
	{
		MAX = new Specificity();
		MAX.A = Integer.MAX_VALUE;
		MIN = new Specificity();
		MIN.A = Integer.MIN_VALUE;
	}

	public Specificity()
	{
	}

	public Specificity(Specificity other)
	{
		setTo(other);
	}

	public void setTo(Specificity other)
	{
		A = other.A;
		B = other.B;
		C = other.C;
	}

	/**
	 * Checks if more of equal.<br>
	 * For two selectors with same specificity th elater definition should be used.
	 */
	public boolean isMoreSpecificOrEqual(Specificity other)
	{
		return (A > other.A) || ((A == other.A) && ((B > other.B) || ((B == other.B) && (C >= other.C))));
	}

	public void addIdMatch()
	{
		if (A < Integer.MAX_VALUE)
			++A;
	}

	public void addClassMatch()
	{
		++B;
	}

	public void addAttributeMatch()
	{
		++B;
	}

	public void addPseudoClassMatch()
	{
		++B;
	}

	public void addTagMatch()
	{
		++C;
	}

	public void addPseudoElementMatch()
	{
		++C;
	}
}
