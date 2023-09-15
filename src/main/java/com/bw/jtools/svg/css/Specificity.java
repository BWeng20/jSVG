package com.bw.jtools.svg.css;

/**
 * Helper to simplify calculation of CSS Selector Specificity.
 */
public final class Specificity
{
	private int A;
	private int B;
	private int C;

	/**
	 * Maximum specificity.
	 */
	public static final Specificity MAX;

	/**
	 * Minimal specificity.
	 */
	public static final Specificity MIN;

	static
	{
		MAX = new Specificity();
		MAX.A = Integer.MAX_VALUE;
		MIN = new Specificity();
		MIN.A = Integer.MIN_VALUE;
	}

	/**
	 * Creates a new instance with zero values.
	 */
	public Specificity()
	{
	}

	/**
	 * Creates a new instance and copies values.
	 *
	 * @param other Other specificity to use as template.
	 */
	public Specificity(Specificity other)
	{
		setTo(other);
	}

	/**
	 * Copies values from other to this instance.
	 *
	 * @param other Other specificity to use as template.
	 */
	public void setTo(Specificity other)
	{
		A = other.A;
		B = other.B;
		C = other.C;
	}

	/**
	 * Checks if more of equal.<br>
	 * For two selectors with same specificity th elater definition should be used.
	 *
	 * @param other Other specificity to compare.
	 * @return true if specificity is higher or equal.
	 */
	public boolean isMoreSpecificOrEqual(Specificity other)
	{
		return (A > other.A) || ((A == other.A) && ((B > other.B) || ((B == other.B) && (C >= other.C))));
	}

	/**
	 * Increase id match counter.
	 */
	public void addIdMatch()
	{
		if (A < Integer.MAX_VALUE)
			++A;
	}

	/**
	 * Increase class match counter.
	 */
	public void addClassMatch()
	{
		++B;
	}

	/**
	 * Increase attribute match counter.
	 */
	public void addAttributeMatch()
	{
		++B;
	}

	/**
	 * Increase pseudo class match counter.
	 */
	public void addPseudoClassMatch()
	{
		++B;
	}

	/**
	 * Increase tag match counter.
	 */
	public void addTagMatch()
	{
		++C;
	}

	/**
	 * Increase pseudo-element match counter.
	 */
	public void addPseudoElementMatch()
	{
		++C;
	}
}
