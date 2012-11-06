package enigma.console.terminal;

import java.awt.Color;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.Arrays;

import enigma.console.Console;
import enigma.console.TextAttributes;
import enigma.console.TextWindow;
import enigma.console.TextWindowNotAvailableException;

/** 
 * An <code>OutputStream</code> which interprets ANSI terminal escape sequences
 * and causes them to display properly on a <code>Console</code>.
 *
 *@see Console
 */
public class AnsiOutputStream extends FilterOutputStream {
    private static final int DARK_LEVEL = 180;
    private static final int BRIGHT_LEVEL = 255;
    private static final Color[] COLORS = {
                                            Color.black,
                                            new Color(DARK_LEVEL, 0, 0),
                                            new Color(0, DARK_LEVEL, 0),
                                            new Color(DARK_LEVEL, DARK_LEVEL, 0),
                                            new Color(0, 0, DARK_LEVEL),
                                            new Color(DARK_LEVEL, 0, DARK_LEVEL),
                                            new Color(0, DARK_LEVEL, DARK_LEVEL),
                                            new Color(DARK_LEVEL, DARK_LEVEL, DARK_LEVEL),
                                        
                                            new Color(96, 96, 96),
                                            new Color(BRIGHT_LEVEL, 0, 0),
                                            new Color(0, BRIGHT_LEVEL, 0),
                                            new Color(BRIGHT_LEVEL, BRIGHT_LEVEL, 0),
                                            new Color(0, 0, BRIGHT_LEVEL),
                                            new Color(BRIGHT_LEVEL, 0, BRIGHT_LEVEL),
                                            new Color(0, BRIGHT_LEVEL, BRIGHT_LEVEL),
                                            new Color(BRIGHT_LEVEL, BRIGHT_LEVEL, BRIGHT_LEVEL)
                                         };
                                         
    private boolean bright;
    private int foreground;
    private int background;
                                         
    private static final int ESCAPE = '\033';
    
    /** The <code>Console</code> that this stream is managing. */
    protected Console console;
    
    /** The <code>TextWindow</code> of the <code>Console</code> that this stream is managing. */
    protected TextWindow textWindow;
    
    /** <code>true</code> if a partial escape sequence has been written. */
    protected boolean inEscape;
    
    /** The escape sequence currently being processed. */
    protected StringBuffer escapeSequence = new StringBuffer();
    
    
    private final byte[] byteHolder = new byte[1];
    private int parameterStart;
    private int savedX;
    private int savedY;


    /** 
     * Constructs a new <code>AnsiOutputStream</code> which will display its output
     * on the given <code>Console</code>.  The <code>Console</code> in question must
     * provide <code>TextWindow</code> support.
     *
     *@param console the <code>Console</code> to which the stream's output is sent
     *@throws TextWindowNotAvailableException if the <code>Console</code> does not provide <code>TextWindow</code> support
     */
    public AnsiOutputStream(Console console) throws TextWindowNotAvailableException {
        super(console.getOutputStream());
        this.console = console;
        this.textWindow = console.getTextWindow();
    }
    
    
    /** 
     * Processes a partial or complete escape sequence.  The escape sequence may be processed
     * piecewise, with each call to <code>processEscape</code> reading more of the escape
     * sequence.  Data will be read from the byte array either until the end of the escape
     * sequence (in which case the escape is complete and <code>inEscape</code> will be reset
     * to <code>false</code>) or until the end of the byte array, in which case the escape
     * sequence is not complete, <code>inEscape</code> remains <code>true</code>, and the
     * next call to <code>processEscape</code> will continue to read bytes of the same
     * escape sequence.
     * <p>Erroneous escape sequences (those which do not conform to the ANSI standard) are
     * read until they are determined to be incorrect, at which point <code>inEscape</code>
     * is reset to false and the invalid escape sequence is discarded.</p>
     *
     *@param b the byte array containing the escape sequence or portion thereof
     *@param index the offset within the byte array from which to begin reading the escape sequence
     */
    protected int processEscape(byte[] b, int index) {
        int start = index;
        boolean complete = false; // we know we want at least one character, otherwise this method would not have been called
        while (!complete && index < b.length) {
            escapeSequence.append((char) b[index++]);
            complete = tryToProcessEscapeSequence();
        }
        return index - start;
    }
    
    
    /**
     * Attempts to process the current escape sequence.  Returns <code>true</code>
     * if no more characters are required (the escape sequence is complete or invalid)
     * and <code>false</code> if more characters are required (the escape sequence is
     * potentially valid, but incomplete).
     */
    protected boolean tryToProcessEscapeSequence() {
        int length = escapeSequence.length();
        
        switch (length) {
            case 0: return false;
            case 1: return escapeSequence.charAt(0) != ESCAPE; // incomplete if ESCAPE, invalid otherwise
            case 2: return escapeSequence.charAt(1) != '[';    // incomplete if '[', invalid otherwise
        }
        
        // else length >= 3
        
        parameterStart = 2;
        char last = escapeSequence.charAt(escapeSequence.length() - 1);
        switch (last) {
            case ';': // fall through
            case '0': // fall through
            case '1': // fall through
            case '2': // fall through
            case '3': // fall through
            case '4': // fall through
            case '5': // fall through
            case '6': // fall through
            case '7': // fall through
            case '8': // fall through
            case '9': return false; // still reading parameters;
            
            case 'A': inEscape = false;  processCursorUp();               return true;
            case 'B': inEscape = false;  processCursorDown();             return true;
            case 'C': inEscape = false;  processCursorRight();            return true;
            case 'D': inEscape = false;  processCursorLeft();             return true;
            case 'H': inEscape = false;  processCursorPosition();         return true;
            case 's': inEscape = false;  processSaveCursorPosition();     return true;
            case 'u': inEscape = false;  processRestoreCursorPosition();  return true;
            case 'K': inEscape = false;  processClearToEnd();             return true;
            case 'J': inEscape = false;  processClearScreen();            return true;
            case 'm': inEscape = false;  processColor();                  return true;
            
            default: inEscape = false;  return true; // invalid escape
        }
    }

    
        
    
    private int getNextParameter(int defaultValue) {
        int length = escapeSequence.length();
        if (parameterStart < length - 1) {
            int end = escapeSequence.indexOf(";", parameterStart);
            if (end == -1)
                end = length - 1;
            try {
                return Integer.parseInt(escapeSequence.substring(parameterStart, end));
            }
            catch (NumberFormatException e) {
                return defaultValue;
            }
            finally {
                parameterStart = end + 1;
            }
        }
        else
            return defaultValue;
    }
    
    
    private void processCursorUp() {
        int count = getNextParameter(1);
        int cursorY = Math.max(textWindow.getCursorY() - count, 0);
        textWindow.setCursorPosition(textWindow.getCursorX(), cursorY);
    }

    
    private void processCursorDown() {
        int count = getNextParameter(1);
        int cursorY = Math.min(textWindow.getCursorY() + count, textWindow.getRows() - 1);
        textWindow.setCursorPosition(textWindow.getCursorX(), cursorY);
    }
    
    
    private void processCursorRight() {
        int count = getNextParameter(1);
        int cursorX = Math.min(textWindow.getCursorX() + count, textWindow.getColumns() - 1);
        textWindow.setCursorPosition(cursorX, textWindow.getCursorY());
    }
    
    
    private void processCursorLeft() {
        int count = getNextParameter(1);
        int cursorX = Math.max(textWindow.getCursorX() - count, 0);
        textWindow.setCursorPosition(cursorX, textWindow.getCursorY());
    }
    
    
    private void processCursorPosition() {
        int y = getNextParameter(-1) - 1; // parameters are 1-based rather than 0-based
        int x = getNextParameter(-1) - 1;
        if (x >= 0 && y >= 0) {
            x = Math.min(x, textWindow.getColumns() - 1);
            y = Math.min(y, textWindow.getRows() - 1);
            textWindow.setCursorPosition(x, y);
        }
    }
    
    
    private void processSaveCursorPosition() {
        savedX = textWindow.getCursorX();
        savedY = textWindow.getCursorY();
    }

    
    private void processRestoreCursorPosition() {
        savedX = Math.min(savedX, textWindow.getColumns() - 1);
        savedY = Math.min(savedY, textWindow.getRows() - 1);
        textWindow.setCursorPosition(savedX, savedY);
    }

    
    private void processClearToEnd() {
        int x = textWindow.getCursorX();
        int y = textWindow.getCursorY();
        if (x < textWindow.getColumns() - 1) {
            char[] buffer = new char[textWindow.getColumns() - x - 1];
            Arrays.fill(buffer, ' ');
            textWindow.output(buffer, 0, buffer.length);
        }
        textWindow.output(textWindow.getColumns() - 1, y, ' ');
        textWindow.setCursorPosition(x, y);
    }

    
    private void processClearScreen() {
        if (getNextParameter(-1) == 2) {
            // output spaces to clear the screen
            // start with one space less than the screen size, so as not to induce scrolling
            char[] buffer = new char[Math.max(0, textWindow.getColumns() * textWindow.getRows() - 1)];
            Arrays.fill(buffer, ' ');
            textWindow.setCursorPosition(0, 0);
            textWindow.output(buffer, 0, buffer.length);
            // this positional output does not cause scrolling
            textWindow.output(textWindow.getColumns() - 1, textWindow.getRows() - 1, ' ');
            // move cursor back to beginning
            textWindow.setCursorPosition(0, 0);
        }
    }
    
    
    private void processColor() {
        int parameter = getNextParameter(-1);
        while (parameter != -1) {
            try {
                switch (parameter) {
                    case 0: bright = false; foreground = 7; background = 0; break;
                    case 1: bright = true; break;
                    case 30: foreground =  0; break;
                    case 31: foreground =  1; break;
                    case 32: foreground =  2; break;
                    case 33: foreground =  3; break;
                    case 34: foreground =  4; break;
                    case 35: foreground =  5; break;
                    case 36: foreground =  6; break;
                    case 37: foreground =  7; break;
                    case 40: background =  0; break;
                    case 41: background =  1; break;
                    case 42: background =  2; break;
                    case 43: background =  3; break;
                    case 44: background =  4; break;
                    case 45: background =  5; break;
                    case 46: background =  6; break;
                    case 47: background =  7; break;
                }
            }
            catch (NumberFormatException e) { return; }
            parameter = getNextParameter(-1);
        }
        console.setTextAttributes(new TextAttributes(COLORS[foreground + (bright ? 8 : 0)],
                                             COLORS[background]));
    }
    
    
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    

    public void write(byte[] b, int off, int len) throws IOException {
        int index = off;
        int end = off + len;
        while (index < end) {
            if (!inEscape && b[index] == ESCAPE && (index == len - 1 || b[index + 1] == '[')) {
                inEscape = true; // we may or may not have seen the bracket yet
                escapeSequence.setLength(0);
            }
                
            if (inEscape)
                index += processEscape(b, index);
            else {
                int start = index;
                for (; index < len && b[index] != ESCAPE; index++); // advance to next ESCAPE (or end of string)
                super.write(b, start, index - start); // output characters.  escape will be processed on next iteration
            }
        }
    }
 
 
    public void write(int b) throws IOException {
        if (!inEscape && b != ESCAPE)
            super.write(b);
        else {
            byteHolder[0] = (byte) b;
            write(byteHolder);
        }
    }
}