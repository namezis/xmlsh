/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.json;

import java.io.IOException;
import java.util.List;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class convert extends AbstractBuiltinFunction {

	public convert()
	{
		super("convert");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws CoreException
	{
		requires( args.size() == 2 , " two arguments required");

		ClassLoader cl = shell.getClassLoader(); // TBD
		Class<?> cls = null ;
		Object from = args.get(0).asObject();
		cls = JavaUtils.convertToClass(args.get(1) , cl );
		if( cls == null )
			cls = JSONUtils.jsonNodeClass();

		ObjectMapper mapper = JSONUtils.getJsonObjectMapper();
		Object value = mapper.convertValue(from, cls);
		return XValue.newXValue( TypeFamily.XTYPE , value );
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
