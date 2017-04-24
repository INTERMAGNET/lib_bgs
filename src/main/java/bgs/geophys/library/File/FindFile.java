package bgs.geophys.library.File;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * This source code came from the Jodd project.
 *
 * Generic file finder, browses all files of specified folder. Search starts
 * with the call to <code>first()</code> method, that will return found
 * file or <code>null</code> if no file found. Method <code>next()</code>
 * then retrieves all other files found, one by one. It will return
 * <code>null</code> when search is over. Here is the example:
 * <code>
 * FindFile ff = new FindFile();
 * File f = ff.first("class");
 * while (f != null) {
 *
 *     f = ff.next();
 * }
 * </code>
 *
 * This is default behaviour, and it can be changed with following
 * flags:
 * <ul>
 * <li>r - search subdirectories (i.e. recursivly)</li>
 * <li>d - also include directories in search results</li>
 * <li>x - exclude files from search results</li>
 * </ul>
 *
 * A list of <code>FileFilter</code> implementation can be added to the
 * search. Each file is matched on every availiable filter, and only if
 * all filters accept the file, it will be returned.
 *
 * @see java.io.FileFilter
 */
public class FindFile {

	// ---------------------------------------------------------------- flags

	private boolean recursive = false;
	public void setRecursive(boolean v) {
		recursive = v;
	}
	public boolean getRecursive() {
		return recursive;
	}

	private boolean incDirs = false;
	public void setIncludeDirs(boolean v) {
		incDirs = v;
	}
	public boolean getIncludeDirs() {
		return incDirs;
	}

	private boolean exFiles = false;
	public void setExcludeFiles(boolean v) {
		exFiles = v;
	}
	public boolean getExcludeFiles() {
		return exFiles;
	}

	// ---------------------------------------------------------------- filters

	private List<FileFilter> ffilters = null;
	private int ffilters_size = 0;

	public void setFileFilters(List<FileFilter> ffs) {
		ffilters = ffs;
		if (ffilters != null) {
			ffilters_size = ffilters.size();
		} else {
			ffilters_size = 0;
		}
	}

	public void setFileFilters(FileFilter[] ff) {
		ArrayList<FileFilter> al = new ArrayList<FileFilter>(ff.length);
		for (int i = 0; i < ff.length; i++) {
			al.add(ff[i]);
		}
		setFileFilters(al);
	}

	public void setFileFilters(FileFilter ff) {
		ArrayList<FileFilter> al = new ArrayList<FileFilter>();
		al.add(ff);
		setFileFilters(al);
	}

	public void setFileFilters() {
            ffilters_size = 0;
            ffilters = new ArrayList<FileFilter> ();
	}


	public List getFileFilters() {
		return ffilters;
	}

	public FileFilter getFileFilter() {
		if (ffilters != null) {
			return (FileFilter) ffilters.get(0);
		} else {
			return null;
		}
	}

	// ---------------------------------------------------------------- finder

	private ArrayList<File> fileList = null;
	private int ndx = 0;
	private int fileListSize = 0;


	public File first(String dir, boolean recursive, boolean include_dirs, boolean exclude_files, FileFilter ff) {
		setFileFilters(ff);
		return dofirst(dir, recursive, include_dirs, exclude_files);
	}

	public File first(String dir, boolean recursive, boolean include_dirs, boolean exclude_files, FileFilter[] ffa) {
		setFileFilters(ffa);
		return dofirst(dir, recursive, include_dirs, exclude_files);
	}

	public File first(String dir, boolean recursive, boolean include_dirs, boolean exclude_files, List<FileFilter> ffs) {
		setFileFilters(ffs);
		return dofirst(dir, recursive, include_dirs, exclude_files);
	}

	public File first(String dir, boolean recursive, boolean include_dirs, boolean exclude_files) {
		setFileFilters();
		return dofirst(dir, recursive, include_dirs, exclude_files);
	}

	public File first(String dir) {
		return dofirst(dir, false, false, false);
	}

	private File dofirst(String dir, boolean recursive, boolean include_dirs, boolean exclude_files) {
                this.recursive = recursive;
                this.exFiles = exclude_files;
                this.incDirs = include_dirs;

		// start
		fileList = new ArrayList<File>();
		File f = new File(dir);
		if (f.exists() == false) {
			return null;						// directory (or file) doesn't exist
		}
		if (f.isDirectory() == false) {
			return f;							// if not a directory, return it
		}
		File[] childs = f.listFiles();
		for (int i = 0; i < childs.length; i++) {
			fileList.add(childs[i]);
		}
		ndx = 0;
		fileListSize = fileList.size();
		return next();
	}

	/**
	 * Finds the next file once when search is activated.
	 *
	 * @return founded file, <code>null</code> if no file has been found or if no more files.
	 */
	public File next() {
		if (ndx == fileListSize) {
			return end();
		}
		File f = null;
		boolean found = false;

		loop: while (ndx < fileListSize) {
			f = (File) fileList.get(ndx);
			ndx++;

			if (f.isDirectory()) {							// directory found
				fileList.subList(0, ndx).clear();			// release previous list elements from memory
				ndx = 0;
				if (recursive == true) {					// recursive: append subfolder files to the list
					File[] childs = f.listFiles();
					for (int i = 0; i < childs.length; i++) {
						fileList.add(childs[i]);
					}
				}
				fileListSize = fileList.size();
				if (incDirs == false) {						// exclude dirs
					continue;
				}
			}
			if (f.isFile()) {								// exclude files
				if (exFiles == true) {
					continue;
				}
			}
			if (ffilters == null) {
				found = true;
				break;
			}
			for (int i = 0; i < ffilters_size; i++) {
				FileFilter ff = (FileFilter) ffilters.get(i);
				if (ff.accept(f) == true) {
					found = true;
					break loop;
				}
			}
		}
		if (found == false) {
			return end();
		}
		return f;
	}


	/**
	 * Finishes the search and frees resources.
	 *
	 * @return always <code>null</code>: it means the search is over.
	 */
	private File end() {
		fileList = null;
		ffilters = null;
		return null;
	}
}

