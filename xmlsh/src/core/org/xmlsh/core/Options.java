/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

public class Options {

	/*
	 * A single option is of the form [+]short[=long][:[+]] Multiple options are
	 * separated by ","
	 * 
	 * [+] If option starts with a "+" then it is a boolean option that at
	 * runtime can start with a + or -. for example cmd +opt short The short
	 * form of the option. Typically a single letter =long The long form of the
	 * option. Typically a word [:[+]] If followed by a ":" then the option is
	 * required to have a value which is taken from the next arg If followed by
	 * a ":+" then the option can be specified multiple times
	 * 
	 * 
	 * Examples
	 * 
	 * a Single optional option "-a" a=all Long form accepted either "-a" or
	 * "-all" +v=verbose Long or short form may be specified with - or + e.g. -v
	 * or +verbose i: Option requires a value. e.g -i inputfile i:+ Option may
	 * be specified multiple times with values. e.g. -i input1 -i input2
	 */

	public static class OptionDef {
		private String name; // short name typically 1 letter
		private String longname; // long name/alias
		private boolean expectsArg; // expects an argument
		private boolean multiple; // may occur multiple times
		private boolean flag; // may be preceeded by +

		public OptionDef(String name, String longname, boolean arg,
				boolean multi, boolean plus) {
			setName(name);
			setLongname(longname);
			setExpectsArg(arg);
			setMultiple(multi);
			setFlag(plus);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLongname() {
			return longname;
		}

		public void setLongname(String longname) {
			this.longname = longname;
		}

		public boolean isExpectsArg() {
			return expectsArg;
		}

		public void setExpectsArg(boolean expectsArg) {
			this.expectsArg = expectsArg;
		}

		public boolean isMultiple() {
			return multiple;
		}

		public void setMultiple(boolean multiple) {
			this.multiple = multiple;
		}

		public boolean isFlag() {
			return flag;
		}

		public void setFlag(boolean flag) {
			this.flag = flag;
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof OptionDef))
				return false;

			OptionDef othat = (OptionDef) that;
			if (this == that)
				return true;

			return name.equals(othat.name) && longname.equals(othat.longname)
					&& expectsArg == othat.expectsArg
					&& multiple == othat.multiple && flag == othat.flag;
		}

		public boolean isOption(String str) {
			assert (str != null);
			return Util.isEqual(str, name) || Util.isEqual(str, longname);
		}

	}

	public static class OptionValue {
		private OptionDef option;
		private boolean optflag = true; // true if '-' , false if '+'
		private XValue value = null;

		OptionValue(OptionDef def, boolean flag) {
			option = def;
			optflag = flag;
		}

		// Set a single value
		void setValue(XValue v) throws UnexpectedException,
				InvalidArgumentException {
			if (option.isExpectsArg())
				value = v;
			else if (option.isFlag())
				optflag = v.toBoolean() ? true : false;
			else
				throw new UnexpectedException("Unexpected use of option: "
						+ option.name);
		}

		/**
		 * @return the option
		 */
		public OptionDef getOptionDef() {
			return option;
		}

		/**
		 * @return the arg
		 */
		public XValue getValue() {
			return option.isExpectsArg() ? value : XValue.newXValue(optflag);
		}

		public boolean getFlag() {
			return optflag;
		}

	}

	@SuppressWarnings("serial")
	public static class OptionDefs extends ArrayList<OptionDef> {
		public OptionDefs() {
			super();
		}
		public OptionDefs( String sdefs ){
			parseDefs(this,sdefs);
		}

		public OptionDefs(List<? extends OptionDef> c) {
			for (OptionDef d : c)
				addOptionDef(d);
		}

		public OptionDefs(OptionDef o) {
			super.add(o); // Safe - only 1 option 
		}

		public OptionDefs addOptionDef(OptionDef def) {
			OptionDef exists = getOptionDef(def.getName());
			if (exists != null) {
				mLogger.warn("Redefined option def: {}", def.getName());
				remove(exists);
			}
			add(def);
			return this;
		}

		public OptionDefs(OptionDef... defs) {
			addOptionDefs( defs );
		}

		public OptionDefs addOptionDefs(OptionDefs defs) {
			for (OptionDef def : defs)
				addOptionDef(def);
			return this;
		}

		public OptionDefs addOptionDefs(OptionDef... defs) {
			for (OptionDef od : defs)
				addOptionDef(od);
			return this ;
		}

		public OptionDef getOptionDef(String str) {

			for (OptionDef opt : this) {
				if (Util.isEqual(str, opt.getName())
						|| Util.isEqual(str, opt.getLongname()))
					return opt;
			}
			return null;
		}
		

		public OptionDefs withOption(OptionDef def) {
			return addOptionDef(def);
		}

		public OptionDefs withOptions(OptionDefs defs) {
			return addOptionDefs(defs);
		}

		public OptionDefs withOptions(String sdefs) {
			return parseDefs(this, sdefs);
		}

		public static OptionDefs parseDefs(String sdefs) {

			return parseDefs(new OptionDefs(), sdefs);
		}

		public static OptionDefs parseDefs(OptionDefs defs, String sdefs) {

			String[] adefs = sdefs.trim().split("\\s*,\\s*");
			for (String sdef : adefs) {
				boolean bHasArgs = false;
				boolean bHasMulti = false;
				boolean bPlus = false;

				if (sdef.startsWith("+")) {
					bPlus = true;
					sdef = sdef.substring(1);
				} else

				if (sdef.endsWith(":")) {
					sdef = sdef.substring(0, sdef.length() - 1);
					bHasArgs = true;
				} else if (sdef.endsWith(":+")) {
					sdef = sdef.substring(0, sdef.length() - 2);
					bHasArgs = true;
					bHasMulti = true;
				}

				// Check for optional long-name
				// a=longer
				StringPair pair = new StringPair(sdef, '=');

				if (pair.hasDelim())
					defs.addOptionDef(new OptionDef(pair.getLeft(), pair
							.getRight(), bHasArgs, bHasMulti, bPlus));
				else
					defs.addOptionDef(new OptionDef(sdef, null, bHasArgs,
							bHasMulti, bPlus));

			}
			return defs;
		}

		public static OptionDefs parseDefs(String defs1, String... defsv) {
			OptionDefs defs = parseDefs(defs1);
			for (String sd : defsv)
				defs = OptionDefs.parseDefs(defs, sd);

			return defs;
		}

	}
	private OptionDefs mDefs;
	private List<XValue> mRemainingArgs;
	private List<OptionValue> mOptions;
	private boolean mDashDash = false;

	static Logger mLogger = LogManager.getLogger();

	/*
	 * Parse a string list shorthand for options defs "a,b:,cde:" =>
	 * ("a",false),("b",true),("cde",true)
	 */

	public static OptionDefs parseDefs(String sdefs) {
		return OptionDefs.parseDefs(sdefs);
	}

	public Options(String options) {
		this(parseDefs(options));
	}

	public Options(OptionDefs options) {
		mDefs = options;

	}

	// @Depreciated
	public Options(String option_str, OptionDefs option_list) {
		this(parseDefs(option_str).withOptions(option_list));
	}

	public OptionDefs addOptionDefs(String option_str) {
		OptionDefs option_list = parseDefs(option_str);
		addOptionDefs(option_list);
		return option_list;

	}

	public void addOptionDefs(OptionDefs option_list) {
		mDefs.addOptionDefs(option_list);

	}

	public OptionDef getOptDef(String str) {
		assert (mDefs != null);
		return mDefs.getOptionDef(str);

	}
    public List<OptionValue> parse(List<XValue> args) throws UnknownOption, UnexpectedException, InvalidArgumentException
    {
        return parse( args , false );
    }
    

	public List<OptionValue> parse(List<XValue> args, boolean stopOnUnknown) throws UnknownOption,
			UnexpectedException, InvalidArgumentException {
		if (mOptions != null)
			return mOptions;

		mOptions = new ArrayList<OptionValue>();
        mRemainingArgs = new ArrayList<XValue>();
        Iterator<XValue> I = args.iterator();
		while ( I.hasNext()) {
			XValue arg = I.next();

			String sarg = (arg.isAtomic() ? arg.toString() : null);

			if (sarg != null && (sarg.startsWith("-") || sarg.startsWith("+"))
					&& !sarg.equals("--") && !Util.isInt(sarg, true)) {
				String a = sarg.substring(1);
				char flag = sarg.charAt(0);

				OptionDef def = getOptDef(a);
				if (def == null){
				    if( stopOnUnknown ) {   
	                    mRemainingArgs.add(arg);
	                    break ;
				    }
					throw new UnknownOption("Unknown option: " + a);
				}
				if (flag == '+' && !def.isFlag())
					throw new UnknownOption("Option : " + a
							+ " cannot start with +");

				boolean bRepeat = this.hasOpt(def);

				if (bRepeat && !def.isMultiple())
					throw new UnknownOption(
							"Unexpected multiple use of option: " + arg);
				OptionValue ov = new OptionValue(def, flag == '-');
				if (def.isExpectsArg()) {
					if (!I.hasNext())
						throw new UnknownOption("Option has no args: " + arg);
					ov.setValue(I.next());
				}
				mOptions.add(ov);

			} else {

				if (arg.isAtomic() && arg.equals("--")) {
					arg = null;
					mDashDash = true;
				}
				if (arg != null)
					mRemainingArgs.add(arg);

				break;

			}

		}
		while (I.hasNext())
              mRemainingArgs.add(I.next());
		return mOptions;

	}

	public List<OptionValue> getOpts() {
		return mOptions;
	}

	public OptionValue getOpt(OptionDef def) {
		assert (def != null);
		for (OptionValue ov : mOptions) {

			if (ov.option.equals(def))
				return ov;
		}
		return null;
	}

	public boolean hasOpt(OptionDef def) {
		return getOpt(def) != null;
	}

	public OptionValue getOpt(String opt) {
		for (OptionValue ov : mOptions) {
			if (ov.getOptionDef().isOption(opt))
				return ov;
		}
		return null;
	}

	public boolean hasOpt(String opt) {
		return getOpt(opt) != null;

	}

	public boolean getOptFlag(String opt, boolean defValue) {
		OptionValue value = getOpt(opt);
		if (value == null)
			return defValue;
		else
			return value.getFlag();
	}

	public String getOptString(String opt, String defValue) {
		OptionValue value = getOpt(opt);
		if (value != null)
			return value.getValue().toString();
		else
			return defValue;

	}

	public String getOptStringRequired(String opt)
			throws InvalidArgumentException {
		OptionValue value = getOpt(opt);
		if (value != null)
			return value.getValue().toString();

		throw new InvalidArgumentException("Required option: -" + opt);

	}

	public boolean getOptBool(String opt, boolean defValue) {
		OptionValue value = getOpt(opt);
		if (value != null)
			try {
				return value.getValue().toBoolean();
			} catch (Exception e) {
				return false;
			}
		return defValue;

	}

	public List<XValue> getRemainingArgs() {
		if (mRemainingArgs == null)
			mRemainingArgs = new ArrayList<XValue>(0);
		return mRemainingArgs;
	}

	public XValue getOptValue(String arg) {
		OptionValue ov = getOpt(arg);
		if (ov == null)
			return null;
		else
			return ov.getValue();
	}

	public XValue getOptValueRequired(String arg)
			throws InvalidArgumentException {
		OptionValue ov = getOpt(arg);
		if (ov != null)
			return ov.getValue();
		throw new InvalidArgumentException("Required option: -" + arg);
	}

	public List<XValue> getOptValuesRequired(String arg)
			throws InvalidArgumentException {

		List<XValue> values = getOptValues(arg);
		if (values == null || values.isEmpty())
			throw new InvalidArgumentException("Required option: -" + arg);

		return values;
	}

	public List<XValue> getOptValues(String arg)
			throws InvalidArgumentException {

		ArrayList<XValue> values = new ArrayList<>();
		for (OptionValue ov : mOptions) {
			if (ov.getOptionDef().isOption(arg))
				values.add(ov.getValue());

		}
		return values.isEmpty() ? null : values;
	}

	public boolean hasRemainingArgs() {
		return mRemainingArgs != null && !mRemainingArgs.isEmpty();
	}

	public double getOptDouble(String opt, double def) {
		return Util.parseDouble(getOptString(opt, ""), def);
	}

	public int getOptInt(String opt, int def) {
		return Util.parseInt(getOptString(opt, ""), def);
	}

	public long getOptLong(String opt, long l) {
		return Util.parseLong(getOptString(opt, ""), l);
	}

	public boolean hasDashDash() {
		return mDashDash;
	}

	/**
	 * @return the defs
	 */
	public OptionDefs getOptDefs() {
		return mDefs;
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
