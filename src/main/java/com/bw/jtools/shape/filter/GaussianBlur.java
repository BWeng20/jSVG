package com.bw.jtools.shape.filter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Gaussian blur filter.
 */
public class GaussianBlur extends FilterBaseSingleSource
{
	double stdDeviationX_;
	double stdDeviationY_;

	@Override
	protected Dimension getTargetDimension(int srcWidth, int srcHeight, double scaleX, double scaleY)
	{
		Dimension targetDimension = new Dimension((int) (.5 + srcWidth + stdDeviationX_ * scaleX * 4),
				(int) (.5 + srcHeight + stdDeviationY_ * scaleY * 4));
		return targetDimension;
	}

	@Override
	protected Point2D.Double getOffset(double scaleX, double scaleY)
	{
		return new Point2D.Double(-stdDeviationX_ * scaleX * 2, -stdDeviationY_ * scaleY * 2);
	}

	@Override
	protected void render(PainterBuffers buffers, String targetName, BufferedImage src, BufferedImage target, double scaleX, double scaleY)
	{

		int kernelWidth = (int) (.5 + stdDeviationX_ * scaleX * 2);
		int kernelHeight = (int) (.5 + stdDeviationY_ * scaleY * 2);

		if (kernelWidth < 1) kernelWidth = 1;
		if (kernelHeight < 1) kernelHeight = 1;

		int targetWidth = target.getWidth();
		int targetHeight = target.getHeight();

		int xOff = (targetWidth - src.getWidth()) / 2;
		int yOff = (targetHeight - src.getHeight()) / 2;

		System.out.println("Gauss target:" + targetName + " [" + targetWidth + "x" + targetHeight
				+ "] src:[" + src.getWidth() + "x" + src.getHeight() + "] kernel:[" + kernelWidth + "x" + kernelHeight + "]");


		float[] kernelX = new float[kernelWidth];
		float[] kernelY = new float[kernelHeight];

		initKernel(kernelX, scaleX * stdDeviationX_);
		initKernel(kernelY, scaleY * stdDeviationY_);

		// A full kernel would be much too slow for higher derivation.
		// Gaussian blur can be implemented by two small kernels.
		// This needs two temporary buffers
		BufferedImage buffer1 = buffers.getTemporaryBuffer(0, targetWidth, targetHeight);
		BufferedImage buffer2 = buffers.getTemporaryBuffer(1, targetWidth, targetHeight);

		ConvolveOp hBlur = new ConvolveOp(new Kernel(kernelWidth, 1, kernelX), ConvolveOp.EDGE_NO_OP, null);
		ConvolveOp vBlur = new ConvolveOp(new Kernel(1, kernelHeight, kernelY), ConvolveOp.EDGE_NO_OP, null);

		Graphics2D g2d = buffer1.createGraphics();
		try
		{
			// The filter will not expand the edges as needed if used on source directly.
			// We need to copy the source to a larger temporary buffer.
			g2d.setBackground(new Color(255, 255, 255, 0));
			g2d.clearRect(0, 0, buffer1.getWidth(), buffer1.getHeight());
			g2d.drawImage(src, xOff, yOff, null);
			// Uncomment to debug the buffer sizes:
			g2d.setColor(Color.BLUE);
			g2d.drawRect(1, 1, buffer1.getWidth() - 2, buffer1.getHeight() - 2);
		}
		finally
		{
			g2d.dispose();
		}
		hBlur.filter(buffer1, buffer2);
		vBlur.filter(buffer2, target);
	}

	private void initKernel(float[] kernel, double stdDeviation)
	{
		final double sX = 2.0 * stdDeviation * stdDeviation;
		final int radius = kernel.length / 2;
		final double piSX = (float) (Math.PI * sX);

		double sum = 0.0;

		int i2 = kernel.length - 1;
		for (int i1 = 0; i1 <= i2; ++i1, --i2)
		{
			int d = i1 - radius;
			sum += kernel[i2] = kernel[i1] = (float) ((Math.exp(-(d * d) / sX)) / piSX);
		}
		sum = sum * 2 - kernel[i2 + 1];
		for (int i = 0; i < kernel.length; ++i)
			kernel[i] /= sum;
	}


	/**
	 * Create a new Gaussian Blur filter.
	 * stdDeviation_ sigma = standard deviation ^2
	 */
	public GaussianBlur(String source, String target, double stdDeviationX, double stdDeviationY)
	{
		super(source, target);
		stdDeviationX_ = stdDeviationX;
		stdDeviationY_ = stdDeviationY;
	}
}

