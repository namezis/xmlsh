/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin.commands;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IFS;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class read extends BuiltinCommand
{

	/*
	 * Read a line of text from stdin and assign to variables
	 */

	@Override
	public int run(List<XValue> args) throws Exception
	{
		// Unset all args
		for (XValue arg : args)
			mShell.getEnv().unsetVar(arg.toString());

		InputPort stdin = mShell.getEnv().getStdin();
		try (InputStream is = stdin.asInputStream(getSerializeOpts())) {
			String line = null;
			try {
				line = Util.readLine(is, getSerializeOpts().getInputTextEncoding());
			} catch (IOException e) 
			{
				mLogger.debug("Caught IOException in read - treating as EOF" , e );
				line = null ;
			}
			if(line == null)
				return 1; // EOF

			IFS ifs = mShell.getIFS();

			List<String> results = ifs.split(line);
			int i;
			for (i = 0; i < args.size() - 1; i++)
				if(i < results.size())
					mShell.getEnv().setVar(args.get(i).toString(), XValue.asXValue(results.get(i)));

			// last var
			if(i < args.size() && i < results.size()) {
				// 1 left
				if(results.size() == 1)
					mShell.getEnv().setVar(args.get(i).toString(), XValue.asXValue(results.get(i)));
				else {
					int n = results.size() - i;
					String[] remaining = results.subList(i, results.size()).toArray(new String[n]);
					mShell.getEnv().setVar(args.get(i).toString(), XValue.asXValue(remaining));
				}

			}
		}

		return 0;
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
