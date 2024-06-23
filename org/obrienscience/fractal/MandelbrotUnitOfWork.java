package org.obrienscience.fractal;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class MandelbrotUnitOfWork implements Runnable {
	private Mandelbrot mandelbrotManager;
	private int threadIndex;
	private boolean isThreadIndexOdd = false;
	private AtomicLong rowStart;
	private AtomicLong rowEnd;

	//public MandelbrotUnitOfWork() {    	}
	public MandelbrotUnitOfWork(Mandelbrot mandelbrotManager, int threadIndex, int rowStart, int rowEnd) {
	    //this();
	    synchronized (this) {
	        this.mandelbrotManager = mandelbrotManager;
	        this.threadIndex = threadIndex;	    
	        this.rowStart = new AtomicLong(rowStart);
	        this.rowEnd = new AtomicLong(rowEnd);
	        if((threadIndex % 2) > 0) {
	            isThreadIndexOdd = true;
	        }
	    }	    
	}
	
    public static final BigDecimal TWO = new BigDecimal(2);
    public int computeJuliaBigDecimal(BigDecimal x, BigDecimal y, BigDecimal radiusMax, int pathMax) {
        int iterations = 0;
        BigDecimal real = new BigDecimal(0);
        BigDecimal imag = new BigDecimal(0);
        BigDecimal realNext = new BigDecimal(0);
        //double realSquared = real * real; // truncated 
        //double imagSquared = imag * imag; // truncated
        //while (Math.sqrt(realSquared + imagSquared) < radiusMax && iterations < pathMax) {
        //while (Math.sqrt((real.multiply(real).add(imag.multiply(imag)))).compareTo(radiusMax) < 0 && iterations < pathMax) {
            // use complex plane
            //realNext = realSquared - imagSquared + x;
            realNext = real.multiply(real).subtract(imag.multiply(imag)).add(x);
            imag = (real.multiply(imag).multiply(TWO)).add(y);
            real = realNext;
            iterations++;
        //}
        return iterations;
    }
	
	public int computeJulia(double x, double y, double radiusMax, int pathMax) {
        int iterations = 0;
        double real = 0;
        double imag = 0;
        double realNext = 0;
        //double realSquared = real * real; // truncated 
        //double imagSquared = imag * imag; // truncated
        //while (Math.sqrt(realSquared + imagSquared) < radiusMax && iterations < pathMax) {
        //while (Math.sqrt((real * real) + (imag * imag)) < radiusMax && iterations < pathMax) {
        double real2;
        double imag2;
        do {
            real2 = real * real;
            imag2 = imag * imag;
        //while ((real2 + imag2) < 4 && iterations < pathMax) {
        		// use complex plane        	
            //realNext = realSquared - imagSquared + x;
            realNext = real2 - imag2 + x;
            imag = (real * imag * 2) + y;
            real = realNext;
            iterations++;
        } while ((real2 + imag2) < 4 && iterations < pathMax);
        return iterations;
    }

	// Either iterations, hv, xy or start/end
	public void run() {
        double h,v;
        int iterations;
        long start = rowStart.get();
        long end = rowEnd.get();
        //for(long x=0;x<12;x++) {//mandelbrotManager.getExtentX();x++) {
        Color color;
        for(long x=0;x<mandelbrotManager.getExtentX();x++) {
            h = mandelbrotManager.getGridXStart() + mandelbrotManager.getRealPerPixelX() * x;
            for(long y=start;y<end;y++) {
                v = mandelbrotManager.getGridYStart() + mandelbrotManager.getRealPerPixelY() * y;
                // not the iterations
                iterations = computeJulia(h,v, mandelbrotManager.getMaximumOrbitRadius(), mandelbrotManager.getMaximumOrbitPath());
                //iterations = 0;
                if(null != mandelbrotManager.getgContext()) {
                    //synchronized(this) {
                    	//Color color, color2;
                	
                        //if(isThreadIndexOdd) {
                        	//color = Mandelbrot.getCurrentColors().get(iterations);
                	
                        	//color2 = color;
                        //} else {
                        	//color = Mandelbrot.getCurrentColors().get(mandelbrotManager.getMaximumOrbitPath() - iterations);
                        	//color2 = color;
                        //}
                        // these 2 lines need to be executed atomically - however we do not control the shared graphics context
                        /**
                         * The solution may be to write to an offscreen grid first
                         */
                        //boolean colorChanged = true;
                        //int rewrites = 0;
//                        synchronized (color) { // this will not help us with the drawRect() call
//                        	do {
                        	mandelbrotManager.getgContext().setColor(new Color(iterations));//color);
                        	// drawRect is not atomic, the color of the context may change before the pixel is written by another thread
                        	mandelbrotManager.getgContext().drawRect((int)x,(int)y,0,0);
                        	//mandelbrotManager.getBufferedImage().setRGB();

/*                            if(color2 != mandelbrotManager.getgContext().getColor()) {
                            	rewrites++;
                            	//colorChanged = false; 
                            	if(rewrites > 100) {
                            		//System.out.println("_Thread contention: color was changed mid-function: (thread,writes,x,y) " + threadIndex + "," + rewrites + "," + x + "," + y);
                            		
                            	}
                            } else {
                            	colorChanged = false; 
                            }
                        } while (colorChanged);
                        }
                       //}
*/                    }
                //}
            }
        }
        // notify the host thread that we are done
        mandelbrotManager.setThreadComplete(threadIndex, true);
        // destroy all fields to aide in Thread.exit()
        //mandelbrotManager = null;
	}

    public Mandelbrot getMandelbrotManager() {		return mandelbrotManager;	}
	public void setMandelbrotManager(Mandelbrot mandelbrotManager) {		this.mandelbrotManager = mandelbrotManager;	}
	public int getThreadIndex() {        return threadIndex;    }
    public void setThreadIndex(int threadIndex) {        this.threadIndex = threadIndex;    }
    
    /**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
