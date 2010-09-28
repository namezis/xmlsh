/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javanet.staxutils.IndentingXMLEventWriter;
import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.event.XMLEmitter;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.SynchronizedOutputStream;
import org.xmlsh.util.Util;

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */




public class StreamOutputPort extends OutputPort
{
	
	
	
	
	
	

	private OutputStream	 mStream;
	private boolean mClose = true ;

	public StreamOutputPort( OutputStream os , boolean bClose ) 
	{
		mStream = os;
		mClose = bClose;
	}

	public StreamOutputPort( OutputStream os ) 
	{
		mStream = os;
	}

	
	
	
	/*
	 * Standard input stream - created on first request
	 */
	
	public	synchronized OutputStream asOutputStream(SerializeOpts opts) 
	{
		return new SynchronizedOutputStream(mStream,mStream != System.out);
	}

	public synchronized void flush() throws CoreException
	{
		if( mStream != null )
			try {
				mStream.flush();
			} catch (IOException e) {
				throw new CoreException(e);
			}
		
	
	}
	
	
	
	public synchronized void close() throws CoreException {
		if( mClose && mStream != null )
			try {
				mStream.close();
			} catch (IOException e) {
				throw new CoreException(e);
			}
	}



	
	public synchronized PrintStream asPrintStream(SerializeOpts opts)
	{
		return new PrintStream(asOutputStream(opts));
	}

	public synchronized Destination asDestination(SerializeOpts opts) throws CoreException
	{

		return Util.streamToDestination(asOutputStream(opts), opts);
	}
	

	
	

	public synchronized PrintWriter asPrintWriter(SerializeOpts opts) throws UnsupportedEncodingException {
		return new PrintWriter( 		
				new OutputStreamWriter(asOutputStream(opts) , 
						opts.getText_encoding() ));
	}


	
	
	public synchronized void writeSequenceSeperator(SerializeOpts opts ) throws IOException, InvalidArgumentException
	{
		
		asOutputStream(opts).write( opts.getSequence_sep().getBytes( opts.getText_encoding()) );
		
		
	}

	public void writeSequenceTerminator(SerializeOpts opts) throws IOException {
			asOutputStream(opts).write(  opts.getSequence_term().getBytes(opts.getText_encoding()) );
		
	}




	@Override
	public XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws XMLStreamException {
		/*
	    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		*/
		XMLOutputFactory fact = XMLOutputFactory.newInstance();
	// XMLOutputFactory fact = new OutputFactory();
		XMLStreamWriter writer =  fact.createXMLStreamWriter(asOutputStream(opts), opts.getEncoding() );
	
		if( opts.isIndent() )
			writer = new IndentingXMLStreamWriter(writer);
		if( opts.isOmit_xml_declaration() )
			writer = new OmittingXMLStreamWriter( writer );
		
		return writer ;
		
		
	}




	@Override
	public XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws InvalidArgumentException, XMLStreamException {
		XMLOutputFactory fact = XMLOutputFactory.newInstance();
		
		
		// XMLOutputFactory fact = new OutputFactory();
		XMLEventWriter writer =  fact.createXMLEventWriter(asOutputStream(opts), opts.getEncoding() );
		
			if( opts.isIndent() )
				writer = new IndentingXMLEventWriter(writer);
		
			if( opts.isOmit_xml_declaration() )
				writer = new OmittingXMLEventWriter( writer );
		
		return writer ;
			
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.OutputPort#asXdmItemOutputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public IXdmValueOutputStream asXdmValueOutputStream(SerializeOpts opts) throws CoreException {
		
		return new DestinationXdmValueOutputStream( asDestination(opts) );
		
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.OutputPort#asContentHandler(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public ContentHandler asContentHandler(SerializeOpts opts) throws XPathException {
	
		ReceivingContentHandler  handler = new ReceivingContentHandler();
		Configuration config = Shell.getProcessor().getUnderlyingConfiguration();
	    PipelineConfiguration pipe = config.makePipelineConfiguration();
		handler.setPipelineConfiguration(pipe);

		
	   XMLEmitter emitter = new XMLEmitter();
       emitter.setPipelineConfiguration(pipe);
       emitter.setOutputProperties(new Properties());
       emitter.setOutputStream( asOutputStream(opts));
       
       handler.setReceiver(emitter);
       return handler;
		
	}
	
	
	
	

}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
