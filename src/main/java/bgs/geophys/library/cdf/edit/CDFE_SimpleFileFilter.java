package bgs.geophys.library.cdf.edit;

/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

// CDFE_SimpleFileFilter.java
// A straightforward extension-based example of a file filter. This should be
// replaced by a "first class" Swing class in a later release of Swing.
//
import javax.swing.filechooser.*;
import java.io.File;

public class CDFE_SimpleFileFilter extends FileFilter {

  String[] extensions;
  String description;

  public CDFE_SimpleFileFilter(String ext) {
    this (new String[] {ext}, null);
  }

  public CDFE_SimpleFileFilter(String exts, String descr) {
    this (new String[] {exts}, null);
  }

  public CDFE_SimpleFileFilter(String[] exts, String descr) {
    // clone and lowercase the extensions
    extensions = new String[exts.length];
    for (int i = exts.length - 1; i >= 0; i--) {
      extensions[i] = exts[i].toLowerCase();
    }
    // make sure we have a valid (if simplistic) description
    description = (descr == null ? exts[0] + " files" : descr);
  }

  public boolean accept(File f) {
    // we always allow directories, regardless of their extension
    if (f.isDirectory()) { return true; }

    // ok, it's a regular file so check the extension
    String name = f.getName().toLowerCase();
    for (int i = extensions.length - 1; i >= 0; i--) {
      if (name.endsWith(extensions[i])) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() { return description; }
}
