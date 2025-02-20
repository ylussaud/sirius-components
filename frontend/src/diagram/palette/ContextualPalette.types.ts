/*******************************************************************************
 * Copyright (c) 2021 Obeo and others.
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
import { Tool, ToolSection } from 'diagram/DiagramWebSocketContainer.types';

export interface ContextualPaletteProps {
  toolSections: ToolSection[];
  targetElement: any;
  invokeTool: (tool: Tool) => void;
  invokeConnectorTool: () => void;
  invokeLabelEdit: () => void | null;
  invokeDelete: () => void | null;
  invokeClose: () => void;
}
