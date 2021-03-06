/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.util.EnumSet;

public abstract class XDynamicVariable extends XVariable {

  public XDynamicVariable(String name, EnumSet<XVarFlag> flags) {
    super(name, flags);

  }

  @Override
  public XVariable clone(EnumSet<XVarFlag> flags)
      throws InvalidArgumentException {
    throw new InvalidArgumentException("Cannot clone: " + getName());

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xmlsh.core.XVariable#getValue()
   */
  @Override
  public abstract XValue getValue();

  @Override
  public XVariable clone() {
    try {
      throw new InvalidArgumentException("Cannot clone: " + getName());
    } catch (InvalidArgumentException e) {
      mLogger.error(e);
    }
    return null;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xmlsh.core.XVariable#setValue(org.xmlsh.core.XValue)
   */
  @Override
  public void setValue(XValue value) throws InvalidArgumentException {
    throw new InvalidArgumentException(
        "Cannot set value of variable: " + getName());
  }

}
