/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.stax.functions;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

public class newStreamWriter extends AbstractBuiltinFunction {



	public newStreamWriter()
	{
		super("newEventWriter");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws CoreException, XMLStreamException, SaxonApiException, IOException {
		if( args.size() == 0 )
			return XValue.newXValue(TypeFamily.JAVA, shell.getEnv().getStdout().asXMLStreamWriter(shell.getSerializeOpts()));
		else
			return XValue.newXValue(TypeFamily.JAVA, shell.getEnv().getOutput(args.get(0),false).asXMLStreamWriter(shell.getSerializeOpts()));



	}

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
