/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import net.sf.saxon.Configuration;
import net.sf.saxon.pull.StaxBridge;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import net.sf.saxon.trans.XPathException;
import org.xml.sax.InputSource;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.databind.JsonNode;

public class XdmStreamInputPort extends InputPort {


	IXdmItemReader  mReader;

	public XdmStreamInputPort(IXdmItemReader reader, SerializeOpts opts) {
		mReader = reader ;
	}

	@Override
	public InputStream asInputStream(SerializeOpts opts) throws CoreException, IOException {

		XdmValue value = mReader.read();
		if( value == null )
			return new NullInputStream();


		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			Util.writeXdmValue(value, Util
					.streamToDestination(buf, opts)); // uses output xml encoding

			return new ByteArrayInputStream(buf.toByteArray());
		} catch (SaxonApiException e) {
			throw new CoreException(e);
		} 
	}

	@Override
	public void close() {


	}

	@Override
	public Source asSource(SerializeOpts opts) throws CoreException {
		return XValue.newXValue(mReader.read()).asSource();
	}

	@Override
	public InputSource asInputSource(SerializeOpts opts) throws CoreException, IOException {
		InputSource in = new InputSource(asInputStream(opts));
		in.setSystemId(getSystemId());
		return in;
	}

	@Override
	public XdmNode asXdmNode(SerializeOpts opts) throws CoreException {
		return  (XdmNode) mReader.read() ;
	}

	@Override
	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException, IOException {
		XdmItem item ;
		while( ( item = mReader.read()) != null )
			try {
				Util.writeXdmValue(item, Util.streamToDestination(out, opts));
			} catch (SaxonApiException e) {
				throw new CoreException(e);
			}


	}

	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException {
		try {
			return XMLInputFactory.newInstance().createXMLEventReader(asXMLStreamReader(opts));
		} catch (Exception e){
			throw new CoreException(e);
		}
	}

	@Override
	public XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws CoreException, IOException {
    Configuration config = Shell.getProcessor().getUnderlyingConfiguration();

    try {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      if( ! opts.isSupports_dtd())
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, "false");
      XMLStreamReader reader =  factory.createXMLStreamReader(getSystemId() , asInputStream(opts));
      return reader;
    } catch (Exception e)
    {
      throw new CoreException( e );
    }

  /* basic
    XMLInputFactory factory = XMLInputFactory.newInstance();
    //XMLInputFactory factory = new WstxInputFactory();
    factory.setXMLReporter(new StaxBridge.StaxErrorReporter());
    reader = factory.createXMLStreamReader(systemId, inputStream);

    */
    // lazy
    /*
    StaxBridge ps = new StaxBridge();
    ps.setPipelineConfiguration(config.makePipelineConfiguration() );
    try {
      ps.setInputStream(  this.getSystemId(), this.asInputStream(opts)   );

    }
    catch(XPathException e) {
      throw new CoreException(e);
    }
    return ps.getXMLStreamReader();
    */
    // TODO: This code was copied from VariableInputPort
/*
		XValue value = XValue.newXValue(mReader.read());

		//System.err.println("sysid: " + this.getSystemId() );
		//System.err.println("base: " + value.asXdmNode().getBaseURI());

		Configuration config = Shell.getProcessor().getUnderlyingConfiguration();

		/*
		 * IF variable is an atomic value then treat as string and parse to XML
		 *  **  /

		if( value.isAtomic() ){

			try {
				XMLInputFactory factory = XMLInputFactory.newInstance();
				if( ! opts.isSupports_dtd())
					factory.setProperty(XMLInputFactory.SUPPORT_DTD, "false");
				XMLStreamReader reader =  factory.createXMLStreamReader(getSystemId() , asInputStream(opts));
				return reader;
			} catch (Exception e)
			{
				throw new CoreException( e );
			}


		}



		// SequenceIterator iter = value.asSequenceIterator();
		NodeInfo nodeInfo = value.asNodeInfo();

		/*
		 * 2010-05-19 - EventReaders assume documents, if not a document then wrap with one
		 *   /
		if( nodeInfo.getNodeKind() != net.sf.saxon.type.Type.DOCUMENT )
			nodeInfo = S9Util.wrapDocument( nodeInfo ).getUnderlyingNode(); ;




	//		Decomposer decomposed = new Decomposer( nodeInfo , config.makePipelineConfiguration()  );

			// EventIteratorOverSequence eviter = new EventIteratorOverSequence(iter);


			StaxBridge ps = new StaxBridge();
			ps.setPipelineConfiguration(config.makePipelineConfiguration() );
    ps.setInputStream(   asInputStream(opts) );


			// TODO: Bug in Saxon 9.1.0.6 
			// PullToStax starts in state 0 not state START_DOCUMENT
			if( ps.current() == 0 )
				try {
					ps.next();
				} catch (Exception e) {
					throw new CoreException(e);
				}


			return ps;
			*/

	}

	@Override
	public XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException {
		return mReader.read();
	}

	// Default implementation uses a singleton as the input stream
	@Override
	public IXdmItemInputStream asXdmItemInputStream(SerializeOpts serializeOpts)
			throws CoreException {

		return mReader ;
	}

	@Override
	public JsonNode asJson(SerializeOpts serializeOpts) throws IOException, CoreException {
		return JSONUtils.readJsonNode( asInputStream(serializeOpts));
	}

	@Override
	public boolean isFile() {
		return false;
	}


}



//
//
//Copyright (C) 2008-2014   David A. Lee.
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
