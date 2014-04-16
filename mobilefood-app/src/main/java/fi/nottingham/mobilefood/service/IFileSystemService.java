package fi.nottingham.mobilefood.service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

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
	OutputStream openOutputFile(String filename);
	
	/**
	 * @param filename
	 * @return stream for reading the file
	 * @throws FileNotFoundException
	 */
	InputStream openInputFile(String filename) throws FileNotFoundException;
}
