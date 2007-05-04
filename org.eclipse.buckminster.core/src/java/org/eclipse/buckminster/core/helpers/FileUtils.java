/*******************************************************************************
 * Copyright (c) 2004, 2006
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.core.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.buckminster.core.CorePlugin;
import org.eclipse.buckminster.runtime.IOUtils;
import org.eclipse.buckminster.runtime.MonitorUtils;
import org.eclipse.buckminster.runtime.URLUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;

/**
 * @author Thomas Hallgren
 */
public abstract class FileUtils
{
	public static final String FILE_SEP = System.getProperty("file.separator");

	private static final Pattern[] s_defaultExcludes;

	static
	{
		ArrayList<Pattern> bld = new ArrayList<Pattern>();

		// Standard Apache ANT defaultexcludes
		//
		addPattern(bld, ".*/[^/]*~$");
		addPattern(bld, ".*/#[^/]*#$");
		addPattern(bld, ".*/\\.#[^/]*$");
		addPattern(bld, ".*/%_[^/]*$");
		addPattern(bld, ".*/CVS$");
		addPattern(bld, ".*/CVS/.*");
		addPattern(bld, ".*/\\.cvsignore$");
		addPattern(bld, ".*/SCCS$");
		addPattern(bld, ".*/SCCS/.*");
		addPattern(bld, ".*/vssver\\.scc$");
		addPattern(bld, ".*/\\.svn$");
		addPattern(bld, ".*/\\.svn/.*");
		addPattern(bld, ".*/\\.DS_Store$");
		s_defaultExcludes = bld.toArray(new Pattern[bld.size()]);
	}

	public static class CopyOntoSelfException extends LocalizedException
	{
		private static final long serialVersionUID = -1533727619065375203L;

		private final String m_source;

		private final String m_destination;

		public CopyOntoSelfException(File source, File destination)
		{
			super("Cannot copy {0} to {1} since the destination is equal to, or contained in, the source");
			m_source = source.toString();
			m_destination = destination.toString();
			this.assignMessage();
		}

		@Override
		protected String[] getArguments()
		{
			return new String[] { m_source, m_destination };
		}
	}

	/**
	 * Perform an OS sensitive equality comparison between two paths. This is very different from
	 * the {@link IPath#equals(Object)} since that method is case sensitive on all platforms.
	 * @param a
	 * @param b
	 * @return <code>true</code> if both paths are equal or both paths are <code>null</code>.
	 */
	public static boolean pathEquals(IPath a, IPath b)
	{
		return (a == null || b == null) ? a == b : a.toFile().equals(b.toFile());
	}

	public static class DestinationNotEmptyException extends LocalizedException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5459338589093100050L;

		private final String m_destination;

		public DestinationNotEmptyException(File destination)
		{
			super("Unable to use {0} as a destination for copy since it is not empty");
			m_destination = destination.toString();
			this.assignMessage();
		}

		@Override
		protected String[] getArguments()
		{
			return new String[] { m_destination };
		}
	}

	public static class DeleteException extends LocalizedException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7937022594722296252L;

		private final String m_file;

		public DeleteException(File file)
		{
			this(file, null);
		}

		public DeleteException(File file, Throwable e)
		{
			super("Unable to delete {0}", e);
			m_file = file.toString();
			this.assignMessage();
		}

		@Override
		protected String[] getArguments()
		{
			return new String[] { m_file };
		}
	}

	public static class MkdirException extends LocalizedException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2249707149761032461L;

		private final String m_directory;

		public MkdirException(File directory)
		{
			this(directory, null);
		}

		public MkdirException(File directory, Throwable e)
		{
			super("Unable to create directory {0}", e);
			m_directory = directory.toString();
			this.assignMessage();
		}

		@Override
		protected String[] getArguments()
		{
			return new String[] { m_directory };
		}
	}

	/**
	 * This method will assert that the <code>source</code> is not present inside the
	 * <code>destination</code> and then call
	 * {@link #prepareDestination(File destination, boolean overwrite)}.
	 * @param source The source. Might be a file or a directory.
	 * @param destination The destination directory.
	 * @param overwrite <code>true</code> if the <code>destination</code> can be cleared prior
	 *            to copy.
	 * @throws BuckminsterException
	 */
	public static void checkCopyConditions(File sourceFile, File destination, boolean overwrite,
		IProgressMonitor monitor) throws BuckminsterException
	{
		// Assert that the destination is different from source and not a
		// subdirectory inside
		// of the source.
		//
		File tmp = destination;
		while(tmp != null)
		{
			if(tmp.equals(sourceFile))
				throw new CopyOntoSelfException(sourceFile, destination);
			tmp = tmp.getParentFile();
		}
		prepareDestination(destination, overwrite, monitor);
	}

	/**
	 * This method will make sure that the <code>destination</code> points to an empty directory.
	 * If <code>overwrite</code> is <code>true</code> and if <code>destination</code> is a
	 * directory, any existing files in the <code>destination</code> directory will be removed. If
	 * <code>overwrite</code> is <code>true</code> and <code>destination</code> is a file, it
	 * will be removed and a directory will be created in its place. An
	 * <code>DestinationNotEmptyException</code> will be thrown if <code>overwrite</code> is
	 * <code>false</code> and the <code>destination</code> exists and is not an empty directory.
	 * An attempt will be made to create the <code>destination</code> directory and needed parent
	 * directories if <code>destination</code> points to a non-existing directory.
	 * @param source The source. Might be a file or a directory.
	 * @param destination The destination directory.
	 * @param overwrite <code>true</code> if the <code>destination</code> can be cleared prior
	 *            to copy.
	 * @throws BuckminsterException
	 */
	public static void prepareDestination(File destination, boolean overwrite, IProgressMonitor monitor)
	throws BuckminsterException
	{
		if(destination.exists())
		{
			monitor.beginTask(null, 200);
			boolean isDirectory = destination.isDirectory();
			MonitorUtils.worked(monitor, 10);

			if(isDirectory)
			{
				File[] list = destination.listFiles();
				MonitorUtils.worked(monitor, 30);
				int numFiles = list.length;
				if(numFiles > 0)
				{
					IProgressMonitor subMonitor = MonitorUtils.subMonitor(monitor, 160);
					subMonitor.beginTask(null, numFiles * 100);
					if(!overwrite)
						throw new FileUtils.DestinationNotEmptyException(destination);
					for(File file : list)
						FileUtils.deleteRecursive(file, MonitorUtils.subMonitor(subMonitor, 100));
				}
				else
					MonitorUtils.worked(monitor, 160);
			}
			else
			{
				if(!overwrite)
					throw new FileUtils.DestinationNotEmptyException(destination);
				FileUtils.deleteRecursive(destination, MonitorUtils.subMonitor(monitor, 190));
			}
		}
		else
			FileUtils.createDirectory(destination, monitor);
	}

	/**
	 * Copy everything found in the <code>sourceDirectory</code> to the
	 * <code>destinationDirectory</code>. The latter is created if it does not exist. If it
	 * exists, and if <code>overwrite</code> is <code>true</code> then the destination will be
	 * cleaned out and recreated. If <code>overwrite</code> is <code>false</code>, this method
	 * will throw a {@link FileUtils.DestinationNotEmptyException}.
	 * @param sourceDirectory The source directory for the copy
	 * @param destinationDirectory The destination directory for the copy.
	 * @param overwrite Set to true if an existing destination should be cleaned out prior to
	 *            copying.
	 * @throws CoreException
	 */
	public static void deepCopy(File sourceDirectory, File destinationDirectory, boolean overwrite,
		IProgressMonitor monitor) throws CoreException
	{
		monitor.beginTask(null, 1000);
		try
		{
			checkCopyConditions(sourceDirectory, destinationDirectory, overwrite, MonitorUtils.subMonitor(
				monitor, 100));
			deepCopyUnchecked(sourceDirectory, destinationDirectory, MonitorUtils.subMonitor(monitor, 900));
		}
		finally
		{
			monitor.done();
		}
	}

	/**
	 * Unzip the <code>source</code> contents to a <code>destDir</code> directory and give it
	 * the name <code>destName</code>. This mehtod assumes that the source is the URL that points
	 * to zipped contents, that destDir is a directory, and that a file named destName can be
	 * created in destDir. If such a file exists already, an attempt will be made to overwrite.
	 * @param source The source zipped content.
	 * @param sourceRelPath Relative path to material inside the soruce file.
	 * @param destDir The destination directory.
	 * @param destName The name of the file relative to the destination.
	 * @param monitor The progress monitor used during the operation
	 * @throws BuckminsterException
	 */
	public static void unzip(URL source, String sourceRelPath, File dest, boolean overwrite,
		IProgressMonitor monitor) throws CoreException
	{
		monitor.beginTask(null, 1000);
		InputStream input = null;
		try
		{
			input = new BufferedInputStream(CorePlugin.getDefault().openCachedURL(source,
				MonitorUtils.subMonitor(monitor, 400)));
			unzip(input, sourceRelPath, dest, overwrite, MonitorUtils.subMonitor(monitor, 600));
		}
		catch(IOException e)
		{
			throw BuckminsterException.wrap(e);
		}
		finally
		{
			monitor.done();
			IOUtils.close(input);
		}
	}

	public static void unzip(InputStream inputs, String sourceRelPath, File dest, boolean overwrite,
		IProgressMonitor monitor) throws CoreException
	{
		ZipEntry entry;
		ZipInputStream input = null;
		monitor.beginTask(null, 600);
		IProgressMonitor nullMon = null;
		FileUtils.prepareDestination(dest, overwrite, MonitorUtils.subMonitor(monitor, 100));
		try
		{
			int ticksLeft = 500;
			input = new ZipInputStream(inputs);

			while((entry = input.getNextEntry()) != null)
			{
				String name = entry.getName();
				if(sourceRelPath != null)
				{
					if(!name.startsWith(sourceRelPath))
						continue;
					name = name.substring(sourceRelPath.length() + 1);
					if(name.length() == 0)
						continue;
				}

				IProgressMonitor subMonitor;
				if(ticksLeft > 0)
				{
					subMonitor = MonitorUtils.subMonitor(monitor, 10);
					ticksLeft -= 10;
				}
				else
				{
					if(nullMon == null)
						nullMon = new NullProgressMonitor();
					subMonitor = nullMon;
				}

				if(entry.isDirectory())
				{
					FileUtils.createDirectory(new File(dest, name), subMonitor);
					continue;
				}
				copyFile(input, dest, name, subMonitor);
			}
			if(ticksLeft > 0)
				MonitorUtils.worked(monitor, ticksLeft);
		}
		catch(IOException e)
		{
			throw BuckminsterException.wrap(e);
		}
		finally
		{
			monitor.done();
		}
	}

	/**
	 * Copy the <code>source</code> file to a <code>destDir</code> directory and give it the
	 * name <code>destName</code>. This mehtod assumes that the source is a common file, that
	 * destDir is a directory, and that a file named destName can be created in destDir. If such a
	 * file exists already, an attempt will be made to overwrite.
	 * @param source The source. Cannot be a directory.
	 * @param destDir The destination directory.
	 * @param destName The name of the file relative to the destination.
	 * @throws IOException, MkdirException
	 * @return The total number of bytes copied
	 */
	public static long copyFile(File source, File destDir, String destName, IProgressMonitor monitor)
	throws CoreException
	{
		InputStream input = null;
		try
		{
			input = new FileInputStream(source);
			return copyFile(input, destDir, destName, monitor);
		}
		catch(IOException e)
		{
			throw BuckminsterException.wrap(e);
		}
		finally
		{
			IOUtils.close(input);
		}
	}

	/**
	 * Copy the <code>source</code> contents to a <code>destDir</code> directory and give it the
	 * name <code>destName</code>. This method assumes that source is not a catalog, that destDir
	 * is a directory, and that a file named destName can be created in destDir. If such a file
	 * exists already, an attempt will be made to overwrite.
	 * @param source The source. Cannot be a directory.
	 * @param destDir The destination directory.
	 * @param destName The name of the file relative to the destination.
	 * @throws IOException, MkdirException
	 * @return The total number of bytes copied
	 */
	public static long copyFile(URL source, File destDir, String destName, IProgressMonitor monitor)
	throws CoreException
	{
		InputStream input = null;
		try
		{
			input = URLUtils.openStream(source, null);
			return copyFile(input, destDir, destName, monitor);
		}
		catch(IOException e)
		{
			throw BuckminsterException.wrap(e);
		}
		finally
		{
			IOUtils.close(input);
		}
	}

	public static long copyFile(InputStream input, File destDir, String destName, IProgressMonitor monitor)
	throws CoreException
	{
		OutputStream output = null;
		monitor = MonitorUtils.ensureNotNull(monitor);
		monitor.beginTask(null, 1000);
		try
		{
			File destFile = new File(destDir, destName);
			destDir = destFile.getParentFile(); // destName might have many
			// components
			if(destDir != null && !destDir.exists())
				createDirectory(destDir, MonitorUtils.subMonitor(monitor, 100));
			MonitorUtils.worked(monitor, 100);

			output = new FileOutputStream(destFile);
			MonitorUtils.worked(monitor, 100);
			return copyFile(input, output, MonitorUtils.subMonitor(monitor, 700));
		}
		catch(IOException e)
		{
			throw BuckminsterException.wrap(e);
		}
		finally
		{
			IOUtils.close(output);
			monitor.done();
		}
	}

	public static long copyFile(InputStream input, OutputStream output, IProgressMonitor monitor)
	throws IOException
	{
		return copyFile(input, output, new byte[0x2000], monitor);
	}

	public static long copyFile(InputStream input, OutputStream output, byte[] buf, IProgressMonitor monitor)
	throws IOException
	{
		long total = 0;
		monitor = MonitorUtils.ensureNotNull(monitor);
		try
		{
			monitor.beginTask(null, IProgressMonitor.UNKNOWN);
			int count;
			while((count = input.read(buf)) > 0)
			{
				MonitorUtils.worked(monitor, 1);
				output.write(buf, 0, count);
				total += count;
			}
			return total;
		}
		finally
		{
			monitor.done();
		}
	}

	public static long getCRC(InputStream input) throws IOException
	{
		CRC32 crc32 = new CRC32();
		byte[] copyBuffer = new byte[0x2000]; // 8 kilobyte buffer.
		int count;
		while((count = input.read(copyBuffer)) > 0)
			crc32.update(copyBuffer, 0, count);
		return crc32.getValue();
	}

	public static long getCRCAndCopy(InputStream input, OutputStream output) throws IOException
	{
		CRC32 crc32 = new CRC32();
		byte[] copyBuffer = new byte[0x2000]; // 8 kilobyte buffer.
		int count;
		while((count = input.read(copyBuffer)) > 0)
		{
			output.write(copyBuffer, 0, count);
			crc32.update(copyBuffer, 0, count);
		}
		return crc32.getValue();
	}

	public static void createDirectory(File file, IProgressMonitor monitor) throws MkdirException
	{
		monitor = MonitorUtils.ensureNotNull(monitor);

		monitor.beginTask(null, 1);
		try
		{
			if(!(file.isDirectory() || file.mkdirs()))
				throw new MkdirException(file);
			MonitorUtils.worked(monitor, 1);
		}
		catch(SecurityException e)
		{
			throw new MkdirException(file, e);
		}
		finally
		{
			monitor.done();
		}
	}

	public static void deleteRecursive(File file, IProgressMonitor monitor) throws DeleteException
	{
		try
		{
			monitor.beginTask(null, IProgressMonitor.UNKNOWN);
			File[] list = file.listFiles();
			if(list != null)
			{
				if(s_foldersToRemove != null)
					s_foldersToRemove.remove(file);

				for(File entry : list)
					deleteRecursive(entry, MonitorUtils.subMonitor(monitor, 1));
			}
			if(!file.delete() && file.exists())
				throw new DeleteException(file);
			MonitorUtils.worked(monitor, 1);
		}
		catch(SecurityException e)
		{
			throw new DeleteException(file, e);
		}
		finally
		{
			monitor.done();
		}
	}

	public static IPath pathForLocation(IPath location)
	{
		if(Platform.getLocation().equals(location))
			return Path.ROOT;

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(int i = 0; i < projects.length; i++)
		{
			IProject project = projects[i];
			IPath projectLocation = project.getLocation();
			if(projectLocation != null && projectLocation.isPrefixOf(location))
			{
				int segmentsToRemove = projectLocation.segmentCount();
				return project.getFullPath().append(location.removeFirstSegments(segmentsToRemove));
			}
		}
		return null;
	}

	/**
	 * Creates the given folder and any needed but nonexistent parent folders.
	 * @param container
	 * @throws CoreException
	 */
	public static void createFolder(IContainer container) throws CoreException
	{
		IProgressMonitor nullMonitor = new NullProgressMonitor();
		if(container instanceof IFolder)
		{
			IFolder folder = (IFolder)container;
			createFolder(folder.getParent());
			folder.refreshLocal(IResource.DEPTH_ZERO, nullMonitor);
			if(!folder.exists())
				folder.create(false, true, new NullProgressMonitor());
		}
	}

	private static boolean isMatch(String fileStr, Pattern[] patterns, boolean whenEmpty)
	{
		if(patterns != null)
		{
			int idx = patterns.length;
			if(idx > 0)
			{
				while(--idx >= 0)
					if(patterns[idx].matcher(fileStr).matches())
						return true;
				return false;
			}
		}
		return whenEmpty;
	}

	private static void addPattern(ArrayList<Pattern> bld, String expr)
	{
		bld.add(Pattern.compile(expr));
	}

	public static void deepCopyUnchecked(File source, File dest, IProgressMonitor monitor)
	throws CoreException
	{
		deepCopyUnchecked(source, dest, null, s_defaultExcludes, monitor);
	}

	public static void deepCopyUnchecked(File source, File dest, Pattern[] includes, Pattern[] excludes,
		IProgressMonitor monitor) throws CoreException
	{
		String sourceStr = source.toString().replace('\\', '/');
		boolean isDir = source.isDirectory();
		if(isDir && sourceStr.charAt(sourceStr.length() - 1) != '/')
			sourceStr += '/';

		if(!isMatch(sourceStr, includes, true) || isMatch(sourceStr, excludes, false))
		{
			MonitorUtils.complete(monitor);
			return;
		}

		File[] files = source.listFiles();
		if(files == null || files.length == 0)
		{
			// Reason might be that the files is in fact a file or
			// an empty directory. Both are ok. A missing entry is
			// not OK.
			//
			if(isDir)
				MonitorUtils.complete(monitor);
			else if(source.isFile())
				copyFile(source, dest, source.getName(), monitor);
			else
				throw BuckminsterException.wrap(new FileNotFoundException(source.getAbsolutePath()));
			return;
		}

		monitor.beginTask(null, 10 + files.length * 100);
		try
		{
			createDirectory(dest, MonitorUtils.subMonitor(monitor, 10));
			for(File file : files)
			{
				IProgressMonitor subMonitor = MonitorUtils.subMonitor(monitor, 100);
				if(file.isFile())
				{
					sourceStr = source.toString().replace('\\', '/');
					if(isMatch(sourceStr, includes, true) && !isMatch(sourceStr, excludes, false))
						copyFile(file, dest, file.getName(), subMonitor);
					else
						MonitorUtils.complete(subMonitor);
				}
				else
					deepCopyUnchecked(file, new File(dest, file.getName()), includes, excludes, subMonitor);
			}
		}
		finally
		{
			monitor.done();
		}
	}

	public static boolean compareContents(InputStream a, InputStream b) throws IOException
	{
		byte[] bufA = new byte[0x400];
		byte[] bufB = new byte[0x400];
		int countA;
		while((countA = a.read(bufA)) > 0)
		{
			if(b.read(bufB) != countA)
				return false;
			if(!ArrayUtils.equals(bufA, bufB, 0, countA))
				return false;
		}
		return b.read(bufB) == countA;
	}

	private static HashSet<File> s_foldersToRemove;

	/**
	 * Creates a folder based on a generated name. The folder and all its content will be deleted
	 * when the process exists.
	 */
	public static synchronized File createTempFolder(String prefix, String suffix) throws CoreException
	{
		File tmpFile;
		try
		{
			tmpFile = File.createTempFile(prefix, suffix);
		}
		catch(IOException e)
		{
			throw BuckminsterException.wrap(e);
		}
		if(!tmpFile.delete())
			throw new MkdirException(tmpFile);

		createTempFolder(tmpFile);
		return tmpFile;
	}

	/**
	 * Creates a folder based on an abstract file handle. The folder and all its content will be
	 * deleted when the process exists. The <code>tmpDir</code> directory must not exist when this
	 * method is called.
	 * @param tmpDir The directory to create
	 * @throws MkdirException If the directory could not be created.
	 */
	public static synchronized void createTempFolder(File tmpDir) throws MkdirException
	{
		if(!tmpDir.mkdirs())
			throw new MkdirException(tmpDir);

		if(s_foldersToRemove == null)
		{
			s_foldersToRemove = new HashSet<File>();
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					NullProgressMonitor nullMonitor = new NullProgressMonitor();

					// Prevent that s_foldersToRemove is updated during the remove
					//
					HashSet<File> folders = s_foldersToRemove;
					s_foldersToRemove = null;
					for(File folder : folders)
					{
						try
						{
							deleteRecursive(folder, nullMonitor);
						}
						catch(Exception e)
						{
							// We're shutting down so this is ignored
							System.err.println("Failed to remove directory " + folder + " :" + e.toString());
						}
					}
				}
			});
		}
		s_foldersToRemove.add(tmpDir);
	}

	/**
	 * Helper method to calculate a digest for a file or a tree
	 */
	static public byte[] calculateDigest(File f, String algorithm, IProgressMonitor monitor)
	throws NoSuchAlgorithmException,
		IOException
	{
		MessageDigest md = MessageDigest.getInstance(algorithm);
		DigestOutputStream dos = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		if(f.isFile())
		{
			FileInputStream fis = new FileInputStream(f);
			FileUtils.copyFile(fis, dos, monitor);
			fis.close();
		}
		else
			FileUtils.deepCalculateDigest(f, dos, f.getCanonicalPath().length() + 1, monitor);
		dos.close();
		return md.digest();
	}

	/**
	 * Returns the timestamp of the file or 0L in case a directory is appointed since we in that
	 * case really don't know if something might be missing.
	 * @param The file or directory to check.
	 * @return The file modification time or 0L if the given file is not found or if it is a
	 *         directory.
	 */
	public static long getFirstModified(File file)
	{
		return file.isFile() ? file.lastModified() : 0L;
	}

	/**
	 * Returns the timestamp of the last modified file. If fileOrDir is a file, the timestamp will
	 * be equal to the lastModified time of that file. If fileOrDir is a directory, this method will
	 * recurse into itself with each file or directory found there. The search stops if a file is
	 * encountered that has a timestamp greater or equal to <code>threshold</code>.
	 * @param The file or directory to check.
	 * @param threshold Stop if a file is found that is newer then threshold
	 * @return The last modification time found on a file. If no file is found, this method returns
	 *         0L.
	 */
	public static long getLastModified(File fileOrDir, long threshold)
	{
		if(threshold == 0)
			return 0;

		long lastModTime = 0;
		if(fileOrDir.isDirectory())
		{
			for(String p : fileOrDir.list())
			{
				long modTime = getLastModified(new File(fileOrDir, p), threshold);
				if(modTime > lastModTime)
				{
					lastModTime = modTime;
					if(modTime >= threshold)
						break;
				}
			}
		}
		else
			lastModTime = fileOrDir.lastModified();
		return lastModTime;
	}

	/**
	 * internal helper method to read all files/dirs in a directory tree to a *single* outstream
	 */
	private static void deepCalculateDigest(File from, OutputStream os, int rootOffset,
		IProgressMonitor monitor) throws IOException
	{
		// get the file list, but *always* sort it to ensure we
		// always process things in the same order as this is important
		// for always getting the same digest
		//
		File names[] = from.listFiles();
		monitor.beginTask(null, names.length * 100);
		try
		{
			Arrays.sort(names);
			for(int i = 0; i < names.length; i++)
			{
				File p = names[i];
				//
				// Make pathnames count for everything we encounter to ensure
				// changes in dir structure also count (new dirs, empty files,
				// changed names)
				// replace the platform specific separator with something
				// else...we should then
				// get identical results on any platform.
				//
				IProgressMonitor subMonitor = MonitorUtils.subMonitor(monitor, 100);
				os.write(p.getName().getBytes());
				if(p.isDirectory())
				{
					os.write(1);
					deepCalculateDigest(p, os, rootOffset, subMonitor);
				}
				else
				{
					FileInputStream fis = new FileInputStream(p);
					copyFile(fis, os, subMonitor);
					fis.close();
				}
			}
		}
		finally
		{
			monitor.done();
		}
	}

	public static File findPath(String pluginId, String path) throws CoreException
	{
		return findPath(Platform.getBundle(pluginId), path);
	}

	public static File findPath(Plugin plugin, String path) throws CoreException
	{
		return findPath(plugin.getBundle(), path);
	}

	public static File findPath(Bundle bundle, String path) throws CoreException
	{
		try
		{
			return path == null ? null : new File(FileLocator.toFileURL(
				FileLocator.find(bundle, new Path(path), null)).toURI());
		}
		catch(Exception e)
		{
			throw BuckminsterException.wrap(e);
		}
	}


	public static IPath getFileAsPath(URL url)
	{
		File file = getFile(url);
		return file == null ? null : new Path(file.toString());
	}

	public static File getFile(URL url)
	{
		if(url == null)
			return null;

		String proto = url.getProtocol();
		if(proto == null || "file".equalsIgnoreCase(proto))
		{
			try
			{
				return new File(url.toURI());
			}
			catch(URISyntaxException e)
			{
				// This is probably due to spaces in the URL path. If it
				// is, the URL is per definition corrupt but let's use that
				// path verbatim anyway
				// http://bugs.eclipse.org/bugs/show_bug.cgi?id=125967
				//
				String path = url.getPath();
				if(path.indexOf(' ') >= 0)
					return new File(path);

				// Nope, that was not the problem!
				//
				CorePlugin.getLogger().warning(e.getMessage(), e);
			}
			catch(IllegalArgumentException e)
			{
				// Probably because the URI has a fragement component
			}
		}
		return null;
	}

}
