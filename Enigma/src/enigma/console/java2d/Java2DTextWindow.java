package enigma.console.java2d;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;

import enigma.console.*;
import enigma.core.*;
import enigma.event.*;
 
/** 
 * An implementation of <code>TextWindow</code> which uses Java2D to render its 
 * display.  <code>Java2DTextWindow</code> uses the <code>Monospaced</code> font
 * by default for its output;  other fonts may be assigned using <code>setFont()</code>,
 * but only monospaced fonts will function properly.
 *
 * <p><code>Java2DTextWindow</code> provides built-in scrollback buffer support.
 * It must be placed in <code>JScrollPane</code> for this support to function.</p>
 *
 *@status.unstable
 *@see TextWindow
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class Java2DTextWindow extends JComponent implements TextWindow, Scrollable {
    private static final int REPAINT_DELAY = 15;
    private static final int CURSOR_BLINK_DELAY = 500;

    private static TextAttributes defaultAttributes = Enigma.getSystemTextAttributes("attributes.console.default"); 

    private static final Color DEFAULT_CURSOR_COLOR = invert(defaultAttributes.getBackground());


    /** Maximum number of rows which will ever be remembered. */
    private int scrollback;
    
    /** Number of rows currently being remembered. */
    private int rows;
    
    /** Number of rows being displayed at one time. */
    private int logicalRows;
    
    /** Number of columns in each row. */
    private int columns;

    /** Number of columns actually being displayed. */
    private int logicalColumns;

    private int charWidth = 16;
    private int charHeight = 16;
    private int baseline = 9;

    private char[][] chars;                // [rows][columns]
    private TextAttributes[][] attributes; // [rows][columns]
    
    private int cursorType;
    private boolean cursorState;
    private Color cursorColor;
    private int cursorX = -1;
    private int cursorY = -1;

    private boolean valid;
    private boolean snapToBottom;
    private Rectangle dirtyRegion;
    
    private Timer cursorTimer = new Timer(CURSOR_BLINK_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cursorState = !cursorState;
                repaintChar(cursorX, cursorY);
            }
        });

    private Timer repaintTimer = new Timer(REPAINT_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (Java2DTextWindow.this) {
                    if (!valid || snapToBottom) {
                        JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, Java2DTextWindow.this);
                        if (!valid) {
                            invalidate();
                            if (sp != null)
                                sp.validate();
                        }
                        if (snapToBottom = true) {
                            if (sp != null) {
                                JScrollBar v = sp.getVerticalScrollBar();
                                v.setValue(v.getMaximum() - v.getVisibleAmount());
                            }
                            snapToBottom = false;
                        }
                    }
                    if (dirtyRegion != null) {
                        repaint(dirtyRegion.x * charWidth, dirtyRegion.y * charHeight, 
                                dirtyRegion.width * charWidth, dirtyRegion.height * charHeight);
                        dirtyRegion = null;
                    }
                }
            }
        });
    { repaintTimer.setRepeats(false); }


    /** 
     * Constructs a new <code>Java2DTextWindow</code> of the specified size.
     * The <code>columns</code> and <code>rows</code> parameters merely
     * control the component's initial and preferred sizes;  changes to the
     * component's size will result in these values being recomputed.
     *
     * <p>The <code>scrollback</code> parameter controls the maximum number 
     * of rows that the component will remember at any given time, regardless
     * of the number actually displayed.  Therefore, the user will be
     * able to scroll back <code>(scrollback - rows)</code> rows.  The
     * <code>rows</code> property, as previously mentioned, may change as
     * the component is resized, but the <code>scrollback</code> property 
     * is hidden and immutable.</p>
     */
    public Java2DTextWindow(int columns, int rows, int scrollback) {
        setFont(new Font("Monospaced", 0, 12));
        setGridSize(columns, rows, columns);
        this.scrollback = scrollback;
        this.logicalRows = rows;
        setCursorColor(DEFAULT_CURSOR_COLOR);
        cursorTimer.start();
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }
    
    
    public String getTitle() {
        Frame f = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);
        if (f != null)
            return f.getTitle();
        return null;
    }


    public void setTitle(String title) {
        Frame f = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);
        if (f != null)
            f.setTitle(title);
    }
    
    
    public void setFont(Font font) {
        FontRenderContext context = new FontRenderContext(null, false, false);
        Rectangle2D bounds = font.getStringBounds("M", context);
        charWidth  = (int) bounds.getWidth();
        charHeight = (int) bounds.getHeight();
        baseline   = (int) font.getLineMetrics("g", context).getAscent();
        super.setFont(font);
        revalidate();
    }
    

    private synchronized void setGridSize(int columns, int rows, int logicalColumns) {
        while (cursorY >= rows)
            scrollByOneRow();

        this.logicalColumns = logicalColumns;
        if (columns == this.columns) { // not changing number of columns, use more efficient algorithm
            int minRows = Math.min(rows, this.rows);
            char[][] oldChars = chars;
            if (oldChars == null)
                oldChars = new char[0][0];
            chars = new char[rows][columns];
            System.arraycopy(oldChars, 0, chars, 0, minRows);
            for (int i = this.rows; i < rows; i++) {
                char[] newRow = new char[columns];
                Arrays.fill(newRow, ' ');
                chars[i] = newRow;
            }
        
            TextAttributes[][] oldAttributes = attributes;
            if (oldAttributes == null)
                oldAttributes = new TextAttributes[0][0];
            attributes = new TextAttributes[rows][columns];
            System.arraycopy(oldAttributes, 0, attributes, 0, minRows);
            for (int i = this.rows; i < rows; i++) {
                TextAttributes[] newRow = new TextAttributes[columns];
                Arrays.fill(newRow, defaultAttributes);
                attributes[i] = newRow;
            }
        }
        else { // changing number of columns, use slower algorithm
            int minRows = Math.min(rows, this.rows);
            int minColumns = Math.min(columns, this.columns);
            char[][] oldChars = chars;
            if (oldChars == null)
                oldChars = new char[0][0];
            chars = new char[rows][columns];
            for (int i = 0; i < minRows; i++) {
                char[] newRow = new char[columns];
                System.arraycopy(oldChars[i], 0, newRow, 0, minColumns);
                if (columns > minColumns)
                    Arrays.fill(newRow, minColumns, columns, ' ');
                chars[i] = newRow;
            }                
            for (int i = oldChars.length; i < rows; i++) {
                char[] newRow = new char[columns];
                Arrays.fill(newRow, ' ');
                chars[i] = newRow;
            }
        
            TextAttributes[][] oldAttributes = attributes;
            if (oldAttributes == null)
                oldAttributes = new TextAttributes[0][0];
            attributes = new TextAttributes[rows][columns];
            for (int i = 0; i < minRows; i++) {
                TextAttributes[] newRow = new TextAttributes[columns];
                System.arraycopy(oldAttributes[i], 0, newRow, 0, minColumns);
                attributes[i] = newRow;
            }                
            for (int i = oldAttributes.length; i < rows; i++)
                attributes[i] = new TextAttributes[columns];
        }
        this.columns = columns;
        this.rows = rows;
        fireResized();
        valid = false; // avoid overhead of calling invalidate() in a tight loop
        repaint();
    }
    
    
    public synchronized int getRows() {
        return logicalRows;
    }
    
    
    public synchronized int getColumns() {
        return logicalColumns;
    }
    
    
    public synchronized Dimension getMinimumSize() {
        int parentHeight = getParent().getHeight();
        Insets parentInsets = getParent().getInsets();
        parentHeight -= parentInsets.top + parentInsets.bottom;
        return new Dimension(logicalColumns * getCharWidth(), Math.max(parentHeight, rows * getCharHeight()));
    }
    

    public synchronized Dimension getPreferredSize() {
        return getMinimumSize();
    }
    
    
    public synchronized Dimension getMaximumSize() {
        return getPreferredSize();
    }
    
    
    public synchronized int getCursorX() {
        return cursorX;
    }
    

    public synchronized int getCursorY() {
        return cursorY + logicalRows - rows;
    }
    
    
    public synchronized void setCursorPosition(int cursorX, int cursorY) {
        cursorY += rows - logicalRows;
        repaintChar(this.cursorX, this.cursorY);
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        cursorState = true;
        cursorTimer.restart();
        repaintChar(cursorX, cursorY);
    }
    
    
    public int getCharWidth() {
        return charWidth;
    }
    
    
    public int getCharHeight() {
        return charHeight;
    }
    
    
    public int getCursorType() {
        return cursorType;
    }
    
    
    public void setCursorType(int cursorType) {
        this.cursorType = cursorType;
    }
    
    
    public void addTextWindowListener(TextWindowListener l) {
        listenerList.add(TextWindowListener.class, l);
    }


    public void removeTextWindowListener(TextWindowListener l) {
        listenerList.remove(TextWindowListener.class, l);
    }
    
    
    protected void fireResized() {
        TextWindowEvent e = new TextWindowEvent(this, TextWindowEvent.RESIZED);
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TextWindowListener.class) {
                TextWindowListener listener = ((TextWindowListener) listeners[i + 1]);
                listener.textWindowResized(e);
            }
        }
     }
        

    public void addKeyListener(KeyListener l) {
        listenerList.add(KeyListener.class, l);
    }


    public void removeKeyListener(KeyListener l) {
        listenerList.remove(KeyListener.class, l);
    }
    
    
    protected void processKeyEvent(KeyEvent e) {
        fireKeyEvent(e);
        if (e.getID() == KeyEvent.KEY_TYPED)
            scrollRectToVisible(new Rectangle(0, cursorY * getCharHeight(), 1, (cursorY + 1) * getCharHeight() - 1));
    }    
    
    
    private void fireKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == KeyListener.class) {
                KeyListener listener = ((KeyListener) listeners[i + 1]);
                switch (e.getID()) {
                    case KeyEvent.KEY_PRESSED:  listener.keyPressed(e);   break;
                    case KeyEvent.KEY_RELEASED: listener.keyReleased(e);  break;
                    case KeyEvent.KEY_TYPED:    listener.keyTyped(e);     break;
                }
            }
        }
    }
        

    public void addTextMouseListener(TextMouseListener l) {
        listenerList.add(TextMouseListener.class, l);
    }


    public void removeTextMouseListener(TextMouseListener l) {
        listenerList.remove(TextMouseListener.class, l);
    }
    
    
    public void addTextMouseMotionListener(TextMouseMotionListener l) {
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        listenerList.add(TextMouseMotionListener.class, l);
    }


    public void removeTextMouseMotionListener(TextMouseMotionListener l) {
        listenerList.remove(TextMouseMotionListener.class, l);
    }
    
    
    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED)
            requestFocus();
        fireMouseEvent(e);
    }
    
    
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
        fireMouseMotionEvent(e);
    }
    
    
    private TextMouseEvent createTextMouseEvent(MouseEvent e) {
        int bias = e.getX() % getCharWidth() < (getCharWidth() / 2) ? TextMouseEvent.BIAS_LEFT : TextMouseEvent.BIAS_RIGHT;
        return new TextMouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), e.getX() / getCharWidth(),
                                                e.getY() / getCharHeight() - rows + logicalRows, e.getClickCount(), e.isPopupTrigger(), 
                                                bias); 
    }
    
    
    private void fireMouseEvent(MouseEvent e) {
        TextMouseEvent textMouseEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TextMouseListener.class) {
                if (textMouseEvent == null)
                    textMouseEvent = createTextMouseEvent(e);
                
                TextMouseListener listener = ((TextMouseListener) listeners[i + 1]);
                switch (e.getID()) {
                    case MouseEvent.MOUSE_PRESSED:  listener.mousePressed(textMouseEvent);   break;
                    case MouseEvent.MOUSE_RELEASED: listener.mouseReleased(textMouseEvent);  break;
                    case MouseEvent.MOUSE_CLICKED:  listener.mouseClicked(textMouseEvent);   break;
                }
            }
        }
    }
        

    private void fireMouseMotionEvent(MouseEvent e) {
        TextMouseEvent textMouseEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TextMouseMotionListener.class) {
                if (textMouseEvent == null)
                    textMouseEvent = createTextMouseEvent(e);
                
                TextMouseMotionListener listener = ((TextMouseMotionListener) listeners[i + 1]);
                switch (e.getID()) {
                    case MouseEvent.MOUSE_MOVED:    listener.mouseMoved(textMouseEvent);     break;
                    case MouseEvent.MOUSE_DRAGGED:  listener.mouseDragged(textMouseEvent);   break;
                }
            }
        }
    }
        

    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        cursorTimer.restart();
        cursorState = e.getID() == FocusEvent.FOCUS_GAINED;
        repaintChar(cursorX, cursorY);
    }
    
    
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        // for up and down arrow, we need to prevent our scrollpane from handling them so that
        // they can be used for command recall.  for page up and page down, it would usually
        // be okay for the scrollpane to handle them, but we need to deliver them so that
        // whatever program is running gets a chance to interpret them.
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN ||
                e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
            return true;
        return super.processKeyBinding(ks, e, condition, pressed);
    }
    
    
    // update the cursor appropriately for just having output character c
    // \n is the only control character handled here, everything else should
    // be done at a higher level
    protected final void advance(char c) {
        cursorState = true;
        cursorTimer.restart();
        switch (c) {
            case '\n': cursorX = 0;  if (++cursorY >= rows) scrollByOneRow();  break;
            case '\r': break;
            default:
                if (++cursorX >= logicalColumns) {
                    cursorX = 0;
                    if (++cursorY >= rows)
                        scrollByOneRow();
                }
        }
        repaintChar(cursorX, cursorY);
    }
    
    
    private void outputRaw(char c, TextAttributes attr, boolean immediate) {
        //assert Thread.holdsLock(this);
        if (attr == null)
            throw new NullPointerException("attributes may not be null");
        if (c == '\t') {
            outputRaw(' ', attr, false);
            outputRaw(' ', attr, false);
            outputRaw(' ', attr, false);
            outputRaw(' ', attr, false);
        }
        else if (c == '\r')
            ; // do nothing
        else if (c == '\n')
            advance(c);
        else {
            try {
                int startingCursorX = this.cursorX;
                int startingCursorY = this.cursorY;
                chars[startingCursorY][startingCursorX] = c;
                attributes[startingCursorY][startingCursorX] = attr;
                advance(c);

                if (!immediate)
                    repaintChar(startingCursorX, startingCursorY);
                else {
                    paintImmediately(startingCursorX * charWidth, startingCursorY * charHeight,
                                        charWidth, charHeight);
                    paintImmediately(cursorX * charWidth, cursorY * charHeight,
                                        charWidth, charHeight);
                }
            }
            catch (ArrayIndexOutOfBoundsException e) { // can happen if thread was terminated in the middle of output
                if (cursorX < 0)
                    cursorX = 0;
                else if (cursorX >= logicalColumns)
                    cursorX = logicalColumns - 1;
                    
                if (cursorY < 0)
                    cursorY = 0;
                else if (cursorY >= rows)
                    cursorY = rows - 1;
                outputRaw(c, attr, false); // cursor should be in valid location, try again
            }
        }
    }


 
    public synchronized void output(char c) {
        outputRaw(c, defaultAttributes, false);
    }
    
        
    public synchronized void output(char c, TextAttributes attributes) {
        outputRaw(c, attributes, false);
    }
    
    
    public synchronized void output(int x, int y, char c) {
        output(x, y, c, defaultAttributes);
    }
    
    
    public synchronized void output(int x, int y, char c, TextAttributes attributes) {
        chars[y][x] = c;
        this.attributes[y][x] = attributes;
        repaintChar(x, y);
    }

    
    public synchronized void output(char[] c, int offset, int length) {
        output(c, offset, length, defaultAttributes);
    }
    
    
    public synchronized void output(char[] c, int offset, int length, TextAttributes attributes) {
        for (int i = offset; i < length; i++)
            output(c[i], attributes);
    }
    
        
    public synchronized void output(String s) {
        output(s, defaultAttributes);
    }
    
    
    public synchronized void output(String s, TextAttributes attributes) {
        int length = s.length();
        for (int i = 0; i < length; i++)
            output(s.charAt(i), attributes);
    }
    
    
    public synchronized void outputImmediately(char c, TextAttributes attributes) {
        outputRaw(c, attributes, true);
    }
    
    
    // hasn't been tested except with last row, may not work in other cases
    public synchronized void insertRow(int row) {
        // don't use copyArea to do the scroll -- copyArea
        // would have to run for each line scrolled, while
        // this approach doesn't update anything until the
        // next paint.  This is at least an order of 
        // magnitude faster in general usage.
        
        char[] spareCharRow;
        TextAttributes[] spareAttributeRow;
        
        if (rows < scrollback) {
            spareCharRow = new char[columns];
            spareAttributeRow = new TextAttributes[columns];
            if (row == rows - 1)
                row++;
            setGridSize(columns, rows + 1, logicalColumns);
            System.arraycopy(chars, row, chars, row + 1, rows - row - 1);
            System.arraycopy(attributes, row, attributes, row + 1, rows - row - 1);
        }
        else {
            spareCharRow = chars[0];
            if (row > 0)
                System.arraycopy(chars, 1, chars, 0, row);
            
            spareAttributeRow = attributes[0];
            if (row > 0)
                System.arraycopy(attributes, 1, attributes, 0, row);
        }

        Arrays.fill(spareCharRow, ' ');
        chars[row] = spareCharRow;

        Arrays.fill(spareAttributeRow, defaultAttributes);
        attributes[row] = spareAttributeRow;

        repaint();
    }


    private synchronized void scrollByOneRow() {
        int oldRows = rows;
        snapToBottom = true;
        insertRow(rows - 1);
        if (rows == oldRows)
            cursorY--; // otherwise, grid just got bigger & no need to move cursor
    }
    
    
    protected void paintRun(Graphics g, int row, int start, int end, TextAttributes attributes) {
        if (attributes == null)
            attributes = defaultAttributes;
        int startX = start * charWidth;
        int startY = row * charHeight;
        Color background = attributes.getBackground();
        if (!background.equals(defaultAttributes.getBackground())) {
            g.setColor(background);
            g.fillRect(startX, startY , (end - start) * charWidth, charHeight);
        }
        g.setColor(attributes.getForeground());
        g.drawChars(chars[row], start, end - start, startX, startY + baseline);
    }
    
    
    public synchronized void repaint() {
        dirtyRegion = new Rectangle(0, 0, getWidth(), getHeight());
        if (!repaintTimer.isRunning())
            repaintTimer.start();
    }
    
    
    private synchronized void repaintChar(int x, int y) {
        if (dirtyRegion == null)
            dirtyRegion = new Rectangle(x, y, 1, 1);
        else
            SwingUtilities.computeUnion(x, y, 1, 1, dirtyRegion);

        if (!repaintTimer.isRunning())
            repaintTimer.start();
    }    

    
    private Rectangle visibleRect = new Rectangle(); // avoid temporary object creation
    
    public void paint(Graphics g) {
        int startRow = 0;
        int endRow = rows;
        int startColumn = 0;
        int endColumn = columns;
    
        Rectangle clip = g.getClipRect();
        if (clip == null) {
            computeVisibleRect(visibleRect);
            clip = visibleRect;
        }
        
        g.setColor(defaultAttributes.getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
            
        startRow = clip.y / charHeight;
        endRow = Math.min(rows, startRow + (clip.height + charHeight - 1) / charHeight + 1);
        startColumn = clip.x / charWidth;
        endColumn = Math.min(columns, startColumn + (clip.width + charWidth - 1) / charWidth + 1);

        for (int i = startRow; i < endRow; i++) {
            int start = startColumn;
            TextAttributes currentAttributes = defaultAttributes;
            for (int j = startColumn; j < endColumn; j++) {
                if (currentAttributes != attributes[i][j]) {
                    if (start != j)
                       paintRun(g, i, start, j, currentAttributes);
                    start = j;
                    currentAttributes = attributes[i][j];
                }
            }
            
            paintRun(g, i, start, endColumn, currentAttributes);
        }

        paintCursor(g);
    }
    
    
    public Color getCursorColor() {
        //assert cursorColor != null : "cursorColor undefined";
        return cursorColor;
    }
    
    
    public void setCursorColor(Color cursorColor) {
        if (cursorColor == null)
            throw new IllegalArgumentException("cursorColor may not be null");
        this.cursorColor = cursorColor;
    }
    
    
    public void paintCursor(Graphics g) {
        if (cursorState && hasFocus()) {
            switch (cursorType) {
                case CURSOR_OVERSTRIKE:
                case CURSOR_INSERT: 
                    int x = cursorX * getCharWidth();
                    int y = cursorY * getCharHeight();
                    g.setColor(getCursorColor());
                    g.setXORMode(defaultAttributes.getBackground());
                    g.drawLine(x, y, x, y + getCharHeight() - 1);
                    break;
                case CURSOR_INVISIBLE: break;
                default: //assert false : "cursorType is invalid";
            }
        }
    }
    

    public synchronized void doLayout() { 
        Rectangle rect = getVisibleRect();
        int columns = rect.width / getCharWidth();
        logicalRows =  rect.height / getCharHeight();
        if (columns != logicalColumns || logicalRows > this.rows) {
             setGridSize(Math.max(columns, this.columns), Math.max(logicalRows, this.rows), columns);
        }
        if (cursorX == -1 || cursorY == -1) {
            setCursorPosition(0, 0);
            scrollRectToVisible(new Rectangle(0, cursorY * getCharHeight(),
                                   columns * getCharWidth(), logicalRows * getCharHeight()));
        }
    }
    
    
    public void validate() {
        valid = true;
        super.validate();
    }
    
    
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(columns * getCharWidth(), logicalRows * getCharHeight());
    }


    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        // quick hack, need to implement properly
        return (logicalRows - 3) * getCharHeight();
    }
    
    
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }


    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        // quick hack, need to implement properly
        return getCharHeight();
    }
    
    
    public void pageUp() {
        Rectangle rect = getVisibleRect();
        rect.y -= getScrollableBlockIncrement(rect, JScrollBar.VERTICAL, -1);
        scrollRectToVisible(rect);
    }
    
    
    public void pageDown() {
        Rectangle rect = getVisibleRect();
        rect.y += getScrollableBlockIncrement(rect, JScrollBar.VERTICAL, 1);
        scrollRectToVisible(rect);
    }
    
    
    private static Color invert(Color color) {
        return new Color(255 - color.getRed(),
                         255 - color.getGreen(), 
                         255 - color.getBlue(),
                         color.getAlpha());
    }
}