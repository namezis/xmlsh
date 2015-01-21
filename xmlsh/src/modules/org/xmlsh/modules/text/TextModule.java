package org.xmlsh.modules.text;

import java.util.List;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;

@org.xmlsh.annotations.Module(name="text")
public class TextModule  extends PackageModule  {
    public TextModule(ModuleConfig config) throws CoreException {
        super(config);
    }

    @Function(name="replace",names={"string-replace"})
    public static class replace extends AbstractBuiltinFunction
    {

        @Override
        public XValue run(Shell shell, List<XValue> args) throws Exception {
            requires(args.size() == 3 , "(text , search , replace)" );
            String text = args.get(0).toString();
            String search = args.get(1).toString();
            String replace = args.get(2).toString();
            return XValue.newXValue(text.replaceAll(search, replace));
        }
    
    }
    
}
