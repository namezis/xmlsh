/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.xmlsh.sh.shell.SerializeOpts;

@SuppressWarnings("serial")
public class LogFrame extends JFrame {

	private JPanel mcontentPane;
	private SerializeOpts mSerializeOps;

	/**
	 * Create the frame.
	 */
	public LogFrame(SerializeOpts ops ) {
		mSerializeOps = ops;
		setTitle("xmlsh Log Window");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		mcontentPane = new JPanel();
		mcontentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mcontentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(mcontentPane);
		setDefaultCloseOperation(HIDE_ON_CLOSE);


		final TextAreaComponent textArea = new TextAreaComponent();
		textArea.setEditable(false);
		new TextComponentPopupMenu( textArea );


		JScrollPane scrollPane = new JScrollPane(textArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mcontentPane.add(scrollPane, BorderLayout.CENTER);

   // LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
    //LoggerConfig conf = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
   // org.apache.logging.log4j.Logger root = LogManager.getRootLogger();

    org.apache.logging.log4j.core.Logger coreLogger =    
        (org.apache.logging.log4j.core.Logger)LogManager.getLogger( "xmlsh.org" );// LogManager.ROOT_LOGGER_NAME);

    LoggerContext context = coreLogger.getContext();
        
    
    ShellAppender appender = new ShellAppender( new TextComponentOutputStream( textArea , mSerializeOps , "log"));
    appender.start();
    coreLogger.addAppender(appender);

	}

}



/*
 * Copyright (C) 2008-2014   David A. Lee.
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