/*******************************************************************************
 * Copyright (c) 2021 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.spring.collaborative.representations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.sirius.web.representations.IRepresentationDescription;
import org.eclipse.sirius.web.spring.collaborative.api.IRepresentationRefreshPolicy;
import org.eclipse.sirius.web.spring.collaborative.api.IRepresentationRefreshPolicyProvider;
import org.eclipse.sirius.web.spring.collaborative.api.IRepresentationRefreshPolicyRegistry;
import org.springframework.stereotype.Service;

/**
 * Registry of the representation refresh policies.
 *
 * @author gcoutable
 */
@Service
public class RepresentationRefreshPolicyRegistry implements IRepresentationRefreshPolicyRegistry {

    private List<IRepresentationRefreshPolicyProvider> representationRefreshPolicyProviders = new ArrayList<>();

    @Override
    public Optional<IRepresentationRefreshPolicy> getRepresentationRefreshPolicy(IRepresentationDescription representationDescription) {
        // @formatter:off
        return this.representationRefreshPolicyProviders.stream()
                .filter(representationRefreshPolicyProvider -> representationRefreshPolicyProvider.canHandle(representationDescription))
                .findFirst()
                .map(representationRefreshPolicyProvider -> representationRefreshPolicyProvider.getRepresentationRefreshPolicy(representationDescription));
        // @formatter:on
    }

    @Override
    public void add(IRepresentationRefreshPolicyProvider representationRefreshPolicyProvider) {
        this.representationRefreshPolicyProviders.add(representationRefreshPolicyProvider);
    }

}
