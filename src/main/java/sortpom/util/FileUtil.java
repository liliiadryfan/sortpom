package sortpom.util;

import java.io.*;
import java.net.*;

import org.apache.commons.io.*;
import org.apache.maven.plugin.*;

import sortpom.*;

/**
 * Used to interface with file system
 * 
 * @author Bjorn
 */
public class FileUtil {
	private static final String DEFAULT_SORT_ORDER_FILENAME = "default_0_4_0.xml";
	private File pomFile;
	private String backupFileExtension;
	private String encoding;
	private String defaultOrderFileName;

	/** Initializes the class with sortpom parameters. */
	public void setup(PluginParameters parameters) {
		this.pomFile = parameters.pomFile;
		this.backupFileExtension = parameters.backupFileExtension;
		this.encoding = parameters.encoding;
		this.defaultOrderFileName = parameters.sortOrderFile;
	}

	/**
	 * Saves a backup of the pomfile before sorting.
	 * 
	 * @throws MojoFailureException
	 */
	public void backupFile() throws MojoFailureException {
		final String newName = pomFile.getAbsolutePath() + backupFileExtension;
		final File backupFile = new File(newName);
		if (backupFile.exists() && !backupFile.delete()) {
			throw new MojoFailureException("Could not remove old backup file, filename: " + newName);
		}
		FileInputStream source = null;
		FileOutputStream newFile = null;
		try {
			source = new FileInputStream(pomFile);
			newFile = new FileOutputStream(backupFile);
			IOUtils.copy(source, newFile);
		} catch (IOException e) {
			throw new MojoFailureException("Could not create backup file to filename: " + newName, e);
		} finally {
			IOUtils.closeQuietly(newFile);
			IOUtils.closeQuietly(source);
		}
	}

	/**
	 * Retrieves the default sort order for sortpom
	 * 
	 * @return
	 */
	public String getDefaultSortOrderXml() {
		InputStream inputStream = null;
		try {
			if (defaultOrderFileName == null) {
				URL resource = this.getClass().getClassLoader().getResource(DEFAULT_SORT_ORDER_FILENAME);
				inputStream = resource.openStream();
			} else {
				inputStream = getFileFromRelativeOrClassPath();
			}
			return IOUtils.toString(inputStream, encoding);
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Loads the pomfile that will be sorted.
	 * 
	 * @return
	 * @throws MojoFailureException
	 */
	public String getPomFileContent() throws MojoFailureException {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(pomFile);
			return IOUtils.toString(inputStream, encoding);
		} catch (IOException ioex) {
			throw new MojoFailureException("Could not read pomfile: " + pomFile.getAbsolutePath(), ioex);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Saves sorted pomfile.
	 * 
	 * @param sortedXml
	 * @throws MojoFailureException
	 */
	public void savePomFile(final String sortedXml) throws MojoFailureException {
		FileOutputStream saveFile = null;
		try {
			saveFile = new FileOutputStream(pomFile);
			IOUtils.write(sortedXml, saveFile, encoding);
		} catch (IOException e) {
			throw new MojoFailureException("Could not save sorted pomfile: " + pomFile, e);
		} finally {
			IOUtils.closeQuietly(saveFile);
		}
	}

	private InputStream getFileFromRelativeOrClassPath() throws IOException {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(defaultOrderFileName);
		} catch (FileNotFoundException fnfex) {
			// try classpath
			try {
				URL resource = this.getClass().getClassLoader().getResource(defaultOrderFileName);
				if (resource == null) {
					throw new IOException("Cannot find resource");
				}
				inputStream = resource.openStream();
			} catch (IOException e1) {
				throw new FileNotFoundException(String.format("Could not find %s or %s in classpath", new File(
						defaultOrderFileName).getAbsolutePath(), defaultOrderFileName));
			}
		}
		return inputStream;
	}

	public byte[] getDefaultSortOrderXmlBytes() throws UnsupportedEncodingException {
		return getDefaultSortOrderXml().getBytes(encoding);
	}

}
