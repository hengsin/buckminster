/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.buckminster.cspec.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.buckminster.cspec.util.CspecAdapterFactory;

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
 * This is the factory that is used to provide the interfaces needed to support
 * Viewers. The adapters generated by this factory convert EMF adapter
 * notifications into calls to {@link #fireNotifyChanged fireNotifyChanged}. The
 * adapters also support Eclipse property sheets. Note that most of the adapters
 * are shared among multiple instances. <!-- begin-user-doc --> <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class CspecItemProviderAdapterFactory extends CspecAdapterFactory implements ComposeableAdapterFactory, IChangeNotifier, IDisposable {
	/**
	 * This keeps track of the root adapter factory that delegates to this
	 * adapter factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ComposedAdapterFactory parentAdapterFactory;

	/**
	 * This is used to implement
	 * {@link org.eclipse.emf.edit.provider.IChangeNotifier}. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected IChangeNotifier changeNotifier = new ChangeNotifier();

	/**
	 * This keeps track of all the supported types checked by
	 * {@link #isFactoryForType isFactoryForType}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected Collection<Object> supportedTypes = new ArrayList<Object>();

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.CSpec} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected CSpecItemProvider cSpecItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.Group} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected GroupItemProvider groupItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.Prerequisite} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected PrerequisiteItemProvider prerequisiteItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.Action} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ActionItemProvider actionItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.Artifact} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ArtifactItemProvider artifactItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.ActionAttribute} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ActionAttributeItemProvider actionAttributeItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.PathGroup} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected PathGroupItemProvider pathGroupItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.Generator} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected GeneratorItemProvider generatorItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.AlterArtifact} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AlterArtifactItemProvider alterArtifactItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.AlterGroup} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AlterGroupItemProvider alterGroupItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.AlterAction} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AlterActionItemProvider alterActionItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.Rename} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected RenameItemProvider renameItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.Remove} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected RemoveItemProvider removeItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.CSpecExtension} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected CSpecExtensionItemProvider cSpecExtensionItemProvider;

	/**
	 * This keeps track of the one adapter used for all
	 * {@link org.eclipse.buckminster.cspec.SelfArtifact} instances. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected SelfArtifactItemProvider selfArtifactItemProvider;

	/**
	 * This constructs an instance. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	public CspecItemProviderAdapterFactory() {
		supportedTypes.add(IEditingDomainItemProvider.class);
		supportedTypes.add(IStructuredItemContentProvider.class);
		supportedTypes.add(ITreeItemContentProvider.class);
		supportedTypes.add(IItemLabelProvider.class);
		supportedTypes.add(IItemPropertySource.class);
	}

	/**
	 * This implementation substitutes the factory itself as the key for the
	 * adapter. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter adapt(Notifier notifier, Object type) {
		return super.adapt(notifier, this);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object adapt(Object object, Object type) {
		if (isFactoryForType(type)) {
			Object adapter = super.adapt(object, type);
			if (!(type instanceof Class<?>) || (((Class<?>) type).isInstance(adapter))) {
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
	@Override
	public void addListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.addListener(notifyChangedListener);
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.Action}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createActionAdapter() {
		if (actionItemProvider == null) {
			actionItemProvider = new ActionItemProvider(this);
		}

		return actionItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.ActionAttribute}. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createActionAttributeAdapter() {
		if (actionAttributeItemProvider == null) {
			actionAttributeItemProvider = new ActionAttributeItemProvider(this);
		}

		return actionAttributeItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.AlterAction}. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createAlterActionAdapter() {
		if (alterActionItemProvider == null) {
			alterActionItemProvider = new AlterActionItemProvider(this);
		}

		return alterActionItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.AlterArtifact}. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createAlterArtifactAdapter() {
		if (alterArtifactItemProvider == null) {
			alterArtifactItemProvider = new AlterArtifactItemProvider(this);
		}

		return alterArtifactItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.AlterGroup}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createAlterGroupAdapter() {
		if (alterGroupItemProvider == null) {
			alterGroupItemProvider = new AlterGroupItemProvider(this);
		}

		return alterGroupItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.Artifact}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createArtifactAdapter() {
		if (artifactItemProvider == null) {
			artifactItemProvider = new ArtifactItemProvider(this);
		}

		return artifactItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.cspec.CSpec}
	 * . <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createCSpecAdapter() {
		if (cSpecItemProvider == null) {
			cSpecItemProvider = new CSpecItemProvider(this);
		}

		return cSpecItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.CSpecExtension}. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createCSpecExtensionAdapter() {
		if (cSpecExtensionItemProvider == null) {
			cSpecExtensionItemProvider = new CSpecExtensionItemProvider(this);
		}

		return cSpecExtensionItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.Generator}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createGeneratorAdapter() {
		if (generatorItemProvider == null) {
			generatorItemProvider = new GeneratorItemProvider(this);
		}

		return generatorItemProvider;
	}

	/**
	 * This creates an adapter for a {@link org.eclipse.buckminster.cspec.Group}
	 * . <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createGroupAdapter() {
		if (groupItemProvider == null) {
			groupItemProvider = new GroupItemProvider(this);
		}

		return groupItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.PathGroup}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createPathGroupAdapter() {
		if (pathGroupItemProvider == null) {
			pathGroupItemProvider = new PathGroupItemProvider(this);
		}

		return pathGroupItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.Prerequisite}. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createPrerequisiteAdapter() {
		if (prerequisiteItemProvider == null) {
			prerequisiteItemProvider = new PrerequisiteItemProvider(this);
		}

		return prerequisiteItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.Remove}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createRemoveAdapter() {
		if (removeItemProvider == null) {
			removeItemProvider = new RemoveItemProvider(this);
		}

		return removeItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.Rename}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createRenameAdapter() {
		if (renameItemProvider == null) {
			renameItemProvider = new RenameItemProvider(this);
		}

		return renameItemProvider;
	}

	/**
	 * This creates an adapter for a
	 * {@link org.eclipse.buckminster.cspec.SelfArtifact}. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Adapter createSelfArtifactAdapter() {
		if (selfArtifactItemProvider == null) {
			selfArtifactItemProvider = new SelfArtifactItemProvider(this);
		}

		return selfArtifactItemProvider;
	}

	/**
	 * This disposes all of the item providers created by this factory. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void dispose() {
		if (cSpecItemProvider != null)
			cSpecItemProvider.dispose();
		if (groupItemProvider != null)
			groupItemProvider.dispose();
		if (prerequisiteItemProvider != null)
			prerequisiteItemProvider.dispose();
		if (actionItemProvider != null)
			actionItemProvider.dispose();
		if (artifactItemProvider != null)
			artifactItemProvider.dispose();
		if (actionAttributeItemProvider != null)
			actionAttributeItemProvider.dispose();
		if (pathGroupItemProvider != null)
			pathGroupItemProvider.dispose();
		if (generatorItemProvider != null)
			generatorItemProvider.dispose();
		if (alterArtifactItemProvider != null)
			alterArtifactItemProvider.dispose();
		if (alterGroupItemProvider != null)
			alterGroupItemProvider.dispose();
		if (alterActionItemProvider != null)
			alterActionItemProvider.dispose();
		if (renameItemProvider != null)
			renameItemProvider.dispose();
		if (removeItemProvider != null)
			removeItemProvider.dispose();
		if (cSpecExtensionItemProvider != null)
			cSpecExtensionItemProvider.dispose();
		if (selfArtifactItemProvider != null)
			selfArtifactItemProvider.dispose();
	}

	/**
	 * This delegates to {@link #changeNotifier} and to
	 * {@link #parentAdapterFactory}. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	@Override
	public void fireNotifyChanged(Notification notification) {
		changeNotifier.fireNotifyChanged(notification);

		if (parentAdapterFactory != null) {
			parentAdapterFactory.fireNotifyChanged(notification);
		}
	}

	/**
	 * This returns the root adapter factory that contains this factory. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ComposeableAdapterFactory getRootAdapterFactory() {
		return parentAdapterFactory == null ? this : parentAdapterFactory.getRootAdapterFactory();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object type) {
		return supportedTypes.contains(type) || super.isFactoryForType(type);
	}

	/**
	 * This removes a listener. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void removeListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.removeListener(notifyChangedListener);
	}

	/**
	 * This sets the composed adapter factory that contains this factory. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setParentAdapterFactory(ComposedAdapterFactory parentAdapterFactory) {
		this.parentAdapterFactory = parentAdapterFactory;
	}

}
