package textout;

import java.io.IOException;
import java.io.Writer;

public class TextWriter extends Writer {

    /* ===================================================================== */
    /* PRIVATE CONSTANTS                                                     */
    /* ===================================================================== */

    /** The minimum allowed width for each line. */
    private static final int WIDTH_MIN = 20;
    /** The maximum allowed width for each line. */
    private static final int WIDTH_MAX = 1024;
    /** The maximum allowed number of words per line. */
    private static final int WORDS_MAX = 1024;
    /** A constant empty character buffer. */
    private static final char EMPTY[] = new char[0];
    /** A constant empty String. */
    private static final String EMPTY_STRING = new String(EMPTY);
    /** A constant character buffer containing only spaces. */
    private static final char SPACE[] = new char[WIDTH_MAX];

    /** Static initializer */
    static {
        for (int x = 0; x < WIDTH_MAX; x++) SPACE[x] = ' ';
    }

    /* ===================================================================== */
    /* PUBLIC CONSTANTS                                                      */
    /* ===================================================================== */

    /** Specifies left alignment. */
    public static final int ALIGN_LEFT = 0;
    /** Specifies right alignment. */
    public static final int ALIGN_RIGHT = 1;
    /** Specifies center alignment. */
    public static final int ALIGN_CENTER = 2;
    /** Specifies justified alignment. */
    public static final int ALIGN_JUSTIFY = 3;

    /** The default indentation level for each line (0). */
    public static final int INDENT_DEFAULT = 0;
    /** The default right margin for each line (80). */
    public static final int MARGIN_DEFAULT = 80;
    /** Specifies the default alignment (left). */
    public static final int ALIGN_DEFAULT = ALIGN_LEFT;
    /** The default bullet string (empty string). */
    public static final String BULLET_DEFAULT = EMPTY_STRING;
    /** The default left quotation string (empty string). */
    public static final String LEFT_QUOTE_DEFAULT = EMPTY_STRING;
    /** The default inner quotation string (empty string). */
    public static final String INNER_QUOTE_DEFAULT = EMPTY_STRING;
    /** The default right quotation string (empty string). */
    public static final String RIGHT_QUOTE_DEFAULT = EMPTY_STRING;

    /* ===================================================================== */
    /* INSTANCE VARIABLES                                                    */
    /* ===================================================================== */

    /** The writer target of our output. */
    private Writer writer = null;

    /** The current indentation level for each line. */
    private int indent = INDENT_DEFAULT;
    /** The current right margin for each line. */
    private int margin = MARGIN_DEFAULT;
    /** The calculated line width. */
    private int width = (MARGIN_DEFAULT - INDENT_DEFAULT);

    /** The current word buffer. */
    private final char wordBuf[] = new char[WIDTH_MAX];
    /** The current line buffer. */
    private final char lineBuf[] = new char[WIDTH_MAX];
    /** The current position in the word buffer. */
    private int wordPos = 0;
    /** The current position in the line buffer. */
    private int linePos = 0;

    /** The array representing word offsets in the current line. */
    private final int wordOff[] = new int[WORDS_MAX];
    /** The array representing word lengths in the current line. */
    private final int wordLen[] = new int[WORDS_MAX];
    /** The array of number of spaces between words for justification. */
    private final int wordSpc[] = new int[WORDS_MAX];
    /** The number of words in the words arrays */
    private int wordNum = 0;

    /** The current bullet. */
    private char bullet[] = EMPTY;
    /** The current left quotation string. */
    private char leftQuote[] = EMPTY;
    /** The current inner quotation string. */
    private char innerQuote[] = EMPTY;
    /** The current right quotation string. */
    private char rightQuote[] = EMPTY;

    /** The current alignment type. */
    private int align = ALIGN_DEFAULT;

    /** True if the current line is the first line of a block. */
    private boolean divided = true;
    /** True if the current line is the first line of a paragraph. */
    private boolean separated = true;
    /** The current line separator string. */
    private char separator[] = null;

    /* ===================================================================== */
    /* CONSTRUCTORS                                                          */
    /* ===================================================================== */

    /**
     * Create a new <code>TextWriter</code> instance writing the
     * formatted text to the specified <code>Writer</code>.
     *
     * @param writer The <code>Writer</code> used for output.
     *
     * @throws NullPointerException If the specified writer was <b>null</b>.
     */
    public TextWriter(Writer writer)
    throws NullPointerException {
        super();
        if (writer == null) throw new NullPointerException();
        setLineSeparator(null);
        this.writer = writer;
    }

    /* ===================================================================== */
    /* PUBLIC METHODS                                                        */
    /* ===================================================================== */

    /**
     * Close this <code>TextWriter</code>.
     * <br>
     * This method will also close the <code>Writer</code> specified at
     * construction. To alter this behavior call <code>close(false)</code>.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close()
    throws IOException {
        this.close(true);
    }

    /**
     * Close this <code>TextWriter</code>.
     *
     * @param flag Whether to close the underlying writer specified at
     *             construction or not.
     * @throws IOException If an I/O error occurs.
     */
    public void close(boolean flag)
    throws IOException {
        if (writer == null) throw new IOException("Writer closed");
        flush();
        if (flag) writer.close();
        writer = null;
    }

    /**
     * Flush the buffers of this <code>TextWriter</code>.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void flush()
    throws IOException {
        if (writer == null) throw new IOException("Writer closed");
        divide();
        writer.flush();
    }

    /**
     * Write a single character.
     * <br>
     * The character to be written is contained in the 16 low-order bits of
     * the given integer value; the 16 high-order bits are ignored.
     *
     * @param c The character to be written.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(int c)
    throws IOException {
        if (writer == null) throw new IOException("Writer closed");

        if (Character.isWhitespace((char)c)) {
            flushWord();
        } else {
            wordBuf[wordPos ++] = (char) c;
        }
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param buf The character array containing the portion to be written.
     * @param off The offset from which to start writing characters.
     * @param len The number of characters to write.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(char[] buf, int off, int len)
    throws IOException {
        if (writer == null) throw new IOException("Writer closed");

        if (buf == null) throw new NullPointerException();

        if ((off < 0) || (off > buf.length) || (len < 0) ||
                ((off + len) > buf.length) || ((off + len) < 0))
            throw new IndexOutOfBoundsException("[" + new String(buf,off,len) +
            "] OFF=" + off + " LEN=" + len);

        if (len == 0) return;

        for (int x = 0 ; x < len ; x++) write(buf[off + x]);
    }

    /**
     * Fill a single line with the specified character.
     *
     * @param c The character to be written on one entire line.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void fill(char c)
    throws IOException {
        divide();
        for (int x = 0; x < width; x++)
            lineBuf[linePos ++] = c;
        divide();
    }

    /**
     * Break the current line.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void divide()
    throws IOException {
        if (wordPos > 0) flushWord();
        if (linePos > 0) flushLine(false);
        if (divided) return;
        divided = true;
    }

    /**
     * Break the current line and start a new paragraph after an empty line.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void separate()
    throws IOException {
        divide();
        if (separated) return;

        writer.write(SPACE, 0, indent);
        writer.write(leftQuote);

        if ((innerQuote.length + rightQuote.length) > 0) {
            writer.write(SPACE, 0, bullet.length);
            writer.write(innerQuote);

            if (rightQuote.length > 0) {
                writer.write(SPACE, 0, width);
                writer.write(rightQuote);
            }
        }
        writer.write(separator);
        separated = true;
    }

    /* ===================================================================== */
    /* BEAN METHODS                                                          */
    /* ===================================================================== */

    public void setIndent(int indent)
    throws IllegalArgumentException {
        width = check(indent, margin, null, null, null, null);
        this.indent = indent;
    }

    public int getIndent() {
        return (indent);
    }

    public void setMargin(int margin)
    throws IllegalArgumentException {
        width = check(indent, margin, null, null, null, null);
        this.margin = margin;
    }

    public int getMargin() {
        return (margin);
    }

    public void setBullet(String bullet)
    throws IllegalArgumentException {
        char array[] = (bullet == null)? EMPTY: bullet.toCharArray();
        width = check(indent, margin, array, null, null, null);
        this.bullet = array;
    }

    public String getBullet() {
        return (new String(bullet));
    }

    public void setLeftQuote(String quote)
    throws IllegalArgumentException {
        char array[] = (quote == null)? EMPTY: quote.toCharArray();
        width = check(indent, margin, null, array, null, null);
        leftQuote = array;
    }

    public String getLeftQuote() {
        return (new String(leftQuote));
    }

    public void setInnerQuote(String quote)
    throws IllegalArgumentException {
        char array[] = (quote == null)? EMPTY: quote.toCharArray();
        width = check(indent, margin, null, null, array, null);
        innerQuote = array;
    }

    public String getInnerQuote() {
        return (new String(innerQuote));
    }

    public void setRightQuote(String quote)
    throws IllegalArgumentException {
        char array[] = (quote == null)? EMPTY: quote.toCharArray();
        width = check(indent, margin, null, null, null, array);
        rightQuote = array;
    }

    public String getRightQuote() {
        return (new String(rightQuote));
    }

    public void setAlignment(int align)
    throws IllegalArgumentException {
        switch (align) {
            case ALIGN_LEFT:
            case ALIGN_RIGHT:
            case ALIGN_CENTER:
            case ALIGN_JUSTIFY:
                this.align = align;
                break;
            default:
                throw new IllegalArgumentException ("Invalid value specified" +
                    " for text alignment: " + align);
        }
    }

    public int getAlignment() {
        return (align);
    }

    public void setLineSeparator(String sep)
    throws IllegalArgumentException {
        if (sep == null) sep = System.getProperty("line.separator");
        separator = sep.toCharArray();
    }

    public String getLineSeparator() {
        return (new String(separator));
    }

    /* ===================================================================== */
    /* PRIVATE METHODS                                                       */
    /* ===================================================================== */

    /** Check bean values before committing them and return line width. */
    private int check(int ind, int mar, char b[], char l[], char i[], char r[])
    throws IllegalArgumentException {
        int bul = (b == null)? bullet.length: b.length;
        int lef = (l == null)? leftQuote.length: l.length;
        int inn = (i == null)? innerQuote.length: i.length;
        int rig = (r == null)? rightQuote.length: r.length;

        int width = 0;

        if (ind < 0)
            throw new IllegalArgumentException ("Invalid indent");
        if (mar < ind)
            throw new IllegalArgumentException("Invalid margin");

        width = mar - (ind + bul + lef + inn + rig);

        if (width < WIDTH_MIN)
            throw new IllegalArgumentException("Line too short");
        if (width > WIDTH_MAX)
            throw new IllegalArgumentException("Line too long");

        return(width);
    }

    /** Flush the current word to the line buffer. */
    private void flushWord()
    throws IOException {
        if (wordPos > 0) {
            if ((wordPos + 1) > (width - linePos)) {
                flushLine(true);
            }

            if (linePos > 0) lineBuf[linePos ++] = ' ';
            System.arraycopy(wordBuf, 0,
                    lineBuf, linePos, wordPos);

            wordOff[wordNum] = linePos;
            wordLen[wordNum] = wordPos;
            wordSpc[wordNum] = 1;
            wordNum ++;

            linePos += wordPos;
            wordPos = 0;
        }
    }

    /** Flush the current line to the output writer. */
    private void flushLine(boolean allowJustification)
    throws IOException {
        if (linePos > 0) {
            int remaining = width - linePos;
            if (remaining < 0) remaining = 0;

            writer.write(SPACE, 0, indent);
            writer.write(leftQuote);

            if (divided) writer.write(bullet);
            else writer.write(SPACE, 0, bullet.length);

            writer.write(innerQuote);

            if (align == ALIGN_RIGHT) {
                writer.write(SPACE, 0, remaining);
            } else if (align == ALIGN_CENTER) {
                writer.write(SPACE, 0, remaining / 2);
            }

            if ((align == ALIGN_JUSTIFY) && allowJustification) {
                if (remaining > 0) {
                    remaining = justify(remaining, wordNum - 1);
                } else {
                    writer.write(lineBuf, 0, linePos);
                }
            } else {
                writer.write(lineBuf, 0, linePos);
            }

            if (rightQuote.length > 0) {
                if ((align == ALIGN_LEFT) || (align == ALIGN_JUSTIFY)) {
                    try {
                    writer.write(SPACE, 0, remaining);
                    } catch (RuntimeException t) {
                        System.err.println("REMAINING=" + remaining);
                        throw(t);
                    }
                } else if (align == ALIGN_CENTER) {
                    writer.write(SPACE, 0, remaining / 2 + remaining % 2);
                }

                writer.write(rightQuote);
            }

            writer.write(separator);

            wordNum = 0;
            linePos = 0;
            separated = false;
            divided = false;
        }
    }

    /** Flush the the current line justifying it */
    private int justify(int remaining, int spaces)
    throws IOException {
        /* Check that we have at least one word in our buffer */
        if (spaces == 0) {
            writer.write(lineBuf, 0, linePos);
            return (remaining);
        }

        if (remaining >= spaces) {
            int add = remaining / spaces;
            int rem = remaining % spaces;
            for (int x = 0; x < spaces; x++) wordSpc[x] += add;
            return (justify(rem, spaces));
        }

        if (remaining == 0) {
            for (int x = 0; x < spaces; x++) {
                writer.write(lineBuf, wordOff[x], wordLen[x]);
                writer.write(SPACE, 0, wordSpc[x]);
            }
            writer.write(lineBuf, wordOff[spaces],
                wordLen[spaces]);

            writer.flush();
            return(0);
        }

        int every = (spaces / remaining) + 1;
        for (int x = 0; x < spaces; x += every) {
            wordSpc[x] += 1;
            remaining --;
        }
        System.err.flush();

        return (justify(remaining, spaces));
    }

    /* ===================================================================== */
    /*                                                                       */
    /* ===================================================================== */

    public static void main(String argv[])
    throws IOException {
        try {
            Writer out = new java.io.OutputStreamWriter(System.out);
            TextWriter tex = new TextWriter(out);
            java.io.Reader in = new java.io.InputStreamReader(System.in);
            tex.setIndent(10);
            tex.setMargin(70);
            tex.setBullet("BULLET: ");
            tex.setLeftQuote("> ");
            tex.setInnerQuote(" | ");
            tex.setRightQuote(" <");
            tex.setAlignment(ALIGN_JUSTIFY);

            int c = -1;
            int k = 0;
            long start = System.currentTimeMillis();
            while ((c = in.read()) != -1) {
                tex.write(c);
                if ((k % 2000) == 0) tex.divide();
                if ((k % 5000) == 0) tex.separate();
                k++;
            }
            tex.flush();
            System.err.println("Total: " + (System.currentTimeMillis() - start));
            tex.close();
        } catch (Throwable e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
