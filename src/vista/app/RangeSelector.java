/*
    Copyright (C) 1996, 1997, 1998 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0beta
	by Nicky Sandhu
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA 95814
    (916)-653-7552
    nsandhu@water.ca.gov

    Send bug reports to nsandhu@water.ca.gov

    This program is licensed to you under the terms of the GNU General
    Public License, version 2, as published by the Free Software
    Foundation.

    You should have received a copy of the GNU General Public License
    along with this program; if not, contact Dr. Francis Chung, below,
    or the Free Software Foundation, 675 Mass Ave, Cambridge, MA
    02139, USA.

    THIS SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES AND CONTRIBUTORS "AS IS" AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES OR ITS CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
    OR SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR PROFITS; OR
    BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
    USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
    DAMAGE.

    For more information about VISTA, contact:

    Dr. Francis Chung
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA  95814
    916-653-5601
    chung@water.ca.gov

    or see our home page: http://wwwdelmod.water.ca.gov/

    Send bug reports to nsandhu@water.ca.gov or call (916)-653-7552

 */
package vista.app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import vista.graph.CoordinateDisplayInteractor;
import vista.graph.Curve;
import vista.graph.GECanvas;
import vista.graph.Scale;

/**
 * Selects a range over the x and y axes of a graph. User interaction consists
 * of starting by an first click to select one corner of the range. A rectangle
 * is drawn from the point of first click and the current mouse location. When
 * the user clicks on the second location, the range selected is availble. A
 * clear selection method clears the range
 * 
 * @author Nicky Sandhu $Author$
 * @version $Rev$ $Date$
 */
public class RangeSelector {
	private GECanvas _gC;
	private Curve _curve;
	private RangeListener rl;
	private double xmin, xmax;
	private double ymin, ymax;
	private FlagEditor fe;

	/**
	 * selects a range on the x axis of the curve in the graph canvas.
	 */
	public RangeSelector(GECanvas gC, Curve curve, FlagEditor fe) {
		_curve = curve;
		_gC = gC;
		this.fe = fe;
		selectRange();
	}

	/**
    *
    */
	void doneSelecting() {
		fe.doneSelecting();
	}

	/**
	 * gets the minimum of range
	 */
	public double getRangeXMin() {
		Scale sc = _curve.getXAxis().getScale();
		xmin = sc.scaleToDC(rl.getRangeMin());
		return xmin;
	}

	/**
	 * gets maximum of range
	 */
	public double getRangeXMax() {
		Scale sc = _curve.getXAxis().getScale();
		xmax = sc.scaleToDC(rl.getRangeMax());
		return xmax;
	}

	/**
    *
    */
	private void selectRange() {
		_gC.addMouseListener(rl = new RangeListener(_gC, _curve));
		_gC.addMouseMotionListener(rl);
	}

	/**
    *
    */
	private class RangeListener implements MouseListener, MouseMotionListener {
		private boolean DEBUG = false;
		private boolean click1 = false, click2 = false;
		private Curve _curve;
		private GECanvas _gC;
		private int x1, x2;
		private Image _gCImage;
		private CoordinateDisplayInteractor _cdi;
		private int y1;
		private int y2;

		/**
      *
      */
		public RangeListener(GECanvas gC, Curve curve) {
			_curve = curve;
			_gC = gC;
			_gC.addMouseMotionListener(_cdi = new CoordinateDisplayInteractor(
					_gC));
			_gC.setDoubleBuffered(true);
			Rectangle r = _gC.getBounds();
			_gCImage = _gC.createImage(r.width, r.height);
		}

		/**
      *
      */
		public int getRangeMax() {
			return Math.max(x1, x2);
		}

		/**
      *
      */
		public int getRangeMin() {
			return Math.min(x1, x2);
		}

		/**
		 * Invoked when the mouse has been clicked on a component.
		 */
		public void mouseClicked(MouseEvent e) {
			Rectangle r = _curve.getBounds();
			if (!click1) {
				click1 = true;
				x1 = Math.min(Math.max(e.getX(), r.x), r.x + r.width);
				y1 = Math.min(Math.max(e.getY(), r.y), r.y + r.height);
			} else if (!click2) {
				click2 = true;
				x2 = Math.min(Math.max(e.getX(), r.x), r.x + r.width);
				y2 = Math.min(Math.max(e.getY(), r.y), r.y + r.height);
				_gC.removeMouseMotionListener(this);
				_gC.addMouseListener(this);
				_gC.removeMouseMotionListener(_cdi);
				_cdi.doneDisplaying();
				RangeSelector.this.doneSelecting();
			}
		}

		/**
		 * Invoked when the mouse button has been moved on a component (with no
		 * buttons no down).
		 */
		public void mouseMoved(MouseEvent e) {
			drawSelectedRegion(e.getX(), e.getY());
		}

		private void drawSelectedRegion(int x, int y) {
			Graphics g = _gCImage.getGraphics();
			g.drawImage(_gC.getGraphicElementImage(), 0, 0, null);
			// g.setClip(r);
			if (click1 && click2) {
				Color fillColor = new Color(100, 100, 100, 125);
				Color oldColor = g.getColor();
				g.setColor(fillColor);
				g.fillRect(x1, y1, x1 - x2, y1 - y2);
				g.setColor(oldColor);
			} else if (click1) { // draw a rectangle
				g.drawRect(x1, y1, x1 - x, y1 - y);
			} else { // draw a cross hair at current position
				int w = 4;// half of cross hair width
				g.drawLine(x - w, y, x + w, y);
				g.drawLine(x, y - w, x, y + w);
			}
			_gC.getGraphics().drawImage(_gCImage, 0, 0, null);
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseDragged(MouseEvent e) {
		}
	} // end of Range Listener
}
