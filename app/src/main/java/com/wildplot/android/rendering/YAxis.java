/**
 * 
 */
package com.wildplot.android.rendering;

import java.text.DecimalFormat;

import com.wildplot.android.rendering.graphics.wrapper.FontMetrics;
import com.wildplot.android.rendering.graphics.wrapper.Graphics;
import com.wildplot.android.rendering.graphics.wrapper.Rectangle;
import com.wildplot.android.rendering.interfaces.Drawable;

/**
 * This Class represents a Drawable x-axis 
 * 
 * 
 */
public class YAxis implements Drawable {

    private float maxTextWidth = 0;

    private boolean isIntegerNumbering = false;

    private boolean isOnRightSide = false;
	
	private boolean isLog = false;
	
	public boolean hasVariableLimits = true;
	
	private boolean isAutoTic = false;
	
	/**
	 * the offset of the x-axis to prevent drawing numbering on x-axis
	 */
	private double yOffset = 0;
	
	/**
	 * offset to move axis left or right
	 */
	private double xOffset = 0;
	
	private String name = "Y";
	
	/**
	 * Format that is used to print numbers under markers
	 */
	private DecimalFormat df =   new DecimalFormat( "##0.0#" );	
	private DecimalFormat dfScience =   new DecimalFormat( "0.0###E0" );
    private DecimalFormat dfInteger =   new DecimalFormat( "#.#" );
	
	private boolean isScientific = false;
	/**
	 * the PlotSheet object the x-axis is drawn onto
	 */
	private PlotSheet plotSheet;
	
	/**
	 * the start of x-axis marker, used for relative alignment of further marks
	 */
	private double ticStart = 0;
	
	/**
	 * the space between two marks
	 */
	private double tic = 1;
	
	/**
	 * the space between two minor marks
	 */
	private double minorTic= 0.5;
	
	/**
	 * the estimated size between two mayor tics in auto tic mode
	 */
	private float pixelDistance = 25;
	
	/**
	 * the estimated size between two minor tics in auto tic mode
	 */
	private float minorPixelDistance = 25;
	
	/**
	 * start of drawn x-axis
	 */
	private double start = 0;
	
	/**
	 * end of drawn x-axis
	 */
	private double end = 100;
	
	/**
	 * true if the marker should be drawn into the direction above the axis
	 */
	private boolean markOnLeft = true;
	
	/**
	 * true if the marker should be drawn into the direction under the axis
	 */
	private boolean markOnRight = true;
	
	/**
	 * length of a marker in pixel, length is only for one side
	 */
	private float markerLength = 5;
	
	private boolean isOnFrame = false;
	
	/**
	 * Constructor for an Y-axis object
	 * @param plotSheet the sheet the axis will be drawn onto
	 * @param ticStart the start of the axis markers used for relative alignment of other markers
	 * @param tic the space between two markers
	 */
	public YAxis(PlotSheet plotSheet, double ticStart, double tic, double minorTic ) {
		this.plotSheet = plotSheet;
		this.ticStart = ticStart;
		this.tic = tic;
		this.minorTic=minorTic;
		this.pixelDistance = (int)Math.round((plotSheet.getyRange()[1] - plotSheet.getyRange()[0])/tic);
	}
	
	/**
	 * Constructor for an Y-axis object this instance uses autocalculation of tics with a given pixeldistance
	 * @param plotSheet the sheet the axis will be drawn onto
	 * @param ticStart the start of the axis markers used for relative alignment of other markers
	 */
	public YAxis(PlotSheet plotSheet, double ticStart, float pixelDistance , float minorPixelDistance) {
		this.plotSheet = plotSheet;
		this.ticStart = ticStart;
		this.pixelDistance = pixelDistance;
		this.minorPixelDistance = minorPixelDistance;
		this.isAutoTic = true;
	}

	/*
	 * (non-Javadoc)
	 * @see rendering.Drawable#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		Rectangle field = g.getClipBounds();
		
		if(this.hasVariableLimits){
			start = plotSheet.getyRange()[0];
			end = plotSheet.getyRange()[1];
		}
		
		if(this.isOnFrame)
			//xOffset = plotSheet.getxRange()[0];
		if(this.isAutoTic) {
			this.tic = plotSheet.ticsCalcY(pixelDistance, field);
			double quotient = (pixelDistance*1.0)/(minorPixelDistance*1.0);
			this.minorTic = (this.tic / Math.round(quotient));
		}
		else {
			this.pixelDistance = Math.abs(plotSheet.yToGraphic(0, field) - plotSheet.yToGraphic(tic, field));
			this.minorPixelDistance = Math.abs(plotSheet.yToGraphic(0, field) - plotSheet.yToGraphic(minorTic, field));
		}
		if(this.tic < 1e-2 || this.tic > 1e2)
			this.isScientific = true;
		
		//vertikale Linie
        float[] coordStart = plotSheet.toGraphicPoint(xOffset, start, field);
        float[] coordEnd = plotSheet.toGraphicPoint(xOffset, end, field);
		
		if(!this.isOnFrame)
			g.drawLine(coordStart[0], coordStart[1], coordEnd[0], coordEnd[1]);
		
		drawMarkers(g);
		drawMinorMarkers(g);
	}
	
	/**
	 * draw markers on the axis
	 * @param g graphic object used for drawing
	 */
	private void drawMarkers(Graphics g) {
		Rectangle field = g.getClipBounds();

        float cleanSpace = 17; // space in pixel that will be unmarked on the end of the axis for arrow and description

        float tics = (int)((this.ticStart - this.start)/tic);
		double leftStart = this.ticStart - this.tic*tics; 
		
		double logStart = 0;
		if(this.isLog) {
			logStart = Math.ceil(Math.log10(this.start));
			leftStart = Math.pow(10, logStart++); 
		}
		double currentY = leftStart;
		
		while(currentY <= this.end) {
			if((!this.isOnFrame &&plotSheet.yToGraphic(currentY, field) >= plotSheet.yToGraphic(this.end, field) +cleanSpace 
					&& plotSheet.yToGraphic(currentY, field) <= plotSheet.yToGraphic(this.start, field) - cleanSpace
					&& plotSheet.yToGraphic(currentY, field) <= field.y + field.height - cleanSpace
					&& plotSheet.yToGraphic(currentY, field) >= field.y  + cleanSpace) || 
					(this.isOnFrame && currentY <= this.plotSheet.getyRange()[1] &&
							currentY >= this.plotSheet.getyRange()[0])){
			
				if(this.markOnRight ) {
					drawRightMarker(g, field, currentY);
				}
				if(this.markOnLeft ) {
					drawLeftMarker(g, field, currentY);
				}
				if(!(Math.abs(currentY) < yOffset+0.001 && !this.isOnFrame)) {
					if(isOnRightSide)
                        drawNumberingOnRightSide(g,field,currentY);
                    else
					    drawNumbering(g, field, currentY);
				} 
			} 
			if(this.isLog) {
				currentY =  Math.pow(10, logStart++);
			}else {
				currentY += this.tic;
			}
		}
		FontMetrics fm = g.getFontMetrics( g.getFont() );
        float width = fm.stringWidth(this.name);
		//arrow

        float[] arowheadPos = {plotSheet.xToGraphic(xOffset, field), (plotSheet.getyRange()[1] >= this.end)? plotSheet.yToGraphic( this.end, field): plotSheet.yToGraphic(plotSheet.getyRange()[1], field) };
		if(!this.isOnFrame) {
			g.drawLine(arowheadPos[0]-1, arowheadPos[1]+1, arowheadPos[0]-3, arowheadPos[1]+6);
			g.drawLine(arowheadPos[0]+1, arowheadPos[1]+1, arowheadPos[0]+3, arowheadPos[1]+6);
			g.drawString(this.name, arowheadPos[0]-13-width, arowheadPos[1] + 10);
		} else {
//			AffineTransform fontAT = new AffineTransform();
//			Font oldFont = g.getFont();
//			fontAT.rotate(-Math.PI/2.0);
//			Font newFont = oldFont.deriveFont(fontAT);
//			g.setFont(newFont);

            g.save();
            if(isOnRightSide) {
                float[] middlePosition = {plotSheet.xToGraphic(xOffset, field), plotSheet.yToGraphic(0, field)};
                g.rotate(90, Math.round(middlePosition[0]+maxTextWidth*1.4f), field.height / 2 - width / 2);
                g.drawString(this.name, Math.round(middlePosition[0]+maxTextWidth*1.4f), field.height / 2 - width / 2);
            }else {
                float[] middlePosition = {plotSheet.xToGraphic(xOffset, field), plotSheet.yToGraphic(0, field)};
                g.rotate(-90, Math.round(middlePosition[0]-maxTextWidth*1.4f), field.height / 2 + width / 2);
                g.drawString(this.name, Math.round(middlePosition[0]-maxTextWidth*1.4f), field.height / 2 + width / 2);
            }
            g.restore();
//			g.setFont(oldFont);
		}
		//axis name
	}
	
	/**
	 * draw number left to a marker
	 * @param g graphic object used for drawing
	 * @param field bounds of plot
	 * @param y position of number
	 */
	private void drawNumbering(Graphics g, Rectangle field, double y) {
		
		if(this.tic < 1 && Math.abs(ticStart-y) < this.tic*this.tic)
			y = ticStart;

        float[] coordStart = plotSheet.toGraphicPoint(xOffset, y, field);
		
		
		FontMetrics fm = g.getFontMetrics( g.getFont() );
        float fontHeight = fm.getHeight(true);
		String font = df.format(y);
        float width = fm.stringWidth(font);
		if(this.isScientific && !isIntegerNumbering){
			
			font = dfScience.format(y);
			width 		= fm.stringWidth(font);
			g.drawString(font, Math.round(coordStart[0]-width*1.1f), Math.round(coordStart[1] + fontHeight*0.4f) );
		}else if(isIntegerNumbering){
            font = dfInteger.format(y);
            width 		= fm.stringWidth(font);
            g.drawString(font, Math.round(coordStart[0]-width*1.1f), Math.round(coordStart[1] + fontHeight*0.4f) );
        }else {
            g.drawString(font, Math.round(coordStart[0]-width*1.1f), Math.round(coordStart[1] + fontHeight*0.4f) );
		}
        if(width > maxTextWidth)
            maxTextWidth = width;
		//g.drawString(df.format(y), coordStart[0]-33, coordStart[1]+4);
	}

    /**
     * draw number left to a marker
     * @param g graphic object used for drawing
     * @param field bounds of plot
     * @param y position of number
     */
    private void drawNumberingOnRightSide(Graphics g, Rectangle field, double y) {
        if(this.tic < 1 && Math.abs(ticStart-y) < this.tic*this.tic)
            y = ticStart;

        float[] coordStart = plotSheet.toGraphicPoint(xOffset, y, field);
        FontMetrics fm = g.getFontMetrics( g.getFont() );
        float fontHeight = fm.getHeight(true);
        String font = df.format(y);
        float width = fm.stringWidth(font);
        if(this.isScientific && !isIntegerNumbering){

            font = dfScience.format(y);
            width = fm.stringWidth(font);
            g.drawString(font, Math.round(coordStart[0]+width*0.1f), Math.round(coordStart[1] + fontHeight*0.4f) );
        }else if(isIntegerNumbering){
            font = dfInteger.format(y);
            width = fm.stringWidth(font);
            g.drawString(font, Math.round(coordStart[0]+width*0.1f), Math.round(coordStart[1] + fontHeight*0.4f) );
        }else {
            g.drawString(font, Math.round(coordStart[0]+width*0.1f), Math.round(coordStart[1] + fontHeight*0.4f) );
        }
        if(width > maxTextWidth)
            maxTextWidth = width;
        //g.drawString(df.format(y), coordStart[0]-33, coordStart[1]+4);
    }
	
	/**
	 * draws an left marker
	 * @param g graphic object used for drawing
	 * @param field bounds of plot
	 * @param y position of marker
	 */
	private void drawLeftMarker(Graphics g, Rectangle field, double y){

        float[] coordStart = plotSheet.toGraphicPoint(xOffset, y, field);
        float[] coordEnd = {coordStart[0] - this.markerLength, coordStart[1]};
		g.drawLine(coordStart[0], coordStart[1], coordEnd[0], coordEnd[1]);
		
	}
	/**
	 * draws an right marker
	 * @param g graphic object used for drawing
	 * @param field bounds of plot
	 * @param y position of marker
	 */
	private void drawRightMarker(Graphics g, Rectangle field, double y){
        float[] coordStart = plotSheet.toGraphicPoint(xOffset, y, field);
        float[] coordEnd = {coordStart[0] + this.markerLength, coordStart[1]};
		g.drawLine(coordStart[0], coordStart[1], coordEnd[0], coordEnd[1]);
		
	}
	

	/**
	 * get the offset of this axis
	 * @return the offset of this axis
	 */
	public double getxOffset() {
		return xOffset;
	}

	/**
	 * set the offset of this axis
	 * @param yOffset new offset
	 */
	public void setxOffset(double yOffset) {
		this.xOffset = yOffset;
	}
	
	/**
	 * set offset back to zero for normal axis behavior
	 */
	public void unsetxOffset() {
		this.xOffset = 0;
	}
	
	/**
	 * set the y coordinate where the x axis goes through this axis
	 * @param yOffset offset of the x-axis
	 */
	public void setIntersectionOffset(double yOffset) {
		this.yOffset = yOffset;
	}
	
	/**
	 * set the axis to draw on the border between outer frame and plot
	 */
	public void setOnFrame() {
		this.isOnFrame = true;
		xOffset = plotSheet.getxRange()[0];
		markOnLeft = false;
	}
	
	/**
	 * unset the axis to draw on the border between outer frame and plot
	 */
	public void unsetOnFrame() {
		this.isOnFrame = false;
		xOffset = 0;
		markOnLeft = true;
        markOnRight = true;
        isOnRightSide = false;
	}

    public void setOnRightSideFrame() {
        this.isOnFrame = true;
        xOffset = plotSheet.getxRange()[1];
        markOnRight = false;
        isOnRightSide = true;
    }
	
	/*
	 * (non-Javadoc)
	 * @see rendering.Drawable#isOnFrame()
	 */
	public boolean isOnFrame() {
		return isOnFrame;
	}
	
	/**
	 * set name of axis
	 * @param name name of axis
	 */
	public void setName(String name) {
		this.name = name;
	}
	private void drawMinorMarkers(Graphics g) {
		Rectangle field = g.getClipBounds();
		
		int cleanSpace = 17; // space in pixel that will be unmarked on the end of the axis for arrow and description
		
		int tics = (int)((this.ticStart - this.start)/tic);
		double leftStart = this.ticStart - this.tic*tics;

        int factor = 1;
		double logStart = 0;
		if(this.isLog) {
			logStart = Math.floor(Math.log10(this.start));
			leftStart = Math.pow(10, logStart) * factor++; 
		}
		
		double currentY = leftStart;
		
		while(currentY <= this.end) {
			
			if((!this.isOnFrame &&plotSheet.yToGraphic(currentY, field) >= plotSheet.yToGraphic(this.end, field) +cleanSpace 
					&& plotSheet.yToGraphic(currentY, field) <= plotSheet.yToGraphic(this.start, field) - cleanSpace
					&& plotSheet.yToGraphic(currentY, field) <= field.y + field.height - cleanSpace
					&& plotSheet.yToGraphic(currentY, field) >= field.y  + cleanSpace) || 
					(this.isOnFrame && currentY <= this.plotSheet.getyRange()[1] &&
							currentY >= this.plotSheet.getyRange()[0])){
			
				if(this.markOnRight ) {
					drawRightMinorMarker(g, field, currentY);
				}
				if(this.markOnLeft ) {
					drawLeftMinorMarker(g, field, currentY);
				}
			} 
			if(this.isLog) {
				if(factor == 10)
				{
					factor = 1;
					logStart++;
				}
				currentY =  Math.pow(10, logStart)*factor++;
			}else {
				currentY += minorTic;
			}
		}
		
	}

//	/**
// 	* caculates minor tics
// 	* @param g graphic object used for drawing
// 	* @return returns the new minor tics in percent
// 	*/
//
//	private double calcMinorTics(Graphics g)
//	{
//		double percentMinorTic=this.minorTic/this.tic;
//		return percentMinorTic;
//	}
	
	/**
	 * draws an left minor marker
	 * @param g graphic object used for drawing
	 * @param field bounds of plot
	 * @param y position of marker
	 */
	private void drawLeftMinorMarker(Graphics g, Rectangle field, double y){

        float[] coordStart = plotSheet.toGraphicPoint(xOffset, y, field);
        float[] coordEnd = {(int) (coordStart[0] - 0.5*this.markerLength), coordStart[1]};
		g.drawLine(coordStart[0], coordStart[1], coordEnd[0], coordEnd[1]);
		
	}
	/**
	 * draws an right minor marker
	 * @param g graphic object used for drawing
	 * @param field bounds of plot
	 * @param y position of marker
	 */
	private void drawRightMinorMarker(Graphics g, Rectangle field, double y){
        float[] coordStart = plotSheet.toGraphicPoint(xOffset, y, field);
        float[] coordEnd = {(int) (coordStart[0] + 0.5*this.markerLength), coordStart[1]};
		g.drawLine(coordStart[0], coordStart[1], coordEnd[0], coordEnd[1]);
		
	}

    public void setIntegerNumbering(boolean isIntegerNumbering){
        this.isIntegerNumbering = isIntegerNumbering;
    }
	public void setLog(){
		this.isLog = true;
	}

	public void unsetLog(){
		this.isLog = false;
	}

	@Override
	public void abortAndReset() {
		// TODO Auto-generated method stub
		
	}
    @Override
    public boolean isClusterable() {
        return true;
    }

    @Override
    public boolean isCritical() {
        return true;
    }
}
