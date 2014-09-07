/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.colon;
import org.xmlsh.builtin.commands.declare;
import org.xmlsh.builtin.commands.echo;
import org.xmlsh.builtin.commands.eval;
import org.xmlsh.builtin.commands.exit;
import org.xmlsh.builtin.commands.jobs;
import org.xmlsh.builtin.commands.log;
import org.xmlsh.builtin.commands.printvar;
import org.xmlsh.builtin.commands.read;
import org.xmlsh.builtin.commands.require;
import org.xmlsh.builtin.commands.set;
import org.xmlsh.builtin.commands.shift;
import org.xmlsh.builtin.commands.source;
import org.xmlsh.builtin.commands.test;
import org.xmlsh.builtin.commands.trap;
import org.xmlsh.builtin.commands.unset;
import org.xmlsh.builtin.commands.wait;
import org.xmlsh.builtin.commands.xbreak;
import org.xmlsh.builtin.commands.xcd;
import org.xmlsh.builtin.commands.xcontinue;
import org.xmlsh.builtin.commands.xecho;
import org.xmlsh.builtin.commands.xfalse;
import org.xmlsh.builtin.commands.ximport;
import org.xmlsh.builtin.commands.xmkpipe;
import org.xmlsh.builtin.commands.xmlsh;
import org.xmlsh.builtin.commands.xmlshui;
import org.xmlsh.builtin.commands.xread;
import org.xmlsh.builtin.commands.xthrow;
import org.xmlsh.builtin.commands.xtrue;
import org.xmlsh.builtin.commands.xtype;
import org.xmlsh.builtin.commands.xversion;
import org.xmlsh.builtin.commands.xwhich;
import org.xmlsh.java.commands.jset;
import org.xmlsh.json.commands.jsonread;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.Modules;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.text.commands.readconfig;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class CommandFactory 
{
	public static final String kCOMMANDS_HELP_XML = "/org/xmlsh/resources/help/commands.xml";
	public static final String kFUNCTIONS_HELP_XML = "/org/xmlsh/resources/help/functions.xml";

	private static Logger mLogger =  org.apache.logging.log4j.LogManager.getLogger( CommandFactory.class);
	private static CommandFactory _instance = null ;

	private HashMap<String,Class<? extends ICommand>>		mBuiltinCommands = new HashMap<String,Class<? extends ICommand>>();

	private void addBuiltinCommand( String name ,   Class<? extends ICommand>  cls )
	{
		mBuiltinCommands.put( name , cls);
	}

	private CommandFactory()
	{
		addBuiltinCommand(  "cd" , xcd.class );
		addBuiltinCommand( "xecho" , xecho.class );
		addBuiltinCommand( "echo" , echo.class );
		addBuiltinCommand( "false" , xfalse.class );
		addBuiltinCommand( "true" , xtrue.class  );
		addBuiltinCommand( "set", set.class);
		addBuiltinCommand( "." , source.class);
		addBuiltinCommand( "source" , source.class);
		addBuiltinCommand("exit" , exit.class);
		addBuiltinCommand( ":" , colon.class);
		addBuiltinCommand( "[" , test.class );
		addBuiltinCommand( "test" , test.class );
		addBuiltinCommand( "shift" , shift.class );
		addBuiltinCommand( "read" , read.class);
		addBuiltinCommand( "xread" , xread.class);
		addBuiltinCommand( "unset" , unset.class );
		addBuiltinCommand( "xwhich" , xwhich.class );
		addBuiltinCommand( "xversion" , xversion.class);
		addBuiltinCommand("jobs" , jobs.class);
		addBuiltinCommand("wait" , wait.class);
		addBuiltinCommand("break" , xbreak.class);
		addBuiltinCommand("continue", xcontinue.class );
		addBuiltinCommand("eval", eval.class);
		addBuiltinCommand("declare" , declare.class);
		addBuiltinCommand("import" , ximport.class);
		addBuiltinCommand("xmlsh" , xmlsh.class);
		addBuiltinCommand("throw" , xthrow.class);
		addBuiltinCommand("log",log.class);
		addBuiltinCommand("xtype", xtype.class);
		addBuiltinCommand("require", require.class);
		addBuiltinCommand("jset" , jset.class );
		addBuiltinCommand("xmlshui" , xmlshui.class);
		addBuiltinCommand("xmkpipe" , xmkpipe.class);
		addBuiltinCommand("printvar" , printvar.class);
		addBuiltinCommand("jsonread" , jsonread.class);
    addBuiltinCommand("propread" , readconfig.class);
		addBuiltinCommand("trap" , trap.class);



	}


	public synchronized static CommandFactory getInstance()
	{
		if( _instance == null )
			_instance = new CommandFactory();
		return _instance ;
	}





	public ICommand		getCommand(Shell shell , String name, SourceLocation loc ) throws IOException, CoreException
	{



		ICommand cmd = 
				getFunctionCommand( shell , name , loc  );
		if( cmd == null )
			cmd = getBuiltin(shell, name , loc );
		if( cmd == null )
			cmd = getModuleCommand(shell,name , loc );
		if( cmd == null )
			cmd = getScript( shell , name , false , loc  );
		if( cmd == null )
			cmd = getExternal(shell,name , loc );

		return cmd ;

	}




	/*
	 * Gets an External command of given name
	 * by looking through the External Path
	 */

	private ICommand getFunctionCommand(Shell shell, String name,  SourceLocation loc) {

		IFunctionDecl func = shell.getFunctionDecl( name );
		if( func != null )
			return new FunctionCommand( func.getName() , func.getBody()  , loc );
		return null;
	}

	private ICommand getExternal(Shell shell, String name, SourceLocation loc ) throws IOException 
	{
		File	cmdFile = null;

		if( Util.hasDirectory(name)){

			cmdFile = shell.getExplicitFile( name , true );
			if( cmdFile == null && ! name.endsWith(".exe"))
				cmdFile = shell.getExplicitFile(name + ".exe", true);
		}

		if( cmdFile == null ){
			Path	path = shell.getExternalPath();
			cmdFile = path.getFirstFileInPath(shell,name);
			if( cmdFile == null && ! name.endsWith(".exe"))
				cmdFile = path.getFirstFileInPath( shell,name + ".exe");

		}


		if( cmdFile == null )
			return null;

		return new ExternalCommand( cmdFile, loc  );




	}



	private ICommand getModuleCommand(Shell shell,String name, SourceLocation loc) {




		StringPair 	pair = new StringPair(name,':');



		Modules modules = shell.getModules();


		if( pair.hasLeft() ){ // prefix:name , prefix non-empty
			IModule m   = 
					Util.isBlank(pair.getLeft()) ? 
							shell.getModule() : 
								modules.getModule(pair.getLeft());
							// Allow C:/xxx/yyy to work 
							// May look like a namespace but isnt

							if( m != null ){

								ICommand cls = m.getCommandClass( pair.getRight() );
								if( cls != null ){
									cls.setLocation( loc );

									return cls ;
								}

							}
							return null;
		}

		/* 
		 * Try all default modules 
		 */
		for( IModule m : modules ){
			if( m.isDefault() ){

				ICommand cls = m.getCommandClass( name);
				if( cls != null ){
					cls.setLocation(loc);
					return cls ;
				}
			}
		}


		return null  ;


	}

	public ICommand getScript(Shell shell, String name , InputStream is , boolean bSourceMode , SourceLocation loc ) throws CoreException {
		if( is == null )
			return null;

		return new ScriptCommand(  name , is , bSourceMode , null );

	}


	public ICommand		getScript( Shell shell , String name, boolean bSourceMode , SourceLocation loc ) throws IOException, CoreException
	{
		File scriptFile = null;

		// If name has a scheme try that first
		URL url =  Util.tryURL(name);
		if( url != null )
			return getScript( shell , name , url.openStream() , bSourceMode , loc );


		// If ends with .xsh try it
		if( name.endsWith(".xsh") || bSourceMode )
			scriptFile = shell.getExplicitFile(name,true);

		if( Util.hasDirectory(name)){
			// try adding a .xsh
			if( scriptFile == null  && ! name.endsWith(".xsh"))
				scriptFile = shell.getExplicitFile(name + ".xsh", true);
		}
		else
			if( scriptFile == null ) {

				Path path = shell.getPath(ShellConstants.XPATH, true );
				scriptFile = path.getFirstFileInPath(shell,name);
				if( scriptFile == null && ! name.endsWith(".xsh") )
					scriptFile = path.getFirstFileInPath(shell, name + ".xsh");
			}
		if( scriptFile == null )
			return null ;
		return getScript( shell , scriptFile , bSourceMode , loc );

	}


	private ICommand getBuiltin(Shell shell, String name, SourceLocation loc) {
		Class<?> cls =  mBuiltinCommands.get(name);
		if( cls != null ){
			try {
				ICommand b =  (ICommand) cls.newInstance();
				b.setLocation( loc );
				return b;
			} catch (Exception e) {
				mLogger.error("Exception creating class: " + cls.toString() );
				return null;
			}
		} else
			return null;
	}



	public URL getHelpURL( Shell shell , String name )
	{

		URL url = null ;


		if( url == null )
			url = getBuiltinHelpURL(shell, name);

		if( url == null )
			url = getNativeHelpURL(shell,name);

		return url ;

	}

	private URL getNativeHelpURL(Shell shell, String name) {


		StringPair 	pair = new StringPair(name,':');
		Modules modules = shell.getModules();


		if( pair.hasLeft() ){ // prefix:name , prefix non-empty
			IModule m   = 
					Util.isBlank(pair.getLeft()) ? 
							shell.getModule() : 
								modules.getModule(pair.getLeft());
							// Allow C:/xxx/yyy to work 
							// May look like a namespace but isnt

							if( m != null && m.hasHelp( pair.getRight() ) )
								return m.getHelpURL( );
							return null;
		}

		/* 
		 * Try all default modules 
		 */
		for( IModule m : modules ){
			if( m.isDefault() ){

				if( m != null && m.hasHelp( name ) )
					return m.getHelpURL( );
			}
		}


		return null  ;


	}

	private URL getBuiltinHelpURL(Shell shell, String name) {
		if( mBuiltinCommands.containsKey(name) )
			return shell.getResource(kCOMMANDS_HELP_XML);
		else
			return null ;
	}

	public IFunctionExpr getBuiltinFunction(Shell shell, String name,SourceLocation loc) {

		StringPair 	pair = new StringPair(name,':');



		Modules modules = shell.getModules();


		if( pair.hasLeft() ){ // prefix:name , prefix non-empty
			IModule m   = 
					Util.isBlank(pair.getLeft()) ? 
							shell.getModule() : 
								modules.getModule(pair.getLeft());
							// Allow C:/xxx/yyy to work 
							// May look like a namespace but isnt

							if( m != null ){

								IFunctionExpr cls = m.getFunctionClass( pair.getRight() );
								if( cls != null ){

									return cls ;
								}

							}
							return null;
		}

		/* 
		 * Try all default modules 
		 */
		for( IModule m : modules ){
			if( m.isDefault() ){

				IFunctionExpr cls = m.getFunctionClass( name);
				if( cls != null ){
					return cls ;
				}
			}
		}


		return null  ;	
	}

	public ICommand getScript(Shell shell, File script, boolean bSourceMode, SourceLocation loc) throws CoreException, IOException {
		return getScript( shell , script.getAbsolutePath() , new FileInputStream(script) , bSourceMode , loc );
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
