/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.runtime;

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

/**
 * This logger will disptach all messages to two destinations; the eclipse logger and the console.
 * Depending on the settings, the message might be dispatched to none, one, or both destinations.
 * The logger can also be made to dispatch console messages through the eclipse logger. It does that
 * by adding an ILogListener to the platform that will dispatch all messages to standard out.
 * @author Thomas Hallgren
 */
public class Logger
{
	public static final int SILENT = IStatus.CANCEL; // We use this constant to avoid collisions

	public static final int DEBUG = IStatus.OK;

	public static final int ERROR = IStatus.ERROR;

	public static final int INFO = IStatus.INFO;

	public static final int WARNING = IStatus.WARNING;

	// Magic used as the bundle specific code in log entries. Can be used
	// for filtering (although not in the Eclipse log viewer since it
	// doesn't support filtering on plugin specific code yet)
	//
	private static final int MAGIC = 293;

	private static Logger s_defaultLogger;

	private static int s_consoleThreshold = IBuckminsterPreferenceConstants.LOG_LEVEL_CONSOLE_DEFAULT;

	private static int s_eclipseLoggerThreshold = IBuckminsterPreferenceConstants.LOG_LEVEL_ECLIPSE_LOGGER_DEFAULT;

	private static ILogListener s_eclipseLogListener;

	private static class EclipseLogListener implements ILogListener
	{
		public void logging(IStatus status, String plugin)
		{
			int severity = status.getSeverity();
			if(severity >= s_consoleThreshold)
			{
				PrintStream out;
				switch(severity)
				{
				case IStatus.ERROR:
				case IStatus.WARNING:
					out = System.err;
					break;
				default:
					out = System.out;
				}
				out.println(status.getMessage());
				out.flush();
			}
		}
	};

	public static Logger getDefault()
	{
		return s_defaultLogger;
	}

	public static void setConsoleLevelThreshold(int threshold)
	{
		s_consoleThreshold = threshold;
	}

	public static void setEclipseLoggerLevelThreshold(int threshold)
	{
		s_eclipseLoggerThreshold = threshold;
	}

	public static synchronized void setEclipseLoggerToConsole(boolean flag)
	{
		if(flag)
		{
			if(s_eclipseLogListener == null)
			{
				s_eclipseLogListener = new EclipseLogListener();
				Platform.addLogListener(s_eclipseLogListener);
			}
		}
		else
		{
			if(s_eclipseLogListener != null)
			{
				Platform.removeLogListener(s_eclipseLogListener);
				s_eclipseLogListener = null;
			}
		}
	}

	static void setDefaultLogger(Bundle bundle)
	{
		s_defaultLogger = new Logger(bundle);
	}

	private final ILog m_log;

	public static final String BUILDER_LOG_RECEIVER_POINT = Buckminster.PLUGIN_ID + ".logReceivers";

	public Logger(Bundle bundle)
	{
		if(bundle == null)
			throw new IllegalArgumentException("The bundle for a logger cannot be null");
		m_log = Platform.getLog(bundle);
	}

	public Logger(ILog log)
	{
		m_log = log;
	}

	public Logger(String bundleId)
	{
		this(Platform.getBundle(bundleId));
	}

	public void debug(String msg, Object ...args)
	{
		this.log(DEBUG, msg, args);
	}

	public void debug(Throwable t, String msg, Object ...args)
	{
		this.log(DEBUG, t, msg, args);
	}

	public void error(String msg, Object ...args)
	{
		this.log(ERROR, msg, args);
	}

	public void error(Throwable t, String msg, Object ...args)
	{
		this.log(ERROR, t, msg, args);
	}

	public void info(String msg, Object ...args)
	{
		this.log(INFO, msg, args);
	}

	public void info(Throwable t, String msg, Object ...args)
	{
		this.log(INFO, t, msg, args);
	}

	public boolean isInfoEnabled()
	{
		return s_consoleThreshold <= INFO || s_eclipseLoggerThreshold <= INFO;
	}

	public boolean isDebugEnabled()
	{
		return s_consoleThreshold <= DEBUG || s_eclipseLoggerThreshold <= DEBUG;
	}

	public void log(int level, String msg, Object ...args)
	{
		this.log(level, null, msg, args);
	}

	private static PrintStream s_errStream;

	private static PrintStream s_outStream;

	static
	{
		setOutStream(getLoggerStream(false));		
		setErrStream(getLoggerStream(true));		
	}

	public synchronized void log(int level, Throwable t, String msg, Object ...args)
	{
		if(level >= s_consoleThreshold && (s_eclipseLogListener == null || level < s_eclipseLoggerThreshold))
		{
			PrintStream logStream = (level == WARNING || level == ERROR) ? s_errStream : s_outStream;
			logStream.format(msg, args);
			if(t != null && level == DEBUG)
				t.printStackTrace(logStream);
			logStream.flush();
		}
		if(level >= s_eclipseLoggerThreshold)
			m_log.log(new Status(level, m_log.getBundle().getSymbolicName(), MAGIC, String.format(msg, args), t));
	}

	public void warning(String msg, Object ...args)
	{
		this.log(WARNING, msg, args);
	}

	public void warning(Throwable t, String msg, Object ...args)
	{
		this.log(WARNING, t, msg, args);
	}

	public static PrintStream getOutStream()
	{
		return s_outStream;
	}

	public static PrintStream getErrStream()
	{
		return s_errStream;
	}

	private static PrintStream getLoggerStream(boolean errorStream)
	{
		// collect all implementors of a builder log receiver and hook them all
		// up in a tee
		//
		PrintStream sysStream = errorStream ? System.err : System.out;
		if(Buckminster.isHeadless())
			return sysStream;

		IExtensionRegistry ier = Platform.getExtensionRegistry();
		IConfigurationElement[] elems = ier.getConfigurationElementsFor(BUILDER_LOG_RECEIVER_POINT);
		int idx = elems.length;
		if(idx == 0)
			return sysStream;

		try
		{
			OutputStream[] streams = new OutputStream[idx+1];
			streams[idx] = sysStream;
			while(--idx >= 0)
			{
				ILogReceiver receiver = (ILogReceiver)elems[idx].createExecutableExtension("class");
				streams[idx] = receiver.start("Buckminster log", "org.eclipse.ui.MessageConsole", true, errorStream);
			}
			return new PrintStream(new MultiTeeOutputStream(streams), true);
		}
		catch(Throwable t)
		{
			t.printStackTrace(System.err);
			return sysStream;
		}
	}

	public static void setOutStream(PrintStream out)
	{
		s_outStream = out;
	}

	public static void setErrStream(PrintStream err)
	{
		s_errStream = err;
	}
}
