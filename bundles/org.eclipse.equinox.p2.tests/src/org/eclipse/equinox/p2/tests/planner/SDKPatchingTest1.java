/*******************************************************************************
 *  Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.planner;

import java.io.File;
import java.util.ArrayList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.*;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.planner.ProfileInclusionRules;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class SDKPatchingTest1 extends AbstractProvisioningTest {
	IProfile profile = null;
	ArrayList<IInstallableUnit> newIUs = new ArrayList<>();
	IInstallableUnit patchInstallingCommon = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		File reporegistry1 = getTestData("test data for sdkpatching test", "testData/sdkpatchingtest");
		File tempFolder = getTempFolder();
		copy("0.2", reporegistry1, tempFolder);
		SimpleProfileRegistry registry = new SimpleProfileRegistry(getAgent(), tempFolder, null, false);
		profile = registry.getProfile("SDKPatchingTest");
		assertNotNull(profile);

		MetadataFactory.InstallableUnitDescription newCommon = createIUDescriptor(profile.query(QueryUtil.createIUQuery("org.eclipse.equinox.common"), new NullProgressMonitor()).iterator().next());
		Version newVersionCommon = Version.createOSGi(3, 5, 0, "zeNewVersion");
		changeVersion(newCommon, newVersionCommon);
		newIUs.add(MetadataFactory.createInstallableUnit(newCommon));

		IRequirementChange change = MetadataFactory.createRequirementChange(MetadataFactory.createRequirement(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.equinox.common", VersionRange.emptyRange, null, false, false, false), MetadataFactory.createRequirement(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.equinox.common", new VersionRange(newVersionCommon, true, newVersionCommon, true), null, false, false, true));
		IRequirement lifeCycle = MetadataFactory.createRequirement(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.rcp.feature.group", new VersionRange("[3.5.0.v20081110-9E9vFtpFlN1yW2Ray4WRVBYE, 3.5.0.v20081110-9E9vFtpFlN1yW2Ray4WRVBYE]"), null, false, false, true);
		patchInstallingCommon = createIUPatch("P", Version.create("1.0.0"), true, new IRequirementChange[] {change}, new IRequirement[0][0], lifeCycle);

		newIUs.add(patchInstallingCommon);
	}

	public void testInstallFeaturePatch() {
		ProvisioningContext ctx = new ProvisioningContext(getAgent());
		ctx.setExtraInstallableUnits(newIUs);
		ProfileChangeRequest request = new ProfileChangeRequest(profile);
		request.addInstallableUnits(patchInstallingCommon);
		request.setInstallableUnitInclusionRules(patchInstallingCommon, ProfileInclusionRules.createOptionalInclusionRule(patchInstallingCommon));
		IPlanner planner = createPlanner();
		IProvisioningPlan plan = planner.getProvisioningPlan(request, ctx, new NullProgressMonitor());
		assertOK("Installation plan", plan.getStatus());
		assertEquals(3, countPlanElements(plan));
	}
}
