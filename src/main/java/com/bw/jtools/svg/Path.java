package com.bw.jtools.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

/**
 * Parse a path expression.<br>
 * Used references:
 * https://www.w3.org/TR/SVG11/implnote.html#PathElementImplementationNotes
 */
public final class Path extends Parser
{
	private double lastXCtrl, lastYCtrl;
	private double cx, cy;

	private Path2D.Double path_;

	public Path2D getPath()
	{
		return path_;
	}

	public Path(String content)
	{
		super(content);

		path_ = new Path2D.Double();
		char cmd = 0;
		double x, y;
		double lastMx = 0, lastMy = 0;
		do
		{
			consumeSeparators();
			char nextCmd = nextChar();
			if (cmd == 0 || !(isDigit(nextCmd) || nextCmd == '+' || nextCmd == '-' || nextCmd == '.'))
				cmd = nextCmd;
			else
			{
				if (cmd == 'M') cmd = 'L';
				else if (cmd == 'm') cmd = 'l';

				--idx_;
			}

			boolean resetCtrl = true;
			switch (cmd)
			{
				case 'M':
					path_.moveTo(nextXOrdinate(), nextYOrdinate());
					lastMx = cx;
					lastMy = cy;
					break;
				case 'm':
					path_.moveTo(nextXRelative(), nextYRelative());
					lastMx = cx;
					lastMy = cy;
					break;
				case 'L':
					path_.lineTo(nextXOrdinate(), nextYOrdinate());
					break;
				case 'l':
					path_.lineTo(nextXRelative(), nextYRelative());
					break;

				case 'H':
					path_.lineTo(nextXOrdinate(), cy);
					break;
				case 'h':
					path_.lineTo(nextXRelative(), cy);
					break;
				case 'V':
					path_.lineTo(cx, nextYOrdinate());
					break;
				case 'v':
					path_.lineTo(cx, nextYRelative());
					break;

				case 'C':
					path_.curveTo(nextXCtrl(), nextYCtrl(), nextXCtrl(), nextYCtrl(),
							nextXOrdinate(), nextYOrdinate());
					resetCtrl = false;
					break;
				case 'c':
					path_.curveTo(nextXCtrlRelativeStay(), nextYCtrlRelativeStay(),
							nextXCtrlRelativeStay(), nextYCtrlRelativeStay(),
							nextXRelative(), nextYRelative());
					resetCtrl = false;
					break;

				case 'S':
					// project last ctrl point across last end point (cx,cy)
					path_.curveTo(nextXSymmetricCtrl(), nextYSymmetricCtrl(), nextXCtrl(), nextYCtrl(), nextXOrdinate(), nextYOrdinate());
					resetCtrl = false;
					break;
				case 's':
					// project last ctrl point across last end point (cx,cy)
					path_.curveTo(nextXSymmetricCtrl(), nextYSymmetricCtrl(), nextXCtrlRelativeStay(), nextYCtrlRelativeStay(), nextXRelative(), nextYRelative());
					resetCtrl = false;
					break;

				case 'Q':
					path_.quadTo(nextXCtrl(), nextYCtrl(), nextXOrdinate(), nextYOrdinate());
					resetCtrl = false;
					break;
				case 'q':
					path_.quadTo(nextXCtrlRelativeStay(), nextYCtrlRelativeStay(), nextXRelative(), nextYRelative());
					resetCtrl = false;
					break;

				case 'T':
					// project last ctrl point across last end point (cx,cy)
					path_.quadTo(nextXSymmetricCtrl(), nextYSymmetricCtrl(), nextXOrdinate(), nextYOrdinate());
					resetCtrl = false;
					break;
				case 't':
					// project last ctrl point across last end point (cx,cy)
					path_.quadTo(nextXSymmetricCtrl(), nextYSymmetricCtrl(), nextXRelative(), nextYRelative());
					resetCtrl = false;
					break;

				// arc
				case 'A':
					arc(nextDouble(), nextDouble(), nextDouble(), nextFlag(), nextFlag(),
							cx, cy, x = nextDouble(), y = nextDouble());
					cx = x;
					cy = y;
					break;
				case 'a':
					arc(nextDouble(), nextDouble(), nextDouble(), nextFlag(), nextFlag(),
							cx, cy, x = nextXRelativeStay(), y = nextYRelativeStay());
					cx = x;
					cy = y;
					break;

				case 'Z':
				case 'z':
					path_.closePath();
					cx = lastMx;
					cy = lastMy;
					break;
				case 0:
					break;
				default:
					throw new IllegalArgumentException("Unknown path command '" + cmd + "' at " + idx_);
			}
			if (resetCtrl)
			{
				lastXCtrl = cx;
				lastYCtrl = cy;
			}
		} while (cmd != 0);
	}

	/**
	 * As Path2D has no "arc" function and Arc2D works much differently,
	 * we need to calculate an arc, rotate it and append it to the path manually.<br>
	 * Used as reference:
	 * <ol>
	 * <li>Implementation notes for "arc" in https://www.w3.org/TR/SVG11/implnote.html#PathElementImplementationNotes.</li>
	 * <li>SVG Salamander Project (com.kitfox.svg.pathcmd.Arc). </li>
	 * <li>Apache Batik (org.apache.batik.ext.awt.geom.ExtendedGeneralPath).</li>
	 * </ol>
	 * Salamander and Batik implementations look <i>very</i> similar and are both based on the SVG implementation notes.
	 */
	public void arc(double rx, double ry,
					double angle,
					boolean largeArcFlag,
					boolean sweepFlag,
					double x0, double y0,
					double x1, double y1)
	{
		if (rx == 0 || ry == 0)
			path_.lineTo(x1, y1);
		else
		{
			if (x0 != x1 || y0 != y1)
			{
				Arc2D.Double arc = new Arc2D.Double();
				angle = Math.toRadians(angle % 360.0);

				double dx2 = (x0 - x1) / 2.0;
				double dy2 = (y0 - y1) / 2.0;
				double cosAngle = Math.cos(angle);
				double sinAngle = Math.sin(angle);

				final double x11 = (cosAngle * dx2 + sinAngle * dy2);
				final double y11 = (-sinAngle * dx2 + cosAngle * dy2);
				rx = Math.abs(rx);
				ry = Math.abs(ry);
				double prx = rx * rx;
				double pry = ry * ry;
				double px1 = x11 * x11;
				double py1 = y11 * y11;
				double check = px1 / prx + py1 / pry;
				if (check > 0.99999)
				{
					double rscale = Math.sqrt(check) * 1.00001;
					rx *= rscale;
					ry *= rscale;
					prx = rx * rx;
					pry = ry * ry;
				}

				double sq = ((prx * pry) - (prx * py1) - (pry * px1)) / ((prx * py1) + (pry * px1));
				sq = (sq < 0) ? 0 : sq;
				double coef = (largeArcFlag == sweepFlag) ? -Math.sqrt(sq) : Math.sqrt(sq);
				double cx1 = coef * ((rx * y11) / ry);
				double cy1 = coef * -((ry * x11) / rx);

				arc.x = ((x0 + x1) / 2.0) + (cosAngle * cx1 - sinAngle * cy1) - rx;
				arc.y = ((y0 + y1) / 2.0) + (sinAngle * cx1 + cosAngle * cy1) - ry;
				arc.width = rx * 2.0;
				arc.height = ry * 2.0;

				final double ux = (x11 - cx1) / rx;
				final double uy = (y11 - cy1) / ry;
				final double vx = (-x11 - cx1) / rx;
				final double vy = (-y11 - cy1) / ry;

				arc.start = -(Math.toDegrees(((uy < 0) ? -1.0 : 1.0) * Math.acos(ux / Math.sqrt((ux * ux) + (uy * uy)))) % 360f);

				final double n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
				final double p = ux * vx + uy * vy;
				double extend = Math.toDegrees(((ux * vy - uy * vx < 0) ? -1.0 : 1.0) * Math.acos(p / n));
				if (!sweepFlag && extend > 0)
					extend -= 360f;
				else if (sweepFlag && extend < 0)
					extend += 360f;
				extend %= 360f;

				arc.extent = -extend;

				AffineTransform t = AffineTransform.getRotateInstance(angle, arc.x + rx, arc.y + ry);
				path_.append(t.createTransformedShape(arc), true);
			}
		}
	}


	/**
	 * Get next control point x ordinate from arguments.
	 */
	private double nextXCtrl()
	{
		return lastXCtrl = nextXOrdinate();
	}

	/**
	 * Get next control point y ordinate from arguments.
	 */
	private double nextYCtrl()
	{
		return lastYCtrl = nextYOrdinate();
	}

	/**
	 * Get next control point x relative ordinate from arguments.
	 */
	private double nextXCtrlRelative()
	{
		return lastXCtrl = nextXRelative();
	}

	/**
	 * Get next control point y relative ordinate from arguments.
	 */
	private double nextYCtrlRelative()
	{
		return lastYCtrl = nextYRelative();
	}

	/**
	 * Get next control point x relative ordinate but keep last reference point.
	 */
	private double nextXCtrlRelativeStay()
	{
		return lastXCtrl = nextXRelativeStay();
	}

	/**
	 * Get next control point y relative ordinate but keep last reference point.
	 */
	private double nextYCtrlRelativeStay()
	{
		return lastYCtrl = nextYRelativeStay();
	}

	/**
	 * Get next x ordinate.
	 */
	private double nextXOrdinate()
	{
		return cx = nextDouble();
	}

	/**
	 * Get next y ordinate.
	 */
	private double nextYOrdinate()
	{
		return cy = nextDouble();
	}

	/**
	 * Get next x relative ordinate and use the new point as next reverence.
	 */
	private double nextXRelative()
	{
		return cx += nextDouble();
	}

	/**
	 * Get next y relative ordinate and use the new point as next reverence.
	 */
	private double nextYRelative()
	{
		return cy += nextDouble();
	}

	/**
	 * Get next x relative ordinate but keep last reference point.
	 */
	private double nextXRelativeStay()
	{
		return cx + nextDouble();
	}

	/**
	 * Get next y relative ordinate but keep last reference point.
	 */
	private double nextYRelativeStay()
	{
		return cy + nextDouble();
	}

	/**
	 * Get next x control ordinate by projecting the last x control ordinate across the
	 * last x ordinate.
	 */
	private double nextXSymmetricCtrl()
	{
		return cx + (cx - lastXCtrl);
	}

	/**
	 * Get next y control ordinate by projecting the last y control ordinate across the
	 * last y ordinate.
	 */
	private double nextYSymmetricCtrl()
	{
		return cy + (cy - lastYCtrl);
	}
}
