
package org.obrienscience.fractal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

// TODO: zoom sequence entity
public class Mandelbrot extends AnimApplet2 {
    // There is only the need for one of the following statics
    private static final long serialVersionUID = 6502859074457924892L;
    /** Get number of (hyperthreaded + real) cores.  IE: p630 with HT=2, Core2 E8400=2 and Core i7-920 = 8 */
    public static final int CORES = 64;//Runtime.getRuntime().availableProcessors() << 8;
    public static List<Color> rgbColors;
    public static List<Color> currentColors;
    private long frame = 1;
    
	static {
        rgbColors = new ArrayList<Color>();
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(0,0,i));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(0,i,0));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(i,0,0));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(0,i,i));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(i,0,i));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(i,i,0));        }
        for(int i=0;i<256;i++) {            rgbColors.add(new Color(i,i,i));        }
        currentColors = rgbColors;
    }
	
    //private int frameNumber = 0;
    private double realPerPixelX;
    private double realPerPixelY;
    private double gridXStart = -2;
    private double gridYStart = -1.5;
    private double gridXEnd = 1;
    private double gridYEnd = 1.5;
    //private double gridZoomOffset = 0.01;
    private int maximumOrbitPath = 65536;//1791;// 7* 255;
    private double maximumOrbitRadius = 2;
    private double originalOriginX = 0;
    private double originalOriginY = 0;
    //private double radiusZoomOffset = 0.0005;
    private double radiusZoomFactor = 0.9;
    private int direction = -1;
    private double radius = 10;
    
    private List<Boolean> threadCompleteList = new ArrayList<Boolean>();
    private List<MandelbrotUnitOfWork> workUnits = new ArrayList<MandelbrotUnitOfWork>();
    public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
	private BufferedImage bufferedImage;
    
    
    private long startTimestamp;
    
    //0.001643721971153 + 0.822467633298876i
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
    }
    
    public void setParameters2(double x, double y, double radius) {
        // compute edges of window
        this.setGridXStart(x - radius);
        this.setGridXEnd(x + radius);
        this.setGridYStart(y - radius);
        this.setGridYEnd(y + radius);        
    }    
    
    
    /**
     * This function is called by the Java2d framework
     */
    @Override
    public void paint(Graphics g) {
    	BufferedImage dest = new BufferedImage(izMaxWidth,izMaxHeight, BufferedImage.TYPE_INT_RGB);
    	setBufferedImage(dest);
        g.drawImage(buffer, 0, 0, this);
        // clear previous image from buffer
        if (null != gContext) { // image may not be ready
            //gContext.setColor(Color.black);
            //gContext.fillRect(0, 0, izMaxWidth, izMaxHeight);
            //displayCurrentSolution();            

            // Repaint only when all threads are complete
            boolean threadsFinished = false;
            long threadCount = 0;
            while(!threadsFinished) {
                for(Boolean complete : threadCompleteList) {
                    if(complete) {
                        threadCount++;
                    }                
                }
                if(threadCount == threadCompleteList.size()) {  
                    // all threads finished - lets create new threads and exit/recalculate
                    threadsFinished = true;
                    for(int core=0;core<CORES;core++) {
                        Thread thread = new Thread(workUnits.get(core));        
                        threadCompleteList.set(core,false); // 20110323 : key to synchronization
                        thread.start(); // we only start once
                        // Let the Java2d display catch up and render the frame completely
                        //try { Thread.sleep(1); } catch (InterruptedException e) { // this will avoid running the host thread at 50% cpu
                          //  showStatus(e.toString());
                        //}
                    }
                } else { threadCount = 0; }
            
                // we will need some sleep time to allow the OS to respond to system events (as in don't use 100% CPU)
                try { Thread.sleep(1); } catch (InterruptedException e) { // this will avoid running the host thread at 50% cpu
                    showStatus(e.toString());
                }
            }
            iterateRadius();
            // write out numerical parameters
            //gContext.setColor(Color.cyan);
            //gContext.drawString("R: " + (new Double(radius)).toString(), 30, izMaxHeight - 20);
            // save image
            saveImage(this.getGraphics());
            frame++;
            
        }
        repaint(); // display buffered image
    }

    public void saveImage(Graphics g) {
    	File f = new File("output" + frame + ".png");
       	
    	//BufferedImage dest = new BufferedImage(izMaxWidth,izMaxHeight, BufferedImage.TYPE_INT_RGB);
    	//BufferedImage src = g.
    	//Graphics2D destG = getBufferedImage().createGraphics(); 
    	//destG.drawImage(src, 0,0,null);
      
    	for(int x=0;x<this.izMaxHeight;x++) {
    		for(int y=0;y<this.izMaxWidth;y++) {
    			//int c = g.
    			//buffGraph.setRGB(x, y, rgb)..setData(Raster)
    			//getBufferedImage().setRGB((int)x,(int)y,iterations);
    		}
    	}
    	try {
    		ImageIO.write(getBufferedImage(), "png", f);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void iterateRadius() {
        if(radius < 0.000000000000001 && direction < 0) {
            radiusZoomFactor = 1.1;
            direction = 1;
            // TEMP
            System.out.println("Duration in ms: " + (System.currentTimeMillis() - startTimestamp));
            System.exit(0);
        }
        if(radius > 10 && direction > 0) {
            radiusZoomFactor = 0.9;
            direction = -1;
        }
        radius = radius * radiusZoomFactor;// - radiusZoomOffset;
        setRadius(radius);
    }
    
    private synchronized void addWorkUnits() {
        // Setup thread handlers by dividing up the work        
        for(int core=0;core<CORES;core++) { // this works best if the # of cores is a power of 2 (not a triple core)
        	MandelbrotUnitOfWork uow = new MandelbrotUnitOfWork(
                    this,  // Host thread
                    core,// thread ID
                    core * (getExtentY() / CORES), // start row
                    (core + 1) * (getExtentY() / CORES) - 1); // end row
            threadCompleteList.add(true); // auto-boxed
            workUnits.add(uow);
        }
    }
    
    @Override
    public void genericInit() {
        // setup extents
        //setOrigin(0.001643721971153, 0.822467633298876);        
        setOrigin(0.001643721971153, 0.822467633295956);        
        //this.setParameters(0.001643721971153, 0.822467633298876, 0.01);
        //setRadius(0.00000000005);//radius);
        setRadius(0.00000050005);//radius);
        // setup screen real estate
        recalculateParameters();
        addWorkUnits();
        // start the timer
        startTimestamp = System.currentTimeMillis();
    }

    public void recalculateParameters() {
        setRealPerPixelX((gridXEnd - gridXStart) / getExtentX());
        setRealPerPixelY((gridYEnd - gridYStart) / getExtentY());        
    }
    public static void main(String[] args) {
        Mandelbrot aMandelbrot = new Mandelbrot();
        aMandelbrot.applicationInit();
        /*
         * double xStart = -2; double yStart = -2; double xEnd = 2; double yEnd = 2; long
         * pathMax = 100; double sizeMax = 2; long iterations; for(double y = yStart; y<yEnd; y
         * += 0.2) { for(double x = xStart; x<xEnd; x += 0.2) { iterations =
         * aMandelbrot.computeJulia(x, y, sizeMax, pathMax); System.out.print(iterations +
         * ", "); } System.out.println(); }
         */
    }
    
    public List<Boolean> getThreadCompleteList() {        return threadCompleteList;    }
    public void setThreadCompleteList(List<Boolean> threadCompleteList) {        this.threadCompleteList = threadCompleteList;    }
    public Boolean isThreadComplete(int index) {        return threadCompleteList.get(index);    }    
    public void setThreadComplete(int index, Boolean flag) {        threadCompleteList.set(index, flag);    }
    public List<MandelbrotUnitOfWork> getWorkUnits() {        return workUnits;    }
    public void setWorkUnits(List<MandelbrotUnitOfWork> workUnits) {        this.workUnits = workUnits;    }    
    public double getRealPerPixelX() {        return realPerPixelX;    }
    public void setRealPerPixelX(double realPerPixelX) {        this.realPerPixelX = realPerPixelX;    }
    public double getRealPerPixelY() {        return realPerPixelY;    }
    public void setRealPerPixelY(double realPerPixelY) {        this.realPerPixelY = realPerPixelY;    }
    public double getGridXStart() {        return gridXStart;    }
    public double getGridYStart() {        return gridYStart;    }
    public double getGridXEnd() {        return gridXEnd;    }
    public double getGridYEnd() {        return gridYEnd;    }
    public int getMaximumOrbitPath() {        return maximumOrbitPath;    }
    public void setMaximumOrbitPath(int maximumOrbitPath) {        this.maximumOrbitPath = maximumOrbitPath;    }
    public double getMaximumOrbitRadius() {        return maximumOrbitRadius;    }
    public void setMaximumOrbitRadius(double maximumOrbitRadius) {        this.maximumOrbitRadius = maximumOrbitRadius;    }
    public static List<Color> getCurrentColors() {      return currentColors;   }
    public static void setCurrentColors(List<Color> currentColors) {        Mandelbrot.currentColors = currentColors;   }
    public synchronized void setGridXStart(double gridXStart) {
        this.gridXStart = gridXStart;    
        recalculateParameters();
        }
    public synchronized void setGridYStart(double gridYStart) {        
        this.gridYStart = gridYStart;
        recalculateParameters();        
        }
    public synchronized void setGridXEnd(double gridXEnd) {        
        this.gridXEnd = gridXEnd;
        recalculateParameters();        
        }
    public synchronized void setGridYEnd(double gridYEnd) {
        this.gridYEnd = gridYEnd;
        recalculateParameters();
        }

    /*
    public void displayCurrentSolution() {
        double h,v;
        int iterations;
        for(int x=0;x<getExtentX();x++) {
            h = getGridXStart() + realPerPixelX * x;
            for(int y=0;y<getExtentY();y++) {
                v = getGridYStart() + realPerPixelY * y;
                iterations = computeJulia(h,v, maximumOrbitRadius, maximumOrbitPath);
                //System.out.println(iterations);
                gContext.setColor(currentColors.get(iterations));
                gContext.drawRect(x,y,1,1);
            }
        }
    }
    
    public void iterateParameters() {
        synchronized (this) {
            if(this.getGridXStart() < 0) {
                this.setGridXStart(this.getGridXStart() + this.gridZoomOffset);
            } else {
                this.setGridXStart(this.getGridXStart() - this.gridZoomOffset);
            }
            if(this.getGridYStart() < 0) {
                this.setGridYStart(this.getGridYStart() + this.gridZoomOffset);
            } else {
                this.setGridYStart(this.getGridYStart() - this.gridZoomOffset);
            }
            if(this.getGridXEnd() < 0) { 
                this.setGridXEnd(this.getGridXEnd() + this.gridZoomOffset);
            } else {
                this.setGridXEnd(this.getGridXEnd() - this.gridZoomOffset);
            }
            if(this.getGridYEnd() < 0) { 
                this.setGridYEnd(this.getGridYEnd() + this.gridZoomOffset);
            } else {
                this.setGridYEnd(this.getGridYEnd() - this.gridZoomOffset);
            }
        }
        switch (frameNumber) {
        case 0:
            //currentColors = redColors;
            frameNumber++;
            break;
        case 1:
            //currentColors = greenColors;
            frameNumber++;
            break;
        case 2:
            //currentColors = blueColors;
            frameNumber = 2;
            break;
        }
            
    }
*/    
}
