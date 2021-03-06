/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.types;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

/*
 * Generic methods available on typed objects
 */
public interface IMethods
{
	public XValue append(Object value, XValue v) throws InvalidArgumentException;
	public String asString( Object obj );
	public int    getSize( Object obj ) throws InvalidArgumentException;
	public XValue getXValue( Object obj ) throws InvalidArgumentException;
  	public XValue getXValue(Object obj, String ind) throws CoreException;
    public XValue getXValue(Object obj, int index) throws CoreException;
	public XValue setXValue(XValue obj , String ind , XValue value ) throws CoreException;
    public XValue setXValue(XValue obj, int index, XValue value) throws CoreException;

	public void   serialize( Object obj , OutputStream os , SerializeOpts opts) throws IOException, InvalidArgumentException ;
	public String  simpleTypeName(Object obj);
	public String  typeName(Object obj);     // specific type name 
    public boolean isEmpty(Object obj) ;
	public List<XValue>  getXValues(Object obj) throws InvalidArgumentException;    // Get all contents 
	public boolean isAtomic(Object obj) ;
	public boolean isContainer( Object obj );
    public boolean hasKey(Object obj , String key ) throws InvalidArgumentException;
    

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */