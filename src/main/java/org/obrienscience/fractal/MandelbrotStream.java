package org.obrienscience.fractal;

import java.math.BigDecimal;

public class MandelbrotStream {
	
	public void compute() {
		// example mandelbrot set
		BigDecimal startX = new BigDecimal(-2.0);
		BigDecimal endX = new BigDecimal(2.0);
		BigDecimal startY = new BigDecimal(-1.5);
		BigDecimal endY = new BigDecimal(1.5);
		Long currentX = 0L;
		Long currentY = 0L;
		Long pixelsX = 1024L;
		Long pixelsY = 1024L;
		BigDecimal currentZ = new BigDecimal(0);
		BigDecimal maxOrbit = new BigDecimal(2.0);
		Long maxCount = 0L;
		
		BigDecimal addX = endX.subtract(startX).divide(new BigDecimal(pixelsX));
		BigDecimal addY = endY.subtract(startY).divide(new BigDecimal(pixelsY));
		for(long y=0; y < pixelsY; y++) {
			
			for(long x=0;x < pixelsX; x++) {
				long count=0;
				
				while(count < 16384 || currentZ.compareTo(maxOrbit) < 0) {
					count++;
					
				}
				if(count > maxCount) {
					maxCount = count;
					System.out.println(maxCount);
				}
			}
		}
		
	}

	public static void main(String[] args) {
		MandelbrotStream mandel = new MandelbrotStream();
		mandel.compute();

	}

}
