package com.bw.jtools.shape.filter;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

/**
 * Calculates bump-map-data from an image.<br/>
 * Will be used in Specular Lighting filters.
 */
public class BumpImage
{

	public BumpImage(BufferedImage i)
	{
		this.img = i;
		cm = i.getColorModel();
		r = i.getData();
		outData = r.getDataElements(0, 0, null);
	}

	private final BufferedImage img;
	private final ColorModel cm;
	private final Raster r;
	private final Object outData;

	public int alphaAt(int x, int y)
	{
		return cm.getAlpha(r.getDataElements(x, y, outData));
	}

	static final int X = 0;
	static final int Y = 1;
	static final int Z = 2;

	public double[] surfaceNormalAt(int x, int y, double scale)
	{
		double[] normal = new double[3];
		double fx = -scale / 255.0, fy = -scale / 255.0;
		normal[Z] = 1.0;
		int _h = img.getHeight();
		int _w = img.getWidth();
		if (x == 0)
		{
			if (y == 0)
			{
				fx *= (2.0 / 3.0);
				fy *= (2.0 / 3.0);
				double p00 = alphaAt(x, y);
				double p10 = alphaAt(x + 1, y);
				double p01 = alphaAt(x, y + 1);
				double p11 = alphaAt(x + 1, y + 1);
				normal[X] = -2.0 * p00 + 2.0 * p10 - 1.0 * p01 + 1.0 * p11;
				normal[Y] = -2.0 * p00 - 1.0 * p10 + 2.0 * p01 + 1.0 * p11;
			}
			else if (y == (_h - 1))
			{
				fx *= (2.0 / 3.0);
				fy *= (2.0 / 3.0);
				double p00 = alphaAt(x, y - 1);
				double p10 = alphaAt(x + 1, y - 1);
				double p01 = alphaAt(x, y);
				double p11 = alphaAt(x + 1, y);
				normal[X] = -1.0 * p00 + 1.0 * p10 - 2.0 * p01 + 2.0 * p11;
				normal[Y] = -2.0 * p00 - 1.0 * p10 + 2.0 * p01 + 1.0 * p11;
			}
			else
			{
				fx *= (1.0 / 2.0);
				fy *= (1.0 / 3.0);
				double p00 = alphaAt(x, y - 1);
				double p10 = alphaAt(x + 1, y - 1);
				double p01 = alphaAt(x, y);
				double p11 = alphaAt(x + 1, y);
				double p02 = alphaAt(x, y + 1);
				double p12 = alphaAt(x + 1, y + 1);
				normal[X] = -1.0 * p00 + 1.0 * p10 - 2.0 * p01 + 2.0 * p11 - 1.0 * p02 + 1.0 * p12;
				normal[Y] = -2.0 * p00 - 1.0 * p10 + 0.0 * p01 + 0.0 * p11 + 2.0 * p02 + 1.0 * p12;
			}
		}
		else if (x == (_w - 1))
		{
			// rightmost column
			if (y == 0)
			{
				// top right corner
				fx *= (2.0 / 3.0);
				fy *= (2.0 / 3.0);
				double p00 = alphaAt(x - 1, y);
				double p10 = alphaAt(x, y);
				double p01 = alphaAt(x - 1, y + 1);
				double p11 = alphaAt(x, y + 1);
				normal[X] = -2.0 * p00 + 2.0 * p10 - p01 + p11;
				normal[Y] = -1.0 * p00 - 2.0 * p10 + p01 + 2.0 * p11;
			}
			else if (y == (_h - 1))
			{
				// bottom right corner
				fx *= (2.0 / 3.0);
				fy *= (2.0 / 3.0);
				double p00 = alphaAt(x - 1, y - 1);
				double p10 = alphaAt(x, y - 1);
				double p01 = alphaAt(x - 1, y);
				double p11 = alphaAt(x, y);
				normal[X] = -1.0 * p00 + 1.0 * p10 - 2.0 * p01 + 2.0 * p11;
				normal[Y] = -1.0 * p00 - 2.0 * p10 + 1.0 * p01 + 2.0 * p11;
			}
			else
			{
				// rightmost column
				fx *= (1.0 / 2.0);
				fy *= (1.0 / 3.0);
				double p00 = alphaAt(x - 1, y - 1);
				double p10 = alphaAt(x, y - 1);
				double p01 = alphaAt(x - 1, y);
				double p11 = alphaAt(x, y);
				double p02 = alphaAt(x - 1, y + 1);
				double p12 = alphaAt(x, y + 1);
				normal[X] = -1.0 * p00 + 1.0 * p10 - 2.0 * p01 + 2.0 * p11 - 1.0 * p02 + 1.0 * p12;
				normal[Y] = -1.0 * p00 - 2.0 * p10 + 0.0 * p01 + 0.0 * p11 + 1.0 * p02 + 2.0 * p12;
			}
		}
		else
		{
			// interior
			if (y == 0)
			{
				// top row
				fx *= (1.0 / 3.0);
				fy *= (1.0 / 2.0);
				double p00 = alphaAt(x - 1, y);
				double p10 = alphaAt(x, y);
				double p20 = alphaAt(x + 1, y);
				double p01 = alphaAt(x - 1, y + 1);
				double p11 = alphaAt(x, y + 1);
				double p21 = alphaAt(x + 1, y + 1);
				normal[X] = -2.0 * p00 + 0.0 * p10 + 2.0 * p20 - 1.0 * p01 + 0.0 * p11 + 1.0 * p21;
				normal[Y] = -1.0 * p00 - 2.0 * p10 - 1.0 * p20 + 1.0 * p01 + 2.0 * p11 + 1.0 * p21;
			}
			else if (y == (_h - 1))
			{
				// bottom row
				fx *= (1.0 / 3.0);
				fy *= (1.0 / 2.0);
				double p00 = alphaAt(x - 1, y - 1);
				double p10 = alphaAt(x, y - 1);
				double p20 = alphaAt(x + 1, y - 1);
				double p01 = alphaAt(x - 1, y);
				double p11 = alphaAt(x, y);
				double p21 = alphaAt(x + 1, y);
				normal[X] = -1.0 * p00 + 0.0 * p10 + 1.0 * p20 - 2.0 * p01 + 0.0 * p11 + 2.0 * p21;
				normal[Y] = -1.0 * p00 - 2.0 * p10 - 1.0 * p20 + 1.0 * p01 + 2.0 * p11 + 1.0 * p21;
			}
			else
			{
				fx *= (1.0 / 4.0);
				fy *= (1.0 / 4.0);
				double p00 = alphaAt(x - 1, y - 1);
				double p10 = alphaAt(x, y - 1);
				double p20 = alphaAt(x + 1, y - 1);
				double p01 = alphaAt(x - 1, y);
				double p21 = alphaAt(x + 1, y);
				double p02 = alphaAt(x - 1, y + 1);
				double p12 = alphaAt(x, y + 1);
				double p22 = alphaAt(x + 1, y + 1);
				normal[X] = -1.0 * p00 + 0.0 * p10 + 1.0 * p20 - 2.0 * p01 + 2.0 * p21 - 1.0 * p02 + 0.0 * p12 + 1.0 * p22;
				normal[Y] = -1.0 * p00 - 2.0 * p10 - 1.0 * p20 + 0.0 * p01 + 0.0 * p21 + 1.0 * p02 + 2.0 * p12 + 1.0 * p22;
			}
		}
		normal[X] *= fx;
		normal[Y] *= fy;
		normalize_vector(normal);
		return normal;
	}

	static double norm(double[] v)
	{
		return Math.sqrt(v[X] * v[X] + v[Y] * v[Y] + v[Z] * v[Z]);
	}

	static void normalize_vector(double[] v)
	{
		final double nv = norm(v);
		for (int i = 0; i < 3; ++i)
			v[i] /= nv;
	}

}
