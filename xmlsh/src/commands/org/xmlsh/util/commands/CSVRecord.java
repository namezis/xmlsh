package org.xmlsh.util.commands;

import java.util.List;

import org.xmlsh.util.Util;

/**
 * A single 'record' of CVS.
 * <br>
 * Implemented as String[]
 * 
 * @author David A. Lee
 * @version $Revision$
 */
public class CSVRecord
{
	private String[]    mFields;

	CSVRecord( String[] fields )
	{
		mFields = fields;
	}


	public CSVRecord(List<String> fields) {
		this( fields.toArray(new String[fields.size()]));
	}


	public String getField( int id )
	{
		return id < mFields.length ?
				Util.notNull(mFields[id]) : "" ; //$NON-NLS-1$
	}    

	public int getNumFields() {
		return mFields.length;
	}

	String[] getFields() { return mFields; }


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


