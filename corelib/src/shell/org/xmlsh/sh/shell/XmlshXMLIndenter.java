/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.serialize.XMLIndenter;
import net.sf.saxon.trans.XPathException;

public class XmlshXMLIndenter extends XMLIndenter {

  public XmlshXMLIndenter(XMLEmitter next) {
    super(next);
    // TODO Auto-generated constructor stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.sf.saxon.serialize.XMLIndenter#endDocument()
   */
  @Override
  public void endDocument() throws XPathException {
    nextReceiver.endDocument();
  }

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
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
