/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.buckminster.aggregator.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.buckminster.aggregator.util.AggregatorAdapterFactory;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.edit.provider.ChangeNotifier;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IChangeNotifier;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;

/**
 * This is the factory that is used to provide the interfaces needed to support Viewers. The adapters generated by this
 * factory convert EMF adapter notifications into calls to {@link #fireNotifyChanged fireNotifyChanged}. The adapters
 * also support Eclipse property sheets. Note that most of the adapters are shared among multiple instances. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class AggregatorItemProviderAdapterFactory extends AggregatorAdapterFactory implements
		ComposeableAdapterFactory, IChangeNotifier, IDisposable
{
	/**
	 * This keeps track of the root adapter factory that delegates to this adapter factory. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected ComposedAdapterFactory parentAdapterFactory;

	/**
	 * This is used to implement {@link org.eclipse.emf.edit.provider.IChangeNotifier}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected IChangeNotifier changeNotifier = new ChangeNotifier();

	/**
	 * This keeps track of all the supported types checked by {@link #isFactoryForType isFactoryForType}. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Collection<Object> supportedTypes = new ArrayList<Object>();

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Aggregator} instances.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AggregatorItemProvider aggregatorItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.MappedRepository}
	 * instances. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected MappedRepositoryItemProvider mappedRepositoryItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Configuration}
	 * instances. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ConfigurationItemProvider configurationItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Contribution}
	 * instances. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ContributionItemProvider contributionItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Contact} instances.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ContactItemProvider contactItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Feature} instances.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected FeatureItemProvider featureItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Bundle} instances.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected BundleItemProvider bundleItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Product} instances.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ProductItemProvider productItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Property} instances.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected PropertyItemProvider propertyItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.Category} instances.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected CategoryItemProvider categoryItemProvider;

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.buckminster.aggregator.CustomCategory}
	 * instances. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected CustomCategoryItemProvider customCategoryItemProvider;

	/**
	 * This constructs an instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public AggregatorItemProviderAdapterFactory()
	{
		supportedTypes.add(IEditingDomainItemProvider.class);
		supportedTypes.add(IStructuredItemContentProvider.class);
		supportedTypes.add(ITreeItemContentProvider.class);
		supportedTypes.add(IItemLabelProvider.class);
		supportedTypes.add(IItemPropertySource.class);
	}

	/**
	 * This implementation substitutes the factory itself as the key for the adapter. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter adapt(Notifier notifier, Object type)
	{
		return super.adapt(notifier, this);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object adapt(Object object, Object type)
	{
		if(isFactoryForType(type))
		{
			Object adapter = super.adapt(object, type);
			if(!(type instanceof Class<?>) || (((Class<?>)type).isInstance(adapter)))
			{
				return adapter;
			}
		}

		return null;
	}

	/**
	 * This adds a listener. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void addListener(INotifyChangedListener notifyChangedListener)
	{
		changeNotifier.addListener(notifyChangedListener);
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Aggregator}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createAggregatorAdapter()
	{
		if(aggregatorItemProvider == null)
		{
			aggregatorItemProvider = new AggregatorItemProvider(this);
		}

		return aggregatorItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Bundle}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createBundleAdapter()
	{
		if(bundleItemProvider == null)
		{
			bundleItemProvider = new BundleItemProvider(this);
		}

		return bundleItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Category}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createCategoryAdapter()
	{
		if(categoryItemProvider == null)
		{
			categoryItemProvider = new CategoryItemProvider(this);
		}

		return categoryItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Configuration}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createConfigurationAdapter()
	{
		if(configurationItemProvider == null)
		{
			configurationItemProvider = new ConfigurationItemProvider(this);
		}

		return configurationItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Contact}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createContactAdapter()
	{
		if(contactItemProvider == null)
		{
			contactItemProvider = new ContactItemProvider(this);
		}

		return contactItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Contribution}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createContributionAdapter()
	{
		if(contributionItemProvider == null)
		{
			contributionItemProvider = new ContributionItemProvider(this);
		}

		return contributionItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.CustomCategory}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createCustomCategoryAdapter()
	{
		if(customCategoryItemProvider == null)
		{
			customCategoryItemProvider = new CustomCategoryItemProvider(this);
		}

		return customCategoryItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Feature}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createFeatureAdapter()
	{
		if(featureItemProvider == null)
		{
			featureItemProvider = new FeatureItemProvider(this);
		}

		return featureItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.MappedRepository}. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createMappedRepositoryAdapter()
	{
		if(mappedRepositoryItemProvider == null)
		{
			mappedRepositoryItemProvider = new MappedRepositoryItemProvider(this);
		}

		return mappedRepositoryItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Product}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createProductAdapter()
	{
		if(productItemProvider == null)
		{
			productItemProvider = new ProductItemProvider(this);
		}

		return productItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.aggregator.Property}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createPropertyAdapter()
	{
		if(propertyItemProvider == null)
		{
			propertyItemProvider = new PropertyItemProvider(this);
		}

		return propertyItemProvider;
	}

	/**
	 * This disposes all of the item providers created by this factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void dispose()
	{
		if(aggregatorItemProvider != null)
			aggregatorItemProvider.dispose();
		if(mappedRepositoryItemProvider != null)
			mappedRepositoryItemProvider.dispose();
		if(configurationItemProvider != null)
			configurationItemProvider.dispose();
		if(contributionItemProvider != null)
			contributionItemProvider.dispose();
		if(contactItemProvider != null)
			contactItemProvider.dispose();
		if(featureItemProvider != null)
			featureItemProvider.dispose();
		if(bundleItemProvider != null)
			bundleItemProvider.dispose();
		if(productItemProvider != null)
			productItemProvider.dispose();
		if(propertyItemProvider != null)
			propertyItemProvider.dispose();
		if(categoryItemProvider != null)
			categoryItemProvider.dispose();
		if(customCategoryItemProvider != null)
			customCategoryItemProvider.dispose();
	}

	/**
	 * This delegates to {@link #changeNotifier} and to {@link #parentAdapterFactory}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	public void fireNotifyChanged(Notification notification)
	{
		changeNotifier.fireNotifyChanged(notification);

		if(parentAdapterFactory != null)
		{
			parentAdapterFactory.fireNotifyChanged(notification);
		}
	}

	/**
	 * This returns the root adapter factory that contains this factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ComposeableAdapterFactory getRootAdapterFactory()
	{
		return parentAdapterFactory == null
				? this
				: parentAdapterFactory.getRootAdapterFactory();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object type)
	{
		return supportedTypes.contains(type) || super.isFactoryForType(type);
	}

	/**
	 * This removes a listener. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void removeListener(INotifyChangedListener notifyChangedListener)
	{
		changeNotifier.removeListener(notifyChangedListener);
	}

	/**
	 * This sets the composed adapter factory that contains this factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setParentAdapterFactory(ComposedAdapterFactory parentAdapterFactory)
	{
		this.parentAdapterFactory = parentAdapterFactory;
	}

}
