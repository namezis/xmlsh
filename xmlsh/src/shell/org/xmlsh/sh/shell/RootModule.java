package org.xmlsh.sh.shell;

import java.io.IOException;

import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;


/*
 * Top level root module for the shell
 */
public class RootModule extends AbstractModule {

	
	// TEMP
	private static RootModule _instance = new RootModule();
	
	public static ModuleHandle getInstance() {
		return new ModuleHandle(_instance );
	}
	
	private RootModule() {
		super("builtin");
	}
	
	public String toString() {
		return getName();
	}
	@Override
	public ICommand getCommandClass(String name) throws IOException {
		return null;
	}

	@Override
	public IFunction getFunctionClass(String name) {
		return null;
	}

	@Override
	public boolean hasHelp(String name) {
		return false;
	}

	@Override
	public String describe() {
		return getName();
	}

	@Override
	public void close() {
		getLogger().error("Root Module should never be closed");
		assert(false);
	}
}
