package com.bw.jtools.shape.filter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class GaussianBlurTest
{
	@Test
	public void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{

		Method initKernel = GaussianBlur.class.getDeclaredMethod("initKernel", float[].class, double.class);
		initKernel.setAccessible(true);
		GaussianBlur gb = new GaussianBlur(FilterBase.SOURCE, "Target", 1, 1);

		for (int s = 0; s < 3; ++s)
		{
			float[] kernelX = new float[s * 4 + 1];
			initKernel.invoke(gb, kernelX, s);
			double sum = 0;
			for (int i = 0; i < kernelX.length; i++)
			{
				System.out.print(kernelX[i] + " ");
				sum += kernelX[i];
			}
		}
	}
}