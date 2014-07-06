/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.builtin;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;

public class eval extends BuiltinCommand {

	/*
	 * Evaluate all arguments as as string
	 * and parse them as a command 
	 * 
	 */
	
	public int run(  List<XValue> args ) throws Exception {
			StringBuffer sb = new StringBuffer();
			for( XValue arg : args ){
				if( sb.length() > 0 )
					sb.append(' ');
				sb.append( arg.toString());
				
				
			}
			Command c = mShell.parseEval(sb.toString());
			if( c == null )
				return 0;
			
			int ret = mShell.exec(c);
			return ret ;
			
			
				
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
