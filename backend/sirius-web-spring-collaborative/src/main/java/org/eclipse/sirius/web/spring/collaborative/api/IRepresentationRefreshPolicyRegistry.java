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
package org.eclipse.sirius.web.spring.collaborative.api;

import java.util.Optional;

import org.eclipse.sirius.web.representations.IRepresentationDescription;

/**
 * Registry of contributed representation refresh policy.
 *
 * @author gcoutable
 */
public interface IRepresentationRefreshPolicyRegistry {

    Optional<IRepresentationRefreshPolicy> getRepresentationRefreshPolicy(IRepresentationDescription representationDescription);

    void add(IRepresentationRefreshPolicyProvider representationRefreshPolicyProvider);

    /**
     * Implementation which does nothing, used for mocks in unit tests.
     *
     * @author gcoutable
     */
    class NoOp implements IRepresentationRefreshPolicyRegistry {

        @Override
        public Optional<IRepresentationRefreshPolicy> getRepresentationRefreshPolicy(IRepresentationDescription representationDescription) {
            return Optional.empty();
        }

        @Override
        public void add(IRepresentationRefreshPolicyProvider representationRefreshPolicyProvider) {
        }

    }
}
