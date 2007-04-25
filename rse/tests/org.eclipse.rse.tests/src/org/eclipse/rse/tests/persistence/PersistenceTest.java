/********************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 ********************************************************************************/

package org.eclipse.rse.tests.persistence;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.PropertySet;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;

/**
 * Tests for {@link SystemPreferencesManager}.
 * Since these are persistence tests they will play with the creation and deletion of 
 * profiles, hosts, filters, and other model objects. You should run this only in a
 * clean workspace.
 */
public class PersistenceTest extends RSECoreTestCase {
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testProfilePersistence() {
		/*
		 * Set up this particular test.
		 */
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
	
		/*
		 * Create a new profile in this profile manager. This will be the third
		 * profile created. Creating a profile causes a commit.
		 */
		try {
			registry.createSystemProfile("bogus", true); //$NON-NLS-1$
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		reload();
		
		/*
		 * There should be three profiles
		 */
		List profiles = registry.getSystemProfileManager().getProfiles();
		assertEquals(3, profiles.size());
		
		/*
		 * One should be default private profile
		 */
		boolean found = false;
		for (Iterator z = profiles.iterator(); z.hasNext() && !found;) {
			ISystemProfile p = (ISystemProfile) z.next();
			found = p.isDefaultPrivate();
		}
		assertTrue("Default private profile not found", found);
		
		/*
		 * One should be the team profile
		 */
		found = false;
		for (Iterator z = profiles.iterator(); z.hasNext() && !found;) {
			ISystemProfile p = (ISystemProfile) z.next();
			found = p.getName().equals("Team");
		}
		assertTrue("Team profile not found", found);
		
		/*
		 * One should be the test profile
		 */
		found = false;
		for (Iterator z = profiles.iterator(); z.hasNext() && !found;) {
			ISystemProfile p = (ISystemProfile) z.next();
			found = p.getName().equals("bogus");
		}
		assertTrue("bogus profile not found", found);
		
		/*
		 * Get the test profile and check its properties.
		 */
		ISystemProfile bogus = registry.getSystemProfile("bogus");
		assertNotNull(bogus);
		assertFalse(bogus.isDefaultPrivate());
		assertTrue(bogus.isActive());
		IPropertySet[] pSets = bogus.getPropertySets();
		assertNotNull(pSets);
		assertEquals(0, pSets.length);
		
		/*
		 * Add a property set to the profile.
		 */
		IPropertySet bogusProperties = new PropertySet("bogus_properties");
		bogusProperties.addProperty("bp1", "1");
		bogusProperties.addProperty("bp2", "2");
		bogus.addPropertySet(bogusProperties);
		bogus.commit();
		
		/*
		 * Refresh the profile manager.
		 */
		reload();
		
		/*
		 * Check to see if everything is still OK and that the properties are restored.
		 */
		bogus = registry.getSystemProfile("bogus");
		assertNotNull(bogus);
		assertFalse(bogus.isDefaultPrivate());
		assertTrue(bogus.isActive());
		pSets = bogus.getPropertySets();
		assertNotNull(pSets);
		assertEquals(1, pSets.length);
		bogusProperties = bogus.getPropertySet("bogus_properties");
		assertNotNull(bogusProperties);
		assertEquals("1", bogusProperties.getProperty("bp1").getValue());
		assertEquals("2", bogusProperties.getProperty("bp2").getValue());
		
		try {
			registry.deleteSystemProfile(bogus);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		reload();
		
	}

	public void testHostPersistence() {
		/*
		 * Set up this particular test.
		 */
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		
		/*
		 * Create a new profile in this profile manager. This will be the third
		 * profile created. Creating a profile causes a commit.
		 */
		try {
			registry.createSystemProfile("bogus", true); //$NON-NLS-1$
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ISystemProfile profile = registry.getSystemProfile("bogus");
		assertNotNull(profile);
		
		try {
			IRSESystemType linuxType = RSECorePlugin.getDefault().getRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LINUX_ID);
			registry.createHost("bogus", linuxType, "myhost", "myhost.mynet.mycompany.net", null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		IHost host = registry.getHost(profile, "myhost");
		assertNotNull(host);
		host.setDescription("description");
		IPropertySet props = new PropertySet("host_props");
		props.addProperty("bp1", "1");
		props.addProperty("bp2", "2");
		host.addPropertySet(props);
		host.commit();
		
		reload();
		
		/*
		 * Get the test profile and check its properties.
		 * Currently fails since hosts are not loaded properly on restore.
		 */
		profile = registry.getSystemProfile("bogus");
		assertNotNull(profile);
		host = registry.getHost(profile, "myhost");
		assertNotNull(host);
		props = host.getPropertySet("host_props");
		assertNotNull(props);
		assertEquals("1", props.getProperty("bp1").getValue());
		assertEquals("2", props.getProperty("bp2").getValue());
		
	}

	private void reload() {
		/*
		 * Set up this particular test. The persistence manager acts as the family for all 
		 * Jobs that are created for reading and writing the persistent form of the model.
		 */
		IRSEPersistenceManager persistenceManager = RSECorePlugin.getThePersistenceManager();

		/*
		 * Pause while the background job completes the save of the profile.
		 */
		IJobManager jobManager = Job.getJobManager();
		try {
			jobManager.join(persistenceManager, null);
		} catch (OperationCanceledException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		/*
		 * restore the profile manager
		 */
		RSEUIPlugin.getDefault().restart();

	}
	
}
