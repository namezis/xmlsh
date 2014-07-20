
package org.xmlsh.core;

import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.regex.Pattern;

import javanet.staxutils.Indentation;
import javanet.staxutils.IndentingXMLEventWriter;
import javanet.staxutils.events.AbstractCharactersEvent;
import javanet.staxutils.helpers.EventWriterDelegate;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

/**
 * A filter that indents an XML stream. To apply it, construct a filter that
 * contains another {@link XMLEventWriter}, which you pass to the constructor.
 * Then call methods of the filter instead of the contained stream. For example:
 * 
 * <pre>
 * {@link XMLEventWriter} stream = ...
 * stream = new {@link IndentingXMLEventWriter}(stream);
 * stream.add(...);
 * </pre>
 * 
 * <p>
 * The filter inserts characters to format the document as an outline, with
 * nested elements indented. Basically, it inserts a line break and whitespace
 * before:
 * <ul>
 * <li>each DTD, processing instruction or comment that's not preceded by data</li>
 * <li>each starting tag that's not preceded by data</li>
 * <li>each ending tag that's preceded by nested elements but not data</li>
 * </ul>
 * This works well with 'data-oriented' XML, wherein each element contains
 * either data or nested elements but not both. It can work badly with other
 * styles of XML. For example, the data in a 'mixed content' document are apt to
 * be polluted with indentation characters.
 * <p>
 * Indentation can be adjusted by setting the newLine and indent properties. But
 * set them to whitespace only, for best results. Non-whitespace is apt to cause
 * problems, for example when this class attempts to insert newLine before the
 * root element.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke Kawaguchi</a>
 * @author <a href="mailto:jk2006@engineer.com">John Kristian</a>
 */
public class OmittingIndentingXMLEventWriter extends EventWriterDelegate implements Indentation {

	private boolean bOmitXml;
	private boolean bIndent;
	private OmittingOutputStream mOut;
    public OmittingIndentingXMLEventWriter(XMLEventWriter out, OutputStream os,boolean indent , boolean omit ) throws IOException {
        super(out);
        bOmitXml = omit;
        bIndent = indent ;
        mOut = ( omit && os instanceof OmittingOutputStream ) ? (OmittingOutputStream) os : null ;
    
        if(mOut != null ) {
        	mOut.flush();
        	mOut.setDiscard(true);
        }
    
    }

    /** How deeply nested the current scope is. The root element is depth 1. */
    private int depth = 0; // document scope

    /** stack[depth] indicates what's been written into the current scope. */
    private int[] stack = new int[] { 0, 0, 0, 0 }; // nothing written yet

    private static final int WROTE_MARKUP = 1;

    private static final int WROTE_DATA = 2;

    /** An object that produces a line break and indentation. */
    private final PrefixCharacters newLineEvent = new PrefixCharacters();

    public void setIndent(String indent) {
        newLineEvent.setIndent(indent);
    }

    public void setNewLine(String newLine) {
        newLineEvent.setNewLine(newLine);
    }

    public String getIndent() {
        return newLineEvent.getIndent();
    }

    public String getNewLine() {
        return newLineEvent.getNewLine();
    }

    /**
     * @return System.getProperty("line.separator"); or
     *         {@link #NORMAL_END_OF_LINE} if that fails.
     */
    public static String getLineSeparator() {
        return Util.getNewlineString();
    }

    public void add(XMLEvent event) throws XMLStreamException {
        switch (event.getEventType()) {
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
            case XMLStreamConstants.SPACE:
                out.add(event);
            	if( bIndent )
            		afterData();
                return;

            case XMLStreamConstants.START_ELEMENT:
            	if( bIndent )
            		beforeStartElement();
                out.add(event);
            	if( bIndent )
            		afterStartElement();
                return;

            case XMLStreamConstants.END_ELEMENT:
            	if( bIndent )
            		beforeEndElement();
                out.add(event);
            	if( bIndent )
            		afterEndElement();
                return;

            case XMLStreamConstants.START_DOCUMENT:
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
            case XMLStreamConstants.COMMENT:
            case XMLStreamConstants.DTD:

            	if( bIndent )
            		beforeMarkup();
                out.add(event);
                boolean discard =  mOut != null && mOut.getDiscard() ;
            	if(discard) {
            		out.flush();
            		mOut.setDiscard(false);
            	}
            	if( bIndent && ! discard )
            		afterMarkup();
                return;

            case XMLStreamConstants.END_DOCUMENT:
                out.add(event);
                
                // Writing text after END_DOCUMENT can cause an NPE 
                // afterEndDocument();
                break;

            default:
                out.add(event);
                return;
        }
    }

    /** Prepare to write markup, by writing a new line and indentation. */
    protected void beforeMarkup() {

        int soFar = stack[depth];
        if ((soFar & WROTE_DATA) == 0 // no data in this scope
                && (depth > 0 || soFar != 0)) // not the first line
        {
            try {
                newLineEvent.write(out, depth);
                if (depth > 0 && getIndent().length() > 0) {
                    afterMarkup(); // indentation was written
                }
            } catch (Exception e) {
            }
        }
    }

    /** Note that markup or indentation was written. */
    protected void afterMarkup() {
        stack[depth] |= WROTE_MARKUP;
    }

    /** Note that data were written. */
    protected void afterData() {
        stack[depth] |= WROTE_DATA;
    }

    /** Prepare to start an element, by allocating stack space. */
    protected void beforeStartElement() {
        beforeMarkup();
        if (stack.length <= depth + 1) {
            // Allocate more space for the stacks:
            int[] newWrote = new int[stack.length * 2];
            System.arraycopy(stack, 0, newWrote, 0, stack.length);
            stack = newWrote;
        }
        stack[depth + 1] = 0; // nothing written yet
    }

    /** Note that an element was started. */
    protected void afterStartElement() {
        afterMarkup();
        ++depth;
    }

    /** Prepare to end an element, by writing a new line and indentation. */
    protected void beforeEndElement() {
        if (depth > 0 && stack[depth] == WROTE_MARKUP) { // but not data
            try {
                newLineEvent.write(out, depth - 1);
            } catch (Exception ignored) {
            }
        }
    }

    /** Note that an element was ended. */
    protected void afterEndElement() {
        if (depth > 0) {
            --depth;
        }
    }

    /** Note that a document was ended. */
    protected void afterEndDocument() {
        depth = 0;
        if (stack[0] == WROTE_MARKUP) { // but not data
            try {
                newLineEvent.write(out, 0);
            } catch (Exception ignored) {
            }
        }
        stack[0] = 0; // start fresh
    }

    private static class PrefixCharacters extends AbstractCharactersEvent implements Indentation {

        PrefixCharacters() {
            super((String) null);
        }

        /** String used for indentation. */
        private String indent = DEFAULT_INDENT;

        /** String for EOL. */
        private String newLine = NORMAL_END_OF_LINE;

        /**
         * Various combinations of newLine and indent, used to begin and indent
         * a line. The appropriate prefix for a given depth is prefixes[depth %
         * prefixes.length]. This array is managed as a ring buffer, containing
         * the prefix for the current depth and recently used adjacent prefixes.
         * <p>
         * This structure uses memory proportional to the maximum depth.
         * Retaining prefixes for all depths would be faster, but require memory
         * proportional the square of the maximum depth.
         */
        private final String[] prefixes = new String[] { null, null, null, null, null, null };

        /**
         * The depth of the shortest String in prefixes (which is located at
         * prefixes[minimumPrefix % prefixes.length]).
         */
        private int minimumPrefix = 0;

        public String getIndent() {
            return indent;
        }

        public String getNewLine() {
            return newLine;
        }

        public void setIndent(String indent) {
            if (!indent.equals(this.indent)) {
                Arrays.fill(prefixes, null);
            }
            this.indent = indent;
        }

        public void setNewLine(String newLine) {
            if (!newLine.equals(this.newLine)) {
                Arrays.fill(prefixes, null);
            }
            this.newLine = newLine;
        }

        void write(XMLEventWriter out, int depth) throws XMLStreamException {
            this.depth = depth; // so getData knows what to do.
            out.add(this);
        }

        /** An implicit parameter to getData(). */
        private int depth = 0;

        public String getData() {
            while (depth >= minimumPrefix + prefixes.length) {
                prefixes[minimumPrefix++ % prefixes.length] = null;
            }
            while (depth < minimumPrefix) {
                prefixes[--minimumPrefix % prefixes.length] = null;
            }
            final int p = depth % prefixes.length;
            String data = prefixes[p];
            if (data == null) {
                StringBuffer b = new StringBuffer(newLine.length() + (indent.length() * depth));
                b.append(newLine);
                for (int d = 0; d < depth; ++d) {
                    b.append(indent);
                }
                data = prefixes[p] = b.toString();
            }
            return data;
        }

        public int getEventType() {
            // it's not clear if we are supposed to return SPACES
            return XMLStreamConstants.CHARACTERS;
        }

        public Characters asCharacters() {
            return this;
        }

        public boolean isCData() {
            return false;
        }

        public boolean isIgnorableWhiteSpace() {
            return isWhiteSpace();
        }

        private static final Pattern ENCODABLE = Pattern.compile("[&<>]");

        public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
            // Similar to super.writeAsEncodedUnicode;
            // but '\r' is not encoded.
            // TODO? memoize the encoded string.
            try {
                String s = getData();
                if (!ENCODABLE.matcher(s).find()) {
                    writer.write(s);
                } else {
                    final char[] data = s.toCharArray();
                    int first = 0;
                    for (int d = first; d < data.length; ++d) {
                        switch (data[d]) {
                            case '&':
                                writer.write(data, first, d - first);
                                writer.write("&amp;");
                                first = d + 1;
                                break;
                            case '<':
                                writer.write(data, first, d - first);
                                writer.write("&lt;");
                                first = d + 1;
                                break;
                            case '>':
                                writer.write(data, first, d - first);
                                writer.write("&gt;");
                                first = d + 1;
                                break;
                            default:
                        }
                    }
                    writer.write(data, first, data.length - first);
                }
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }

    }

}