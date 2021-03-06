/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.xproc.util.XProcException;
/*
 <p:inline
  exclude-inline-prefixes? = prefix list>
    anyElement
 </p:inline>

 */
class Inline extends Binding {

	String[]		exclude_inline_prefixes;
	XdmNode			node;
	
	
	@Override
	void parse(XdmNode node) {
		exclude_inline_prefixes = XProcUtil.getAttrList(node, "exclude-inline-prefixes");
		this.node = XProcUtil.getFirstChild(node);
		
	}


	@Override
	void serialize(OutputContext c) throws XProcException {
		c.addBodyLine("<<EOF");
		try {
			c.addBody(XProcUtil.serialize(node));
		} catch (XPathException e) {
			throw new XProcException(e);
		}
		
		c.addBodyLine("");
		c.addBodyLine("EOF");

		
	}
	/* (non-Javadoc)
	 * @see org.xmlsh.xproc.compiler.Binding#isInput()
	 */
	@Override
	boolean isInput() {
		
		return true;
	}
	
}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
