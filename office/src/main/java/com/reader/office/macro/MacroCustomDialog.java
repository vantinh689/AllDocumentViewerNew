
package com.reader.office.macro;

import com.reader.office.common.ICustomDialog;

public class MacroCustomDialog implements ICustomDialog
{
    /**
     * 
     * 
     */
    protected MacroCustomDialog(DialogListener listener)
    {
        this.dailogListener = listener;
    }

    /**
     * 
     *
     */
    public void showDialog(byte type)
    {
        if (dailogListener != null)
        {
            dailogListener.showDialog(type);
        }
    }

    /**
     * 
     *
     */
    public void dismissDialog(byte type)
    {
        if (dailogListener != null)
        {
            dailogListener.dismissDialog(type);
        }

    }
    
    public void dispose()
    {
        dailogListener = null;
    }
    
    //
    private DialogListener dailogListener;

}
