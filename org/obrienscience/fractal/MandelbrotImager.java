package org.obrienscience.fractal;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import javax.imageio.ImageIO;

public class MandelbrotImager {

    public static final int CORES = 6;//32;//Runtime.getRuntime().availableProcessors() << 8;
    private ForkJoinPool mapReducePool = new ForkJoinPool(CORES);
    public static List<Color> rgbColors;
    public static List<Color> currentColors;
    private double radius = 10;
    
	/*static {
        rgbColors = new ArrayList<Color>();
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(0,0,255-i));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(0,255-i,0));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(i,0,0));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(0,255-i,255-i));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(i,0,i));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(255-i,255-i,0));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(i,i,i));        }
        currentColors = rgbColors;
    }*/
	
	public static final double DEFAULT_ORIGIN_X = 0.0; 
	public static final double DEFAULT_ORIGIN_Y = 0.0;
//	public static final double DEFAULT_ORIGIN_X = 0.001643721971153; 
//	public static final double DEFAULT_ORIGIN_Y = 0.822467633296005;
//	public static final double DEFAULT_ORIGIN_X = -0.00573721971153; 
//	public static final double DEFAULT_ORIGIN_Y = 0.805657633296005;
    //public static final double DEFAULT_RADIUS = 0.002000000002001;
    public static final double DEFAULT_RADIUS = 10.0;
    public static final int FULL_RADIUS = 5;

	// maximum = 16384 = 256 mpixel
	private int colorPowerMult = 2;
    private int extentX = 16384;//512 << 0;//3;
    private int extentY = 16384;//512 << 0;//3;	
    private double realPerPixelX;
    private double realPerPixelY;
    private double gridXStart = -2;
    private double gridYStart = -1.5;
    private double gridXEnd = 1;
    private double gridYEnd = 1.5;
    private int maximumOrbitPath = 65536 << 0;//1791;// 7* 255;
    private double maximumOrbitRadius = 2;
    private double originalOriginX = 0;
    private double originalOriginY = 0;
    private long startTimestamp;
	//private static int TRUNC = 100000000;

    public void process() {//int width, int height) {	
    	genericInit();
    	int height = this.getExtentX();
    	int width = this.getExtentY();
    	setRadius(5);
    	for(int r=0;r<4;r++) {
        	int frame = 0;
    		//int frames = (int)Math.pow((2*radius), r);
        	int trunc = (int)Math.pow(10, (r+1));
    		int nextRadius = (int)(radius*trunc);
    		setRadius(((double)nextRadius)/(trunc * 10));
    		//setRadius(radius * 0.05);
    		int h=0;
    		int v=0;
			System.out.println();
    		for(double x=-5;x<5;x+=(radius*2)) {
    			v=0;
    			System.out.print(".");
    			for(double y=-5;y<5;y+=(radius*2)) {
//    				setOrigin(DEFAULT_ORIGIN_X + (2 * radius * x) + (radius), 
//    						DEFAULT_ORIGIN_Y + (2 * radius * y) + (radius));
    				setOrigin(x + (radius), 
    						y + (radius));
    				setRadius(radius);
    				BufferedImage dest = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
    				MandelbrotForkJoinUnitOfWork fork = new MandelbrotForkJoinUnitOfWork(
    						this, dest, height, 0, height, height, width);
    				
    				//System.out.print(".");//Start: " + System.currentTimeMillis());
    				long start = System.currentTimeMillis();
    				Boolean allMax = mapReducePool.invoke(fork);
    				long duration = (System.currentTimeMillis() - start);
    				long end = System.currentTimeMillis();
    				// only save significant frames
    				if(duration > 270 && !allMax) { // 260 for virtual
    					
        				System.out.println("\ncompute in: " + duration + " ms :");
           				System.out.println("center: " + originalOriginX + "," + originalOriginY);
        				System.out.println("radius: " + this.radius);
        				System.out.println("frame : " + frame);
    					saveImage(dest, duration, frame, trunc, (r), h,v);
    				
    				System.out.println("image in:   " + (System.currentTimeMillis() - end) + " ms :");
    				System.out.println("End  : " + System.currentTimeMillis());
    				}
    				frame++;
    				v++;    				
    			}
    			h++;
    		}
    	}
    }    
    public void process2() {//int width, int height) {	
    	genericInit();
    	int height = this.getExtentX();
    	int width = this.getExtentY();
    	setRadius(FULL_RADIUS);
    	
    	for(int zoom=0;zoom<4;zoom++) {
        	int frame = 0;
    		//int frames = (int)Math.pow((2*radius), r);
        	int trunc = (int)Math.pow(FULL_RADIUS * 2, (zoom + 1));
    		int nextRadius = (int)(radius*trunc);
    		setRadius(((double)nextRadius)/(trunc * FULL_RADIUS * 2));
    		//setRadius(radius * 0.05);
    		int h=0;
    		int v=0;
    		double x=0;
    		double y=0;
			System.out.println();
			for(int horiz=0;horiz<trunc;horiz++) {
				x = -FULL_RADIUS + ((double)(horiz - FULL_RADIUS) * (radius*2)); 
    			v=0;
    			System.out.print(".");
    			for(int vert=0;vert<trunc;vert++) {
    				y = -FULL_RADIUS + ((double)(vert - FULL_RADIUS) * (radius*2)); 
//    				setOrigin(DEFAULT_ORIGIN_X + (2 * radius * x) + (radius), 
//    						DEFAULT_ORIGIN_Y + (2 * radius * y) + (radius));
    				setOrigin(x + (radius), y + (radius));
    				setRadius(radius);
    				BufferedImage dest = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
    				MandelbrotForkJoinUnitOfWork fork = new MandelbrotForkJoinUnitOfWork(
    						this, dest, height, 0, height, height, width);
    				
    				//System.out.print(".");//Start: " + System.currentTimeMillis());
    				long start = System.currentTimeMillis();
    				Boolean allMax = mapReducePool.invoke(fork);
    				long duration = (System.currentTimeMillis() - start);
    				long end = System.currentTimeMillis();
    				// only save significant frames
    				if(duration > 400 && !allMax) {    					
        				System.out.println("\ncompute in: " + duration + " ms:");
           				System.out.println("center: " + originalOriginX + "," + originalOriginY);
        				System.out.println("radius: " + this.radius);
        				System.out.println("frame : " + frame);
    					saveImage(dest, duration, frame, trunc, (zoom), h,v);
    				
    				System.out.println("image in:   " + (System.currentTimeMillis() - end) + " ms :");
    				System.out.println("End  : " + System.currentTimeMillis());
    				} else {
    					if(allMax) {
    						System.out.print("M");
    					}
    				}
    				frame++;
    				v++;    				
    			}
    			h++;
    		}
    	}
    }
	
    public void saveImage(BufferedImage image, long duration, int frame, int trunc, int r, int h, int v) {
    	File f = new File("mandelbrot_p" +
    			r + "_x" + h + "_y" + v + "_o" +
    			
//    	        ((double)((double)((int)(originalOriginX * trunc))/trunc)) + "_y" + 
//    	        ((double)((double)((int)(originalOriginY * trunc))/trunc)) + "_r" +
//    	        ((double)((double)((int)(radius * trunc))/trunc)) + "_o" +
   //originalOriginX + "_y" + 
   //originalOriginY + "_r" +
   //radius + "_o" +
    	        maximumOrbitPath + "_h" +
    	        extentX + "_v" +
    	        extentY + "_c" +
    	        colorPowerMult + //"_t" +
    	        //duration +
    	        "_f" + frame +
    	        ".png");
      	try {
    		ImageIO.write(image, "png", f);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }        
    
    public void setOrigin(double x, double y) {
        originalOriginX = x;
        originalOriginY = y;
    }
        

    public void setRadius(double aRadius) {
    	radius = aRadius;
        // compute edges of window
        this.setGridXStart(originalOriginX - radius);
        this.setGridXEnd(originalOriginX + radius);
        this.setGridYStart(originalOriginY - radius);
        this.setGridYEnd(originalOriginY + radius);  
		// setup screen real estate
		setRealPerPixelX((gridXEnd - gridXStart) / getExtentX());
		setRealPerPixelY((gridYEnd - gridYStart) / getExtentY());
    }

	public void genericInit() {
		// setup extents
		setOrigin(DEFAULT_ORIGIN_X, DEFAULT_ORIGIN_Y);
		setRadius(DEFAULT_RADIUS);
		// start the timer
		startTimestamp = System.currentTimeMillis();
	}
 
	public ForkJoinPool getMapReducePool() {
		return mapReducePool;
	}

	public void setMapReducePool(ForkJoinPool mapReducePool) {
		this.mapReducePool = mapReducePool;
	}

	public static List<Color> getRgbColors() {
		return rgbColors;
	}

	public static void setRgbColors(List<Color> rgbColors) {
		MandelbrotImager.rgbColors = rgbColors;
	}

	public static List<Color> getCurrentColors() {
		return currentColors;
	}

	public static void setCurrentColors(List<Color> currentColors) {
		MandelbrotImager.currentColors = currentColors;
	}

	public double getRealPerPixelX() {
		return realPerPixelX;
	}

	public void setRealPerPixelX(double realPerPixelX) {
		this.realPerPixelX = realPerPixelX;
	}

	public double getRealPerPixelY() {
		return realPerPixelY;
	}

	public void setRealPerPixelY(double realPerPixelY) {
		this.realPerPixelY = realPerPixelY;
	}

	public double getGridXStart() {
		return gridXStart;
	}

	public void setGridXStart(double gridXStart) {
		this.gridXStart = gridXStart;
	}

	public double getGridYStart() {
		return gridYStart;
	}

	public void setGridYStart(double gridYStart) {
		this.gridYStart = gridYStart;
	}

	public double getGridXEnd() {
		return gridXEnd;
	}

	public void setGridXEnd(double gridXEnd) {
		this.gridXEnd = gridXEnd;
	}

	public double getGridYEnd() {
		return gridYEnd;
	}

	public void setGridYEnd(double gridYEnd) {
		this.gridYEnd = gridYEnd;
	}

	public int getMaximumOrbitPath() {
		return maximumOrbitPath;
	}

	public void setMaximumOrbitPath(int maximumOrbitPath) {
		this.maximumOrbitPath = maximumOrbitPath;
	}

	public double getMaximumOrbitRadius() {
		return maximumOrbitRadius;
	}

	public void setMaximumOrbitRadius(double maximumOrbitRadius) {
		this.maximumOrbitRadius = maximumOrbitRadius;
	}

	public double getOriginalOriginX() {
		return originalOriginX;
	}

	public void setOriginalOriginX(double originalOriginX) {
		this.originalOriginX = originalOriginX;
	}

	public double getOriginalOriginY() {
		return originalOriginY;
	}

	public void setOriginalOriginY(double originalOriginY) {
		this.originalOriginY = originalOriginY;
	}

	public int getColorPowerMult() {
		return colorPowerMult;
	}

	public void setColorPowerMult(int colorPowerMult) {
		this.colorPowerMult = colorPowerMult;
	}

	public int getExtentX() {
		return extentX;
	}

	public void setExtentX(int extentX) {
		this.extentX = extentX;
	}

	public int getExtentY() {
		return extentY;
	}

	public void setExtentY(int extentY) {
		this.extentY = extentY;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public static void main(String[] args) {
		MandelbrotImager imager = new MandelbrotImager();
		imager.process();
	}
}
