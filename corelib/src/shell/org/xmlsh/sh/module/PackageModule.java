/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.module;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.AnnotationUtils;
import org.xmlsh.core.AbstractCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IXFunction;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.ScriptFunctionCommand;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;


public class PackageModule extends Module {

	/*
	 * Constructor for internal modules like xlmsh
	 * These dont get their own thread group
	 */
	
	
	protected static Logger mLogger = LogManager.getLogger();

	protected PackageModule(  ModuleConfig config , XClassLoader loader ) throws CoreException {
		super( config , loader );
	}

	
	@Override
	public String describe() {
		return getName() + "[ packages " + Util.join(getPackages(), ",") + " ]";
	}

	public static String fromReserved(String name) {
		if (JavaUtils.isReserved(name))
			return "_" + name;
		else
			return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.sh.shell.IModule#getCommandClass(java.lang.String)
	 */
	@Override
	public ICommand getCommand(String name) throws FileNotFoundException,
			URISyntaxException {

		/*
		 * Convert from hyphen-case to camelCase
		 */

		name = JavaUtils.convertToCamelCase(name);
		name = fromReserved(name);

		// Store the camel name not the hyphen name
		String origName = name;
		
        Class<?> cls = findCommandClass( name );

		/*
		 * First try to find a class that matches name
		 */

		try {
            if( cls == null )
                // Cached in AbstractModule
                  cls = findClass(name, getPackages());
			if (cls != null) {
				mLogger.trace("Found class matching command: {} " , cls );
				if(  AnnotationUtils.isCommandClass( cls ) || true ){
				Constructor<?> constructor = cls.getConstructor();
				if (constructor != null) {
					mLogger.trace("Found constructor for class : {} " , constructor );
					Object obj = constructor.newInstance();
					if (obj instanceof AbstractCommand) {
						mLogger.trace("Is instanceof AbstractCommand");
						AbstractCommand cmd = (AbstractCommand) obj;
						cmd.setModule(this);
						return cmd;
					} else
					if( ! (obj instanceof ICommand) )
						getLogger()
								.warn("Command class found [ {} ] but is not instance of AbstractCommand.",
										cls.getName());
					else 
						return mLogger.exit((ICommand) obj );
					
				}
			}
			}

		} catch (Exception e) {
			getLogger().debug("Exception calling constructor for:" + name, e);

		}

		/*
		 * Second
		 * Try a script stored as a resource
		 */

		// mScriptCache caches a Boolean indicating whether the resource is found or not
		// No entry in cache means it has not been tested yet

		// Failures are cached with a null command
		String scriptName = origName + ".xsh";

		URL scriptURL = findResourceInPackages(scriptName, getPackages());
		if (scriptURL != null)

			return mLogger.exit(new ScriptCommand(new ScriptSource(scriptName, scriptURL,
					getTextEncoding() ), SourceMode.RUN, null, this));

		return mLogger.exit(null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.sh.shell.IModule#getFunctionClass(java.lang.String)
	 */
	@Override
	public IXFunction getFunction(String name) {

		String origName = name;
		
		/* 
		 * Try unchanged predeclared functions first
		 */
		// Try predeclared functions
					
		Class<?> cls = findFunctionClass( name );
						
		
		
		try {
			/*
			 * Convert from camelCase to hypen-case
			 */

			name = JavaUtils.convertToCamelCase(name);
			name = fromReserved(name);

			
			if( cls == null )
			  // Cached in AbstractModule
		    	cls = findClass(name, getPackages());
			if (cls != null) {
				if( AnnotationUtils.isFunctionClass( cls )){
					Constructor<?> constructor = cls.getConstructor();
					if (constructor != null) {
						Object obj = constructor.newInstance();
						if (obj instanceof IXFunction)
							return (IXFunction) obj;
	
						if (obj instanceof IFunctionDefiniton) {
							IFunctionDefiniton cmd = (IFunctionDefiniton) obj;
							return cmd.getFunction();
						}
					}
			}
		}
		} catch (Exception e) {
			mLogger.catching(e);

		}

		/*
		 * Try a script
		 */
		URL scriptURL = findResourceInPackages(origName + ".xsh", getPackages());
		if (scriptURL != null)
			return new ScriptFunctionCommand(name, scriptURL, this);
		return null;
	}

	protected ModuleConfig getPackageConfig() {
		return (ModuleConfig) super.getConfig() ;
	}
	protected List<String> getPackages() {
		return getPackageConfig().getPackages();
	}

	protected boolean hasCommandResource(String name) {
		for (String pkg : getPackages()) {
			if (getClassLoader().getResource(toResourceName(name, pkg)) != null)
				return true;
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.sh.shell.IModule#hasCommand(java.lang.String)
	 */
	@Override
	public boolean hasHelp(String name) {

		try {
			// Cached in AbstractModule
			Class<?> cls = findClass(name, getPackages());

			if (cls != null)
				return true;

		} catch (Exception e) {
			;

		}

		return hasCommandResource(name + ".xsh");

	}

	@Override
	public URL findResource(String res) {
		return findResourceInPackages( res, getPackages() );
	}

	@Override
	public ModuleConfig getModuleConfig(Shell shell , String qname , List<URL> at ) throws Exception {

		mLogger.entry(shell, qname, at);
		ModuleConfig config = null ;
		// If hame has ":" it might be a schemed or prefixed module 
		StringPair pair = new StringPair(qname, ':');
		String name = pair.getRight();
		String prefix =  pair.getLeft();
	    
	    IModule mod = null ;
	    String helpURL = null ; // TODO:
	  
	    // special scheme
	    if(prefix != null && Util.isEqual(prefix, "java"))
	    	config =  JavaModule.getConfiguration(shell, name, at  );
	    
	    /*
	    if( mod == null && prefix  == null ){
	    	mod = ModuleFactory.createInternalModule( name );
	    }
	    */
	    if( config == null && prefix == null  ){
			mLogger.debug("trying to find package module by name: {} " , name );
			config  =  ModuleFactory.getPackageModuleConfig( shell , name , this.getPackages() ,  null, helpURL  );
	    
	    }
	    /*
	    if( mod == nul){
	    	mod = ModuleFactory.createModuleModule(shell, pair , at );
	    }
	    *
	    
	    if( mod == null )
	    {
	    // Try to find script source by usual means 
	       ScriptSource script  = getScriptSource(shell,qname ,SourceMode.IMPORT , at );
	       if( script != null )
	         mod = createScriptModule(shell ,script, qname, at  );
	    } 
	    
	    /*
	    if( mod == null )
	        mod = ModuleFactory.createExternalModule(shell, qname, at);
	    
	    */
	    
	    return mLogger.exit(config ) ;

		
	}

	private ScriptSource getScriptSource(Shell shell, String name,
			List<URL> at) throws URISyntaxException {
		
		
		String ext = FileUtils.getExt( name );

		boolean bIsXsh = ".xsh".equals(ext);

		
		// Failures are cached with a null command
		String scriptName = ( bIsXsh || ! Util.isBlank(ext)) ? name : ( name  + ".xsh" ) ;
		
		URL scriptURL = findResourceInPackages(scriptName, getPackages());
		if (scriptURL != null){
			ScriptSource ss = new ScriptSource(scriptName, scriptURL,
					getTextEncoding() );
			if( ss != null )
				return ss ;
		}
		return null;
	}

	private ModuleConfig getScriptModuleConfig(Shell shell, ScriptSource script,
			String qname, List<URL> at) throws URISyntaxException, IOException, CoreException {
		
		ScriptSource ss = getScriptSource(shell,qname, at );
		if( ss !=null )
			return ScriptModule.getConfiguration(shell, script ,at);
		return null;
	
	}

	@Override
	public void onInit(Shell shell, List<XValue> args) throws Exception {
		
		
	}

	@Override
	public void onLoad(Shell shell) {
		
		super.onLoad(shell);
		reflectModuleClass( shell , getClass() );
		reflectClassNames( shell ,  getConfig().getClassNames() );

	}

	private void reflectClassNames(Shell shell, List<String> classNames) {
		mLogger.entry(shell, classNames);
		
		if( classNames == null )
			return ;
		
		
		// Try exact name , module class package relative and package relative
		
		List<String> pkgs = new ArrayList<>();

		pkgs.add(null);
		pkgs.addAll(getPackages()); 
		
		for( String className : classNames ){
		  mLogger.trace("Looking for module referenced class: {}" , className);
			Class<?> cls = findClass(className,pkgs);
		  if( cls != null ){
			  mLogger.debug("Found for module referenced class: {}" , cls.getName());
			  reflectClass( shell , cls );
		  }
		  
		}

		mLogger.exit();
		
	}

	protected void reflectModuleClass(Shell shell, Class<?> cls ) 
	{
		reflectClass( shell , cls );

		
	}

	private void reflectClass(Shell shell, Class<?> cls) {
		mLogger.entry(shell, cls);
		
		List<String> names = AnnotationUtils.getFunctionNames(cls);
		// Look for functions 
		if( ! Util.isEmpty(names) ){
		    mLogger.debug("found function annotations {}" , names );
			cacheFunctionClass( names , cls );
		}
		
		names = AnnotationUtils.getCommandNames( cls );
	      // Look for commands 
        if( ! Util.isEmpty(names) ){
            mLogger.debug("found command annotations {}" , names );
            cacheCommandClass( names , cls );
            
        }
		
		for( Class<?>  c : cls.getClasses()){
			reflectClass(shell,c);
		}
		
		mLogger.exit();
		
	}
	
	
	


}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
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
 */