/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.buckminster.aggregator;

import org.eclipse.emf.common.EMFPlugin;

import org.eclipse.emf.common.util.ResourceLocator;

/**
 * This is the central singleton for the Aggregator model plugin. <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public final class AggregatorPlugin extends EMFPlugin
{
	/**
	 * The actual implementation of the Eclipse <b>Plugin</b>. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static class Implementation extends EclipsePlugin
	{
		/**
		 * Creates an instance. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		public Implementation()
		{
			super();

			// Remember the static instance.
			//
			plugin = this;
		}
	}

	/**
	 * Keep track of the singleton. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static final AggregatorPlugin INSTANCE = new AggregatorPlugin();

	/**
	 * Keep track of the singleton. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static Implementation plugin;

	/**
	 * Returns the singleton instance of the Eclipse plugin. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the singleton instance.
	 * @generated
	 */
	public static Implementation getPlugin()
	{
		return plugin;
	}

	/**
	 * Create the instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public AggregatorPlugin()
	{
		super(new ResourceLocator[] {});
	}

	/**
	 * Returns the singleton instance of the Eclipse plugin. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the singleton instance.
	 * @generated
	 */
	@Override
	public ResourceLocator getPluginResourceLocator()
	{
		return plugin;
	}

}
