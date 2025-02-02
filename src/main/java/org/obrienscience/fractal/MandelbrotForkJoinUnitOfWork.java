package org.obrienscience.fractal;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Split the image into strips down to possibly a single line
 * @author mfobrien
 */
public class MandelbrotForkJoinUnitOfWork extends RecursiveTask<Boolean> {//RecursiveAction  {
	private static final long serialVersionUID = 5386834196721056333L;
	protected static int sThreshold = 8;
	private long rowNumber;
	private long rowStart;
	private long rowEnd;
	private boolean isBaseCase;
	private long width;
	private long height;
	private BufferedImage bufferedImage;
	private MandelbrotImager imager;
    public static final BigDecimal TWO = new BigDecimal(2);
    public static final BigDecimal FOUR = new BigDecimal(4);
    public static final int BIGDECIMAL_SCALE = 64; // don't let performance degrade exponentially as size goes to infinity
    private static final MathContext context = MathContext.DECIMAL128;

	public MandelbrotForkJoinUnitOfWork(MandelbrotImager imager,BufferedImage bufferedImage,
			long rowNumber, long rowStart, long rowEnd, long height, long width) {
		this.imager =  imager;
		this.bufferedImage = bufferedImage;
		this.rowNumber = rowNumber;
		this.rowStart = rowStart; 
		this.rowEnd = rowEnd;
		this.height = height;
		this.width = width;	
	}
	
	@Override
	protected Boolean compute() {
		Boolean allMaxOrbit = true;
		if(rowNumber < sThreshold) { // don't subdivide too small so we keep overhead to a minimum
			isBaseCase = true;
		}
		// base case
		if(isBaseCase) {
			try {
				allMaxOrbit = calculate();
				//calculateBigDecimal();
				//System.out.print(".");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return allMaxOrbit;
		}
		
		// recursive case (0-1023),(0-511,512-1023),(0-255,256-511,512-767,768-1023)
		long split = (rowEnd - rowStart) >> 1;
		long splitRowNumber = rowNumber >> 1; 
		//System.out.print("f");
	    /*invokeAll(
	    		new MandelbrotForkJoinUnitOfWork(imager, bufferedImage,
	    				splitRowNumber, rowStart, rowStart + split, height, width),
   	    		new MandelbrotForkJoinUnitOfWork(imager, bufferedImage,
   	    				splitRowNumber, rowStart + split, rowEnd, height, width)
	    		);*/		
	    MandelbrotForkJoinUnitOfWork m0 = new MandelbrotForkJoinUnitOfWork(imager, bufferedImage,
	    				splitRowNumber, rowStart, rowStart + split, height, width);
	    MandelbrotForkJoinUnitOfWork m1 = new MandelbrotForkJoinUnitOfWork(imager, bufferedImage,
   	    				splitRowNumber, rowStart + split, rowEnd, height, width);
	    // if any of the threads return false then the image contains non-max-orbit data
	    return m0.invoke() && m1.invoke();
	}
		
	public Boolean calculate() {
		Boolean allMax = true;
		double h,v;
		double radiusMax = imager.getMaximumOrbitRadius();
		int orbitPath = imager.getMaximumOrbitPath();
		double xstart = imager.getGridXStart();
		double ystart = imager.getGridYStart();
		double xscale = imager.getRealPerPixelX();
		double yscale = imager.getRealPerPixelY();
		for(long x=0;x<width;x++) {
            h = xstart + xscale * x;
			for(long y=rowStart;y<rowEnd;y++) {
                v = ystart + yscale * y;
                int iter = computeJulia(h,v, radiusMax, orbitPath);
				bufferedImage.setRGB((int)x, (int)y, iter);
                if(allMax && (iter >> imager.getColorPowerMult() != imager.getMaximumOrbitPath())) { // do this only once
                	allMax = false;
                }
			}
		}
		return allMax;
	}
	
	public Boolean calculateBigDecimal() {
		Boolean allMax = true;
		BigDecimal h,v;
		BigDecimal radiusMax = BigDecimal.valueOf(imager.getMaximumOrbitRadius());
		int orbitPath = imager.getMaximumOrbitPath();
		double xstart = imager.getGridXStart();
		double ystart = imager.getGridYStart();
		double xscale = imager.getRealPerPixelX();
		double yscale = imager.getRealPerPixelY();
		for(long x=0;x<width;x++) {
            h = BigDecimal.valueOf(xstart + xscale * x);
            //h.setScale(BIGDECIMAL_SCALE); // don't allow decimal value to grow infinite
			for(long y=rowStart;y<rowEnd;y++) {
                v = BigDecimal.valueOf(ystart + yscale * y);
                //v.setScale(BIGDECIMAL_SCALE); // don't allow decimal value to grow infinite
                int iter = computeJuliaBigDecimal(h,v, radiusMax, orbitPath);
                if(iter >> imager.getColorPowerMult() != imager.getMaximumOrbitPath()) {
                	allMax = false;
                }

                bufferedImage.setRGB((int)x, (int)y, iter);
			}
		}
		return allMax;
	}
    
	public int computeJuliaBigDecimal(BigDecimal x, BigDecimal y, BigDecimal radiusMax, int pathMax) {
        int iterations = 0;
        BigDecimal real = new BigDecimal(0);
        BigDecimal imag = new BigDecimal(0);
        BigDecimal realNext = new BigDecimal(0);
        do {
            realNext = ((real.multiply(real, context)).subtract(
            		imag.multiply(imag, context), context)).add(x, context);
            imag = ((real.multiply(imag, context)).multiply(TWO, context)).add(y, context);
            real = realNext;
            iterations++;
        } while (realNext.compareTo(FOUR) < 0 && iterations < pathMax);
        return iterations << imager.getColorPowerMult();
    }	

	public int computeJulia(double x, double y, double radiusMax, int pathMax) {
        int iterations = 0;
        double real = 0;
        double imag = 0;
        double realNext = 0;
        do {
            realNext = ( real * real) - (imag * imag) + x;
            imag = (real * imag * 2) + y;
            real = realNext;
            iterations++;
		} while (realNext < 4 && iterations < pathMax);
        return iterations << imager.getColorPowerMult();
    }
}
