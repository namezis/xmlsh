/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.util.IReferenceCounted;
import org.xmlsh.util.ReferenceCounter;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;


public abstract class AbstractPort implements  Closeable , Flushable, IPort {
	private	ReferenceCounter mCounter = new ReferenceCounter();
	private String mSystemId = "";
	private boolean    mSystem;    // System port from original env

	protected void setSystem( boolean system ){
		mSystem = system ;
	}
	
	public String getSystemId() {
		// TODO Auto-generated method stub
		return mSystemId;
	}

	public void setSystemId(String systemId)
	{
		mSystemId = systemId;
	}
	public void addRef() {
		mCounter.addRef();
	}

	
	public final boolean release() throws IOException 
	{		
			if(mCounter.release() ) {
				    flush();
					close();
					return true ;
			}
			return false ;
	}
	
	
	public	boolean	  isFile() { return false ; }
	
	public File		getFile() throws UnimplementedException
	{
		throw new UnimplementedException("IPort.getFile() is not implmented() in class: " + this.getClass().getName() );
	}
	
	public boolean isSystem(){
		return mSystem;
	}

	

}



//
//
//Copyright (C) 2008-2014    David A. Lee.
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
