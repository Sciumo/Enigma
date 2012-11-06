package enigma.console;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import enigma.core.Enigma;

/** 
 * A concrete Console implementation which hooks up to an arbitrary TextWindow
 * for its low-level functionality.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class DefaultConsoleImpl implements Console {
    private static final Object editLock = new String("editLock");
    
    /** true if we are currently accepting user input */
    private boolean editMode;
    
    /** true to echo asterisks instead of the typed characters while in edit mode */
    private boolean passwordMode;
    
    private int cursorIndex;
    private StringBuffer enteredText = new StringBuffer();
    
    /** The index of the currently displayed command from the command buffer.  */
    private int currentCommandRecall;
    
    /** A list of all remembered commands. */
    private List commandRecallBuffer = new ArrayList();
    

    /** The TextWindow this Console is managing. */
    private TextWindow window;
    
    
    /** The TextAttributes used for stream & writer output. */
    private TextAttributes textAttributes = Enigma.getSystemTextAttributes("attributes.console.default");
    
    
    private PrintWriter consoleWriter = new PrintWriter(new Writer() {
        public void close() { }
        
        
        public void flush() { }
        
        
        public void write(char c) {
            window.output(c, textAttributes);
        }
        
        
        public void write(char[] c, int offset, int length) {
            window.output(c, offset, length, textAttributes);
        }
        
        
        public void write(String str) {
            window.output(str, textAttributes);
        }
    });
    
    
    private PrintStream consolePrintStream = new PrintStream(new OutputStream() {
        public void close() {
            consoleWriter.close();
        }
        
        
        public void flush() {
            consoleWriter.flush();
        }
        
        
        public void write(int b) {
            consoleWriter.write((char) b);
        }
        
        
        public void write(byte[] b, int offset, int length) {
            consoleWriter.write(new String(b, offset, length));
        }
        
        
        public void write(byte[] b) {
            consoleWriter.write(new String(b));
        }
    });
    
    
    private InputStream consoleInputStream = new InputStream() {
        byte[] data;
        int offset;
        
        public int read() {
            if (data == null) {
                data = (readLine() + "\n").getBytes();
                offset = 0;
            }
            if (offset >= data.length) {
                data = null;
                return -1;
            }
            return data[offset++];
        }
        
        
        public int available() {
            return data != null ? data.length - offset : 0;
        }
    };


    private Reader consoleReader = new InputStreamReader(consoleInputStream);              
        
    
    
    /** 
     * Creates a <code>Console</code> wrapping the specified {@link TextWindow}. 
     * <code>DefaultConsoleImpl</code> expects to be the sole manager of the 
     * <code>TextWindow</code>; any further direct interaction with the 
     * <code>TextWindow</code> may result in unspecified behavior. 
     *
     *@param window the <code>TextWindow</code> to use for low-level input and output
     */
    public DefaultConsoleImpl(TextWindow window) {
        if (window == null)
            throw new NullPointerException();
        this.window = window;
        window.addKeyListener(createKeyListener());
        window.setCursorType(TextWindow.CURSOR_INVISIBLE);
    }
    
    
    protected KeyListener createKeyListener() {
        return new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (editMode)
                        handleKeyPressed(e.getKeyCode());
                    else
                        buffer(e);
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
                    if (editMode)
                        handleKeyReleased(e.getKeyCode());
                    else
                        buffer(e);
                }
            }

            public void keyTyped(KeyEvent e) {
                if (editMode)
                    handleKeyTyped(e.getKeyChar());
                else
                    buffer(e);
            }
        };
    }
    
    
    protected void buffer(KeyEvent e) {
    }
    

    protected void processBuffer() {
    }


    private void enterEditMode() {
        //assert Thread.holdsLock(editLock);
        window.setCursorType(TextWindow.CURSOR_INSERT);
        editMode = true;
        enteredText.setLength(0);
        cursorIndex = 0;
        currentCommandRecall = commandRecallBuffer.size();
        commandRecallBuffer.add("");
        processBuffer();
    }
    
    
    private void exitEditMode() {
        //assert Thread.holdsLock(editLock);
        window.setCursorType(TextWindow.CURSOR_INVISIBLE);
        editMode = false;
        if (enteredText.length() > 0)
            commandRecallBuffer.set(commandRecallBuffer.size() - 1, enteredText.toString());
        else
            commandRecallBuffer.remove(commandRecallBuffer.size() - 1);
    }

    
    public synchronized String readLine() {
        String result;
        synchronized (editLock) {
            enterEditMode();
            try {
                editLock.wait();
                // exitEditMode is performed in event thread
            }
            catch (InterruptedException e) { }
            result = enteredText.toString();
        }
        return result;
    }
    
    
    public synchronized String readPassword() {
        try {
            passwordMode = true;
            return readLine();
        }
        finally {
            passwordMode = false;
        }
    }
    

    protected void setCommandRecall(int newIndex) {
        commandRecallBuffer.set(currentCommandRecall, enteredText.toString());
        currentCommandRecall = newIndex;
        int oldLength = enteredText.length();
        enteredText = new StringBuffer(oldLength);
        for (int i = 0; i < oldLength; i++)
            enteredText.append(' ');
        moveCursor(-cursorIndex);
        refreshEndOfString();
        enteredText = new StringBuffer((String) commandRecallBuffer.get(currentCommandRecall));
        refreshEndOfString();
        moveCursor(enteredText.length());
        //assert cursorIndex == enteredText.length() : "expected cursorIndex to be at end of string (length = " + enteredText.length() + ", cursorIndex = " + cursorIndex + ")";
    }
    

    public void commandRecallUp() {
        int newIndex = Math.max(0, currentCommandRecall - 1);
        setCommandRecall(newIndex);
    }
    

    public void commandRecallDown() {
        int newIndex = Math.min(commandRecallBuffer.size() - 1, currentCommandRecall + 1);
        setCommandRecall(newIndex);
    }
    
    
    protected void refreshEndOfString() {
        window.setCursorType(TextWindow.CURSOR_INVISIBLE);
        if (!passwordMode) {
            window.output(enteredText.substring(cursorIndex));
        }
        else {
            int length = enteredText.length();
            for (int i = cursorIndex; i < length; i++)
                window.output('*', textAttributes);
        }
        window.output(' ');
        int delta = enteredText.length() - cursorIndex + 1;
        cursorIndex += delta;
        moveCursor(-delta);
        window.setCursorType(TextWindow.CURSOR_INSERT);
    }
    
    
    protected void handleKeyPressed(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:    commandRecallUp(); break;
            case KeyEvent.VK_DOWN:  commandRecallDown(); break;
            case KeyEvent.VK_PAGE_UP:  window.pageUp(); break;
            case KeyEvent.VK_PAGE_DOWN:  window.pageDown(); break;
            case KeyEvent.VK_LEFT:  if (cursorIndex > 0)
                                        moveCursor(-1);
                                    break;
            case KeyEvent.VK_RIGHT: if (cursorIndex < enteredText.length())
                                        moveCursor(1);
                                    break;
            case KeyEvent.VK_HOME: moveCursor(-cursorIndex); break;
            case KeyEvent.VK_END:  moveCursor(enteredText.length() - cursorIndex);
            case KeyEvent.VK_DELETE: if (cursorIndex < enteredText.length()) {
                                         enteredText.deleteCharAt(cursorIndex);
                                         refreshEndOfString();
                                     }
                                     break;
        }
        //assert cursorIndex >= 0 && cursorIndex <= enteredText.length() : "cursorIndex out of bounds";
    }
        

    protected void handleKeyReleased(int keyCode) {
        //assert cursorIndex >= 0 && cursorIndex <= enteredText.length() : "cursorIndex out of bounds";
    }
    
    
    protected void handleKeyTyped(char keyChar) {
        if (keyChar == 8) { // backspace
            if (cursorIndex > 0) {
                moveCursor(-1);
                enteredText.deleteCharAt(cursorIndex);
                refreshEndOfString();
            }
        }
        else if (keyChar == 27) // escape
            clearEnteredText();
        else if (keyChar == '\n') { // newline
            moveCursor(enteredText.length() - cursorIndex);
            window.output('\n', textAttributes);
            synchronized (editLock) {
                editLock.notify();
                exitEditMode();
            }
        }
        else if (keyChar > 31) { // non-control character
            enteredText.insert(cursorIndex++, keyChar);
            
            // this moves the cursor, so no moveCursor(1) needed
            if (!passwordMode)
                window.outputImmediately(keyChar, textAttributes); 
            else
                window.outputImmediately('*', textAttributes);
                
            if (cursorIndex < enteredText.length())
                refreshEndOfString();
        }

        //assert cursorIndex >= 0 && cursorIndex <= enteredText.length() : "cursorIndex out of bounds";
    }


    private void clearEnteredText() {
        moveCursor(-cursorIndex);
        for (int i = 0; i < enteredText.length(); i++)
            enteredText.setCharAt(i, ' ');
        refreshEndOfString();
        enteredText.setLength(0);
    }
    
    
    private void moveCursor(int delta) {
        cursorIndex += delta;
        int newCursorX = window.getCursorX() + delta;
        int newCursorY = window.getCursorY();
        while (newCursorX < 0) {
            newCursorY--;
            newCursorX += window.getColumns();
        }
        while (newCursorX >= window.getColumns()) {
            newCursorY++;
            newCursorX -= window.getColumns();
        }
        while (newCursorY >= window.getRows()) {
            window.insertRow(window.getRows() - 1);
            newCursorY--;
        }
        window.setCursorPosition(newCursorX, newCursorY);
    }
    
    
    public TextAttributes getTextAttributes() {
        return textAttributes;
    }
    
    
    public void setTextAttributes(TextAttributes textAttributes) {
        this.textAttributes = textAttributes;
    }
    

    public Reader getReader() {
        return consoleReader;
    }
    
    
    public PrintWriter getWriter() {
        return consoleWriter;
    }


    public InputStream getInputStream() {
        return consoleInputStream;
    }
    
    
    public PrintStream getOutputStream() {
        return consolePrintStream;
    }
    
    
    public boolean isTextWindowAvailable() {
        return true;
    }
    
    
    public TextWindow getTextWindow() {
        return window;
    }
    
    
    public String getTitle() {
        return window.getTitle();
    }
    
    
    public void setTitle(String title) {
        window.setTitle(title);
    }
}