/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.cloudformation.model.TemplateParameter;

public abstract class AWSCFNCommand extends AWSCommand<AmazonCloudFormationClient> {
	
	
	public AWSCFNCommand() {
		super();
	}

	protected void getCFNClient( Options opts ) throws UnexpectedException, InvalidArgumentException {
		setAmazon(AWSClientFactory.newCFNClient(mShell, opts));

	}



	protected void writeNotifications(List<String> notificationARNs) throws XMLStreamException {
		writeStringList("notification-arns", "notification-arn", "arn" , notificationARNs);		
	}


	protected void writeCapibilities(List<String> capabilities) throws XMLStreamException {
	
		writeStringList("capabilities", "capability", "name" , capabilities);
		
		
	}


	protected void writeParameters(List<Parameter> parameters) throws XMLStreamException {
		startElement("parameters");
		for( Parameter p : parameters )
			writeParameter( p );
		endElement();
	}


	public void writeParameter(Parameter p) throws XMLStreamException {
		startElement("parameter");
		attribute("key",p.getParameterKey());
		attribute("value",p.getParameterValue());
		endElement();
		
	}


	protected void writeTemplateParameters(List<TemplateParameter> parameters) throws XMLStreamException {
		startElement("parameters");
		for( TemplateParameter p : parameters )
			writeTemplateParameter( p );
		endElement();
	}


	public void writeTemplateParameter(TemplateParameter p) throws XMLStreamException {
		startElement("parameter");
		attribute("key",p.getParameterKey());
		attribute("description",p.getDescription());
		attribute("no-echo",p.getNoEcho());
		attribute("default",p.getDefaultValue());
		endElement();
		
	}


  protected void writeOutputs(List<Output> outputs) throws XMLStreamException {
  	startElement("outputs")	;
  	for( Output o : outputs )
  		writeOutput( o );
  	endElement();
  
  }


  private void writeOutput(Output o) throws XMLStreamException {
  	startElement("output")	;
  	attribute("description" ,o.getDescription());
  	attribute("output-key" ,o.getOutputKey());
  	attribute("output-value" ,o.getOutputValue());
  	endElement();
  
  }
  protected void writeTags(List<Tag> tags) throws XMLStreamException {
    if( tags == null )
      return ;      
    startElement("tags");
          for( Tag t : tags )
            writeTag( t );
          endElement();
    
  }




  public void writeTag(Tag t) throws XMLStreamException {
    startElement("tag");
    attribute( "key" , t.getKey());
    attribute("value", t.getValue());
    endElement();
    
	}



}

//
//
// Copyright (C) 2008-2014    David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
