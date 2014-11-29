package org.xmlsh.core.io;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Constructor;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InputPort;
import org.xmlsh.util.Util;

import jline.ConsoleReader;

/*
 * Singleton Console support for all shells
 * Console interface for interactive input , 
 * provides a structured IO as well as stream IO
 * 
 */

public class ShellConsole {
    
    static Logger mLogger = LogManager.getLogger();
    
    private static volatile ShellConsole _instance = null ;
    private Console sJavaConsole ;               // JRE  6 Console if available 
    private ConsoleReader sJLineConsole ;        // JLine 2 Console Reader if available
    private InputStream   sSystemIn;             // System.in 
    private PrintStream   sSystemOut;            // System.out
    private PrintStream   sSystemErr;            // System.err 

    /*
    private Reader mReader ;
    

    public ShellReader getReader() {
        mLogger.entry();
        if( sJavaConsole != null )
            return new ShellConsoleReader(ps1,ps2) ;
        else {
            return new ShellSystemReader(ps1,ps2);
        }
        
    }
    */
    
    public static ShellConsole getConsole() {
    
        if( _instance == null ){
            synchronized( ShellConsole.class){
                if( _instance == null )
                    _instance = new ShellConsole();
            }
        }
        return _instance;
    }
    
    public static boolean hasConsole() {
        return mLogger.exit(getConsole().sJavaConsole != null) ;
    }
        

    private ShellConsole() 
    {
        mLogger.entry();
        
        sJavaConsole = System.console();
        sSystemIn = System.in;
        sSystemOut = System.out;
        sSystemErr = System.err;

        if( sSystemOut == null )
            sSystemOut = sSystemErr ;
        if( sSystemErr == null && sSystemOut != null )
            sSystemErr = sSystemOut;
    }
    
    public boolean hasInput() {
        if( sJavaConsole != null )
            return true ;
        if( sJLineConsole != null )
            return true ;
    
        return sSystemIn != null ;
    
    }
    public boolean hasOutput() {
        if( sJavaConsole != null )
            return true ;
        if( sJLineConsole != null )
            return true ;
    
        return sSystemOut != null ;
    
    }
    public boolean hasErr() {
        if( sJavaConsole != null )
            return true ;
        if( sJLineConsole != null )
            return true ;
        return sSystemErr != null ;
    }

    private class ShellConsoleReader extends ShellReader {

        ShellConsoleReader(IShellPrompt prompt) {
            super(prompt);
        }

        @Override
        protected String readLine(int promptLevel) {
            
            return sJavaConsole.readLine(getPrompt(promptLevel));
        }

        public  InputPort getInputPort()
        {
            return new StreamInputPort( sSystemIn , null , true    );
         }
         
         public OutputPort getOutuptPort()
         {
             return new StreamOutputPort(sSystemOut,false,true)   ;
         }
         public OutputPort getErrorPort()
         {
             return new StreamOutputPort(sSystemErr,false,true)  ;
         }
         
         
    }
    
    
    public  ShellReader newReader(boolean bUseConsole , IShellPrompt prompt) { 
        
        mLogger.entry();
        if( sJavaConsole != null )
            return new ShellConsoleReader(prompt) ;
        else {
            return new ShellReader.ShellSystemReader(sSystemIn,sSystemOut,prompt);
        }
    
    }

/*
 *      Class<?> consoleReaderClass = Class
                            .forName("jline.ConsoleReader");

                    if (consoleReaderClass != null) {
                        Class<?> consoleInputClass = Class
                                .forName("jline.ConsoleReaderInputStream");
                        if (consoleInputClass != null) {
                            // ConsoleReader jline = new ConsoleReader();
                            Object jline = consoleReaderClass.newInstance();

                            Constructor<?> constructor = consoleInputClass
                                    .getConstructor(consoleReaderClass);
                            // mCommandInput = new
                            // ConsoleReaderInputStream(jline);

                        //  if (constructor != null) {
                        //      mCommandInput = (InputStream) constructor
                        //              .newInstance(jline);
                                // System.err.println("using jline");
                            }

                        }
                        
                            
 */
    
    
    
}