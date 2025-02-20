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

import org.eclipse.sirius.web.representations.IRepresentationDescription;

/**
 * The provider interface to contribute a representation refresh policy to the representation refresh policy registry.
 *
 * @author gcoutable
 */
public interface IRepresentationRefreshPolicyProvider {
    boolean canHandle(IRepresentationDescription representationDescription);

    IRepresentationRefreshPolicy getRepresentationRefreshPolicy(IRepresentationDescription representationDescription);
}
