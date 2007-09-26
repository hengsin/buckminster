/*******************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.jnlp;

import static org.eclipse.buckminster.jnlp.MaterializationConstants.*;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.buckminster.jnlp.accountservice.IAuthenticator;
import org.eclipse.buckminster.runtime.BuckminsterException;

/**
 * @author Karel Brezina
 *
 */
public class MaterializationUtils
{

	/**
	 * Checks HTTP response code and throws JNLPException if there is a problem
	 * 
	 * @param connection
	 * @throws JNLPException
	 * @throws IOException
	 */
	public static void checkConnection(int status, String originalURL) throws JNLPException, IOException
	{
		if(status != HttpStatus.SC_OK)
		{
			String errorCode;

			switch(status)
			{
			case HttpStatus.SC_FORBIDDEN:

				errorCode = ERROR_CODE_403_EXCEPTION;
				break;

			case HttpStatus.SC_NOT_FOUND:

				errorCode = ERROR_CODE_404_EXCEPTION;
				break;

			case HttpStatus.SC_INTERNAL_SERVER_ERROR:

				errorCode = ERROR_CODE_500_EXCEPTION;
				break;

			default:
				errorCode = ERROR_CODE_REMOTE_IO_EXCEPTION;
				break;
			}

			throw new JNLPException("Cannot read materialization specification", errorCode, new BuckminsterException(
					originalURL + " - " + HttpStatus.getStatusText(status)));

		}
	}
	
	/**
	 * Checks response of IAuthenticator.register method
	 * 
	 * @param result result of IAuthenticator.register method
	 * @throws JNLPException
	 */
	
	public static void checkRegistrationResponse(int result) throws JNLPException
	{
		switch(result)
		{
		case IAuthenticator.REGISTER_FAIL:
			throw new JNLPException("Registration was not successful", null);
		case IAuthenticator.REGISTER_LOGIN_EXISTS:
			throw new JNLPException("Login name already exists - choose a different one", null);
		case IAuthenticator.REGISTER_LOGIN_TOO_SHORT:
			throw new JNLPException("Login is too short - length must be between 3 and 25", null);
		case IAuthenticator.REGISTER_PASSWORD_TOO_SHORT:
			throw new JNLPException("Password is too short - length must be between 4 and 25", null);
		case IAuthenticator.REGISTER_EMAIL_FORMAT_ERROR:
			throw new JNLPException("Email does not have standard format", null);
		}
	}
}
