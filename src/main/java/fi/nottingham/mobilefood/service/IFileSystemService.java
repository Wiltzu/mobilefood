package fi.nottingham.mobilefood.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Ville
 *
 */
public interface IFileSystemService {
	
	/**
	 * New file is created if file with {@code filename} doesn't exist.
	 * 
	 * @param filename
	 * @return stream for writing to the file
	 */
	FileOutputStream openOutputFile(String filename);
	
	/**
	 * @param filename
	 * @return stream for reading the file
	 * @throws FileNotFoundException
	 */
	FileInputStream openInputFile(String filename) throws FileNotFoundException;
}
