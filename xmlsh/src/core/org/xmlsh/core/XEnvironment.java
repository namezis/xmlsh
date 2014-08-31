/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.xml.sax.InputSource;
import org.xmlsh.core.XVariable.XVarFlag;
import static org.xmlsh.core.XVariable.*;
import static org.xmlsh.core.XVariable.XVarFlag.*;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Stack;

import javax.xml.transform.Source;

public class XEnvironment implements AutoCloseable, Closeable {

	@SuppressWarnings("unused")
	private 	static Logger mLogger = LogManager.getLogger( XEnvironment.class );
	private 	Shell mShell;
	private		volatile XIOEnvironment mIO;
	private		Variables	mVars;
	private		Namespaces	mNamespaces = null;

	private		Stack<XIOEnvironment>  mSavedIO;




	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}


	public XEnvironment(Shell shell, boolean bInitIO ) throws IOException
	{
		mShell = shell;
		mVars = new Variables();
		mIO = new XIOEnvironment();

		if( bInitIO )
			getIO().initStdio();


	}


	// Only for the empty environment 
	private XEnvironment()
	{
		// TODO Auto-generated constructor stub
	}


	public void	addAutoRelease( AbstractPort obj )
	{
		getIO().addAutoRelease(obj);
	}


	private XIOEnvironment getIO()
	{
		if( mIO == null ) {
			synchronized( this ) {
				mIO = new XIOEnvironment();
			}
		}
		return mIO ;
	}

	/*
	 * Standard Varibles 
	 */
	public XVariable	getVar( String name )
	{
		/*
		 * Special variables 
		 */
	 
		if( name == null ) {
			// $* 
	    	 return new XVariable( null ,  new XValue( mShell.getArgs() ),  shellArgListFlags() );
		}
		
		switch( name ) {
		case "*" :
			return new XVariable( null ,  new XValue( mShell.getArgs() ) , shellArgListFlags( )  );
		case "@" :
			return  mShell.getArgs().size() == 0 ? null 
					: new XVariable( null ,  new XValue( mShell.getArgs() ) , shellArgListFlags( )  );
	     case "#" :
	    	 return new XVariable( name , new XValue(mShell.getArgs().size()) , standardFlags() );
	     case "$" :
	    	 return new XVariable( name ,new XValue(Thread.currentThread().getId()), standardFlags() );
	     case "?" :
	            return new XVariable( name ,new XValue(mShell.getStatus()),standardFlags());
	     case "!" :
	    	  return new XVariable( name , new XValue(mShell.getLastThreadId()),standardFlags());
		}
		if(Util.isInt(name, false)) {
           int n = Util.parseInt(name, -1);
           if(n == 0)
        	   return new XVariable( name , new XValue(mShell.getArg0()),shellArgFlags());
           else if(n > 0 && n <= mShell.getArgs().size()) 
        	   return new XVariable( name ,mShell.getArgs().get(n - 1), shellArgFlags());
            else
        	   return null ;
       }
		return mVars.get(name);
	}

	public void	setVar( XVariable var, boolean local)
	{
		String name = var.getName();
		if( local )
	  	var.getFlags().add(LOCAL);
		else
		  var.getFlags().remove(LOCAL);
		mVars.put(name , var, local );
	}


	public void setIndexedVar( String name , XValue value, String ind , EnumSet<XVarFlag> flags , boolean local  ) throws CoreException 
	{
	    XVariable var = mVars.get(name);
        if( var == null )
           var = new XVariable( name ,  new XValue( TypeFamily.XTYPE , new XValueMap()) , flags  );
      else
          var = var.clone();
          
      var.setIndexedValue(value, ind);
            
	    setVar( var , local );
	}



	/*
	 * Append to a variable as a sequence 
	 */
	public void appendVar(String name, XValue xvalue, EnumSet<XVarFlag> varFlags, boolean local ) throws InvalidArgumentException {


		XVariable var = mVars.get(name);
		if( var == null ){
			// If no existing variable then dont touch
			setVar(new XVariable( name , xvalue  , varFlags ) , local );
			return ;
		}

		var = var.clone();
		xvalue = var.getValue().append(xvalue);
		var.setValue(  xvalue  );
		setVar( var , local);

	}
	
	 public void appendVar(String name, XValue xvalue, boolean local ) throws InvalidArgumentException {
	   appendVar(name,xvalue,standardFlags() , local );
	 }



  public void setVar( String name , XValue value, boolean local ) throws InvalidArgumentException 
  {

    XVariable var = mVars.get(name);
    if( var == null )
      var = new XVariable( name , value  );
    else
      var = var.newValue( value );


    setVar( var , local );
  }


	@Override
	public XEnvironment clone()
	{

		// TODO When cloning, only export marked for export vars
		// Add typeset command




		try {
			return clone( mShell );
		} catch (IOException e) {
			mShell.printErr("Exception cloning shell", e);
			return null;
		}
	}
	/*
	 * Clone an environment for use in a new thread
	 * 
	 * @see java.lang.Object#clone()
	 */

	public XEnvironment clone(Shell shell) throws IOException
	{
		XEnvironment 	that = new XEnvironment(shell, false);
		that.mVars		= new Variables(this.mVars);

		that.mIO = new XIOEnvironment(this.getIO());

		if( this.mNamespaces != null )	
			that.mNamespaces = new Namespaces( this.mNamespaces );

		return that;
	}


	@Override
	public void close() throws IOException  {
		if( this.mSavedIO != null && ! mSavedIO.isEmpty())
			throw new IOException("Closing XEnvironment when mSavedIO is not empty");

		getIO().release();
	}

	public Shell getShell() { 
		return mShell;
	}

	public Variables getVars() { return mVars ; }


	public Collection<String> getVarNames() {
		return mVars.getVarNames();
	}

	public String getVarString( String key )
	{
		XVariable var = getVar(key);
		if( var== null )
			return null ;

		return var.getValue().toString();
	}

	/*
	 * Save the environment by cloning it and pushing it to this
	 * and return the OLD environment
	 */
	public void saveIO() throws CoreException
	{
		if( mSavedIO == null )
			mSavedIO = new Stack<XIOEnvironment>();

		mSavedIO.push(getIO());
		mIO = new XIOEnvironment(getIO());

	}

	public void restoreIO()
	{
		getIO().release();
		mIO = mSavedIO.pop();
	}







	/**
	 * @param file
	 * @param append
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CoreException 
	 * @see org.xmlsh.sh.shell.Shell#getOutputStream(java.lang.String, boolean)
	 */
	public OutputStream getOutputStream(String file, boolean append, SerializeOpts opts ) throws FileNotFoundException,
	IOException, CoreException {
		return mShell.getOutputStream(file, append, opts );
	}

	public OutputStream getOutputStream(File file, boolean append) throws FileNotFoundException {
		return mShell.getOutputStream(file, append);
	}
	/**
	 * @param s
	 * @param e
	 * @see org.xmlsh.sh.shell.Shell#printErr(java.lang.String, java.lang.Exception)
	 */
	public void printErr(String s, Exception e) {
		mShell.printErr(s, e);
	}


	/**
	 * @param s
	 * @see org.xmlsh.sh.shell.Shell#printErr(java.lang.String)
	 */
	public void printErr(String s) {
		mShell.printErr(s);
	}


	/**
	 * @return
	 * @see org.xmlsh.sh.shell.Shell#getCurdir()
	 */
	public File getCurdir() {
		return mShell.getCurdir();
	}


	/**
	 * @param cd
	 * @throws IOException 
	 * @see org.xmlsh.sh.shell.Shell#setCurdir(java.io.File)
	 */
	public void setCurdir(File cd) throws IOException {
		mShell.setCurdir(cd);
	}


	public XValue getVarValue(String name) {
		XVariable var = getVar(name);
		if( var == null )
			return null ;
		else
			return var.getValue();
	}


	public void unsetVar(String name ) throws InvalidArgumentException {
		mVars.unset( name );
	}

	public boolean isStdinSystem() { return  getStdin().isSystem(); }
	public boolean isStdoutSystem() { return  getStdout().isSystem() ; }
	public boolean isStderrSystem() { return  getStderr().isSystem() ; }


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStderr()
	 */
	public OutputPort getStderr() {
		return getIO().getStderr();
	}


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStdin()
	 */
	public InputPort getStdin()  {
		return getIO().getStdin();
	}


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStdout()
	 */
	public OutputPort getStdout()  {
		return getIO().getStdout();
	}


	/*
	 * Create or return an output port - managed by the autorelease pool
	 */
	public OutputPort getOutput( XValue port, boolean append ) throws IOException
	{


		if( port == null )
			return getStdout();
		if( port.isAtomic()){
			String name = port.toString().trim();
			if( name.equals("-"))
				return getStdout();

			OutputPort p = mShell.newOutputPort(name, append);
			addAutoRelease(p);
			return p;
		}
		else
		{
			OutputPort p = new VariableOutputPort(  new XVariable(null,port,standardFlags()) );
			addAutoRelease(p);
			return p;
		}


	}

	public OutputPort getOutput( String port , boolean append) throws IOException
	{
		return getOutput( new XValue(port) , append );
	}


	public OutputPort getOutput( File file , boolean append ) throws IOException
	{

		return new FileOutputPort( file , append);



	}


	/**
	 * @param stderr
	 * @throws IOException
	 * @throws InvalidArgumentException 
	 * @see org.xmlsh.core.XIOEnvironment#setStderr(java.io.OutputStream)
	 */
	public void setStderr(OutputStream stderr) throws CoreException {
		getIO().setStderr(stderr);
	}
	public void setStderr(OutputPort stderr) throws CoreException {
		getIO().setStderr(stderr);
	}


	/**
	 * @param stdin
	 * @throws IOException
	 * @see org.xmlsh.core.XIOEnvironment#setStdin(java.io.InputStream)
	 */

	public void setStdin(InputStream in) throws IOException {
		setInput(null, in );
	}

	public void setStdin(XVariable variable) throws IOException, InvalidArgumentException {

		setInput( null , variable );
	}
	public void setStdin(InputPort in ) throws IOException {

		setInput( null , in );
	}

	public InputPort setInput(String name,InputStream in) throws IOException  {
		return getIO().setInput( name,new StreamInputPort(in,null));
	}

	public InputPort setInput(String name, XVariable variable) throws IOException, InvalidArgumentException {

		return getIO().setInput( name,new VariableInputPort(variable));
	}

	public InputPort setInput( String name , InputPort in ) throws IOException {

		return getIO().setInput( name  , in );
	}


	public void setStdout(OutputStream out) throws CoreException {
		setOutput( null ,  new StreamOutputPort(out));

	}
	public void setStdout(OutputPort  port) throws CoreException {
		setOutput( null , port );
	}

	public void setStdout(XVariable xvar) throws CoreException {
		setOutput( null ,  new VariableOutputPort(xvar));
	}

	public void setOutput(String name ,OutputStream out) throws CoreException {
		setOutput( name,new StreamOutputPort(out));

	}


	public void setOutput(String name ,XVariable xvar) throws CoreException {
	  assert( xvar != null );
		setOutput( name,new VariableOutputPort(xvar));
	}

	public void setOutput(String name , OutputPort out) throws CoreException {
		getIO().setOutput( name, out );
	}


	public void declareNamespace(String ns ) {

	  getNamespaces().declare( ns );

	}
	public void declareNamespace(String prefix, String uri) {
		
	  getNamespaces().declare(prefix, uri);
	}


	public Namespaces getNamespaces()
	{
	  if( mNamespaces == null )
      mNamespaces = new Namespaces();
		return mNamespaces;
	}


	public InputStream getInputStream(XValue file,SerializeOpts opts) throws IOException, InvalidArgumentException, CoreException{
		return getInput(file).asInputStream(opts);
	}



	public Source getSource(XValue value,SerializeOpts opts) throws  IOException, InvalidArgumentException, CoreException {
		return getInput(value).asSource(opts);

	}




	/*
	 * Get an input by name or value
	 * 
	 * If port is null return stdin
	 * If port is a string 
	 * If port is a string 
	 * 	  if equals to "-" return stdin
	 *    if looks like "scheme://path" return a port based on an input stream from UI
	 *    if looks like "name" return a port based on an input stream by filename
	 * if port is a node return an anonymous port based on a value
	 * 
	 */
	public InputPort getInput(XValue port) throws  IOException, InvalidArgumentException  {

		if( port == null )
			return getStdin();
		if( port.isAtomic()){
			String name = port.toString().trim();
			if( name.equals("-"))
				return getStdin();


			InputPort p = mShell.newInputPort(name);
			// Port is not managed, add to autorelease
			addAutoRelease( p );
			return p;


		}
		else
		{
			VariableInputPort p = new VariableInputPort(  new XVariable(null,port,standardFlags()) );
			// Port is not managed, add to autorelease
			addAutoRelease(p);
			return p;
		}

	}

	/*
	 * Get an input port explicitly by its name 
	 */
	public InputPort getInputPort(String name)
	{
		return getIO().getInputPort(name);
	}

	public InputPort getInput( String name ) throws CoreException, IOException
	{
		return getInput( new XValue(name));
	}

	public OutputPort getOutputPort(String name){
		return getIO().getOutputPort(name);
	}


	public String getAbsoluteURI(String sysid) throws URISyntaxException 
	{
		URI uri = new URI(sysid);
		if( uri.isAbsolute())
			return sysid ;

		URI absolute = getBaseURI().resolve(sysid );
		return absolute.toString();


	}


	public URI getBaseURI() {
		return getCurdir().toURI();
	}



	public OutputPort getOutputPort(String portname, boolean append) {
		// TODO: Add append mode to output ports 
		return getIO().getOutputPort(portname);
	}


	public InputSource getInputSource(XValue value, SerializeOpts opts) throws CoreException, FileNotFoundException, IOException {
		InputPort in = getInput(value);
		return in.asInputSource(opts);

	}


	public boolean isDefined(String name) {
		return mVars.containsKey(name);

	}


	public Variables pushLocalVars() {
		Variables current = mVars ;
		mVars = mVars.pushLocals();
		return current ;

	}


	public void popLocalVars(Variables vars) {
		mVars = vars ;

	}


	/**
	 * @return the savedIO
	 */
	public XIOEnvironment getSavedIO() {
		return mSavedIO.peek();
	}




	// "1>&2" 
	public void dupOutput(String portLeft, String portRight ) throws IOException {
		getIO().dupOutput( portLeft , portRight );

	}


	public void dupInput(String portLeft, String portRight) throws IOException {
		getIO().dupInput( portLeft , portRight );

	}


	public static boolean isSpecialVarname( String name ) {
	     switch( name ) {
	     case "#" :
	     case "$" :
	     case "?" :
	     case "!" :
	     case "*" : 
	     case "@" :
	         return true ;
	      default:
	          return false ;
	      
	     }
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
