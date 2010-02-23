/*******************************************************************************
 * Copyright (c) 2009 Cloudsmith Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Cloudsmith Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.ql;

import java.net.URI;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.query.ExpressionContextQuery;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class TestIndexes extends AbstractProvisioningTest {

	public void testIdIndexSimple() throws Exception {
		IMetadataRepository repo = getMDR("/testData/galileoM7");
		IQuery<IInstallableUnit> query = new ExpressionContextQuery<IInstallableUnit>(IInstallableUnit.class, "select(x | x.id == $0)", "org.eclipse.sdk.feature.group");
		IQueryResult<IInstallableUnit> result = repo.query(query, getMonitor());
		assertEquals(queryResultSize(result), 1);
	}

	public void testIdIndexWithOR() throws Exception {
		IMetadataRepository repo = getMDR("/testData/galileoM7");
		IQuery<IInstallableUnit> query = new ExpressionContextQuery<IInstallableUnit>(IInstallableUnit.class, "select(x | x.id == $0 || x.id == $1)", "org.eclipse.sdk.feature.group", "org.eclipse.sdk.feature.jar");
		IQueryResult<IInstallableUnit> result = repo.query(query, getMonitor());
		assertEquals(queryResultSize(result), 2);
	}

	public void testIdIndexWithNot() throws Exception {
		IMetadataRepository repo = getMDR("/testData/galileoM7");
		IQuery<IInstallableUnit> query = new ExpressionContextQuery<IInstallableUnit>(IInstallableUnit.class, "select(x | x.id == $0 || x.id != $1)", "org.eclipse.sdk.feature.group", "org.eclipse.sdk.feature.jar");
		IQueryResult<IInstallableUnit> result = repo.query(query, getMonitor());
		assertEquals(queryResultSize(result), 3464);
	}

	public void testCapabilityIndexSimple() throws Exception {
		IMetadataRepository repo = getMDR("/testData/galileoM7");
		IQuery<IInstallableUnit> query = new ExpressionContextQuery<IInstallableUnit>(IInstallableUnit.class, "select(x | x.providedCapabilities.exists(pc | pc.namespace == 'org.eclipse.equinox.p2.iu' && pc.name == $0))", "org.eclipse.core.resources");
		IQueryResult<IInstallableUnit> result = repo.query(query, getMonitor());
		assertEquals(queryResultSize(result), 1);
	}

	public void testCapabilityIndexMatches() throws Exception {
		IMetadataRepository repo = getMDR("/testData/galileoM7");
		IRequirement requirement = MetadataFactory.createRequiredCapability("org.eclipse.equinox.p2.iu", "org.eclipse.core.resources", null, null, 1, 2, true);
		IQuery<IInstallableUnit> query = new ExpressionContextQuery<IInstallableUnit>(IInstallableUnit.class, "select(x | x ~= $0)", requirement);
		IQueryResult<IInstallableUnit> result = repo.query(query, getMonitor());
		assertEquals(queryResultSize(result), 1);
	}

	private IMetadataRepository getMDR(String uri) throws Exception {
		URI metadataRepo = getTestData("1.1", uri).toURI();

		IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) getAgent().getService(IMetadataRepositoryManager.SERVICE_NAME);
		assertNotNull(metadataManager);

		return metadataManager.loadRepository(metadataRepo, new NullProgressMonitor());
	}
}