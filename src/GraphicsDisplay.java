package bsu.rfe.java.group6.lab4.Ivleva.var1;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
	// Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scaleX;
    private double scaleY;
    private boolean isDragging = false;
    private Point dragStart = null;
    private Rectangle dragRect = null;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;

    public GraphicsDisplay() {
    	// Цвет заднего фона области отображения - белый
    	        setBackground(Color.WHITE);
    	// Сконструировать необходимые объекты, используемые в рисовании
    	// Перо для рисования графика
    	        float[] dash = {3,10,12,10,3,10,21,10,12,10,3,10};
    	        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_SQUARE,
    	                BasicStroke.JOIN_MITER, 22.0f, dash, 0.0f);
    	// Перо для рисования осей координат
    	        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
    	                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
    	// Перо для рисования контуров маркеров
    	        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
    	                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
    	// Шрифт для подписей осей координат
    	        axisFont = new Font("Serif", Font.BOLD, 36);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    resetScale(); // Восстановление исходного масштаба
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true; // Начало выделения
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    if (dragRect != null && dragRect.width > 0 && dragRect.height > 0) {
                        scaleToArea(dragRect); // Масштабирование выделенной области
                    }
                    isDragging = false;
                    dragRect = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                showPointCoordinates(e); // Отображение координат точки при наведении
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    dragRect = new Rectangle(dragStart);
                    dragRect.add(e.getPoint()); // Рисование рамки выделения
                    repaint();
                }
            }
        });
    }

    private void showPointCoordinates(MouseEvent e) {
        if (graphicsData == null) return;

        Point mousePoint = e.getPoint();
        for (Double[] point : graphicsData) {
            Point2D.Double graphPoint = xyToPoint(point[0], point[1]);
            if (Math.abs(graphPoint.x - mousePoint.x) < 5 && Math.abs(graphPoint.y - mousePoint.y) < 5) {
                Graphics2D g2d = (Graphics2D) getGraphics();
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("(%.2f, %.2f)", point[0], point[1]),
                        (int) graphPoint.x + 5, (int) graphPoint.y - 5);
                break;
            }
        }
    }

    private void scaleToArea(Rectangle rect) {
        double newMinX = minX + (rect.x / scaleX);
        double newMaxX = minX + ((rect.x + rect.width) / scaleX);
        double newMinY = maxY - ((rect.y + rect.height) / scaleY);
        double newMaxY = maxY - (rect.y / scaleY);

        minX = newMinX;
        maxX = newMaxX;
        minY = newMinY;
        maxY = newMaxY;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);

        repaint();
    }

    private void resetScale() {
        calculateBounds(); // Восстановление границ
        repaint();
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        calculateBounds();
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;

        Graphics2D canvas = (Graphics2D) g;
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);

        if (dragRect != null) { // Рисование рамки выделения
            canvas.setColor(Color.BLACK);
            canvas.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0));
            canvas.draw(dragRect);
        }
    }

    private void calculateBounds() {
        minX = graphicsData[0][0];
        maxX = graphicsData[0][0];
        minY = graphicsData[0][1];
        maxY = graphicsData[0][1];

        for (Double[] point : graphicsData) {
            if (point[0] < minX) minX = point[0];
            if (point[0] > maxX) maxX = point[0];
            if (point[1] < minY) minY = point[1];
            if (point[1] > maxY) maxY = point[1];
        }

        maxX += maxX * 0.25;
        minX -= maxX * 0.25;
        maxY += maxX * 0.2;
        minY -= maxX * 0.1;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);
    }

    protected void paintAxis(Graphics2D canvas) {

		canvas.setStroke(axisStroke);

		canvas.setColor(Color.BLACK);

		canvas.setPaint(Color.BLACK);

		canvas.setFont(axisFont);

		FontRenderContext context = canvas.getFontRenderContext();

		if (minX<=0.0 && maxX>=0.0) {

			canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
					xyToPoint(0, minY)));

			GeneralPath arrow = new GeneralPath();

			Point2D.Double lineEnd = xyToPoint(0, maxY);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());

			arrow.lineTo(arrow.getCurrentPoint().getX()+5,
					arrow.getCurrentPoint().getY()+20);

			arrow.lineTo(arrow.getCurrentPoint().getX()-10,
					arrow.getCurrentPoint().getY());

			arrow.closePath();
			canvas.draw(arrow); 
			canvas.fill(arrow); 

			Rectangle2D bounds = axisFont.getStringBounds("y", context);
			Point2D.Double labelPos = xyToPoint(0, maxY);

			canvas.drawString("y", (float)labelPos.getX() + 10,
					(float)(labelPos.getY() - bounds.getY()));
		}


		if (minY<=0.0 && maxY>=0.0) {

			canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
					xyToPoint(maxX, 0)));

			GeneralPath arrow = new GeneralPath();

			Point2D.Double lineEnd = xyToPoint(maxX, 0);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());

			arrow.lineTo(arrow.getCurrentPoint().getX()-20,
					arrow.getCurrentPoint().getY()-5);

			arrow.lineTo(arrow.getCurrentPoint().getX(),
					arrow.getCurrentPoint().getY()+10);

			arrow.closePath();
			canvas.draw(arrow); 
			canvas.fill(arrow); 

			Rectangle2D bounds = axisFont.getStringBounds("x", context);
			Point2D.Double labelPos = xyToPoint(maxX, 0);

			canvas.drawString("x", (float)(labelPos.getX() -
					bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
		}
	}

    protected void paintGraphics(Graphics2D canvas) {

		canvas.setStroke(graphicsStroke);

		canvas.setColor(Color.RED);

		GeneralPath graphics = new GeneralPath();
		for (int i=0; i<graphicsData.length; i++) {

			Point2D.Double point = xyToPoint(graphicsData[i][0],
					graphicsData[i][1]);
			if (i>0) {

				graphics.lineTo(point.getX(), point.getY());
			} else {
				
				graphics.moveTo(point.getX(), point.getY());
			}
		}
		
		canvas.draw(graphics);
	}


protected boolean dsCondition(double value) {
        
        double intPart = Math.floor( Math.abs(value));
        if(Math.sqrt(intPart)%1==0) {
        	return true;
        }
        else return false;
      
    }

    // Отображение маркеров точек, по которым рисовался график
protected void paintMarkers(Graphics2D canvas) {
	
	canvas.setStroke(markerStroke);
	
	
	GeneralPath marker = new GeneralPath();
	int i=0;
	for (Double[] point: graphicsData) {
		// Инициализировать эллипс как объект для представления маркера
		Point2D.Double point1 = xyToPoint(graphicsData[i][0],
				graphicsData[i][1]);
		
		if (dsCondition(point[1])) {
            canvas.setColor(Color.GREEN);
        } else {
            canvas.setColor(Color.BLACK);
        }
		
		double inc1=5.5;
		double inc2=2.3;
		
		marker.moveTo(point1.getX(), point1.getY());
		marker.lineTo(point1.getX()+inc1, point1.getY());
		marker.lineTo(point1.getX()+inc1, point1.getY()+inc2);
		marker.lineTo(point1.getX()+inc1, point1.getY()-inc2);
		
		marker.moveTo(point1.getX(), point1.getY());
		marker.lineTo(point1.getX()-inc1, point1.getY());
		marker.lineTo(point1.getX()-inc1, point1.getY()+inc2);
		marker.lineTo(point1.getX()-inc1, point1.getY()-inc2);
		
		marker.moveTo(point1.getX(), point1.getY());
		marker.lineTo(point1.getX(), point1.getY()-inc1);
		marker.lineTo(point1.getX()+inc2, point1.getY()-inc1);
		marker.lineTo(point1.getX()-inc2, point1.getY()-inc1);
		
		marker.moveTo(point1.getX(), point1.getY());
		marker.lineTo(point1.getX(), point1.getY()+inc1);
		marker.lineTo(point1.getX()+inc2, point1.getY()+inc1);
		marker.lineTo(point1.getX()-inc2, point1.getY()+inc1);
		
		//Rectangle2D.Double marker = new Rectangle2D.Double();
		/* Эллипс будет задаваться посредством указания координат
		его центра
		и угла прямоугольника, в который он вписан */
		// Центр - в точке (x,y)0
		
		//Point2D.Double center = xyToPoint(point[0], point[1]);
		// Угол прямоугольника - отстоит на расстоянии (3,3)
		
		//Point2D.Double corner = shiftPoint(center, 3, 3);
		// Задать эллипс по центру и диагонали
		
		canvas.draw(marker);
		//marker.setFrameFromCenter(center, corner);
		// Начертить контур маркера
		//canvas.fill(marker); // Залить внутреннюю область маркера
		i++;
	}
}

}