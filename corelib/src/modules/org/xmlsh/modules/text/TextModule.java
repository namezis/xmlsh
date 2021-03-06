package org.xmlsh.modules.text;

import java.util.List;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;

@org.xmlsh.annotations.Module(name="text")
public class TextModule  extends PackageModule  {
    public TextModule(ModuleConfig config, XClassLoader loader) throws CoreException {
        super(config, loader);
    }

    
}
