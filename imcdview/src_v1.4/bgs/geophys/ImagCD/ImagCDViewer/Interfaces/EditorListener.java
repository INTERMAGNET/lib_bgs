/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Interfaces;

import java.io.File;

/**
 * An interface to allow an editor to communicate when it has
 * save a file and when it exits
 * 
 * @author smf
 */
public interface EditorListener 
{
    /** called when the editor saves a file
     * @param editor a reference to the editor that allows
     *        the listener to identify the editor
     * @param file the file that was saved */
    public void savedFile (Object editor, File file);

    /** called when the editor exits
     * @param editor a reference to the editor that allows
     *        the listener to identify the editor */
    public void editorExited (Object editor);
}
