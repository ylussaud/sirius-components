/*******************************************************************************
 * Copyright (c) 2019, 2020 Obeo.
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
/** @jsx svg */
import { svg } from 'snabbdom-jsx';
import { RenderingContext, SGraph, SGraphView } from 'sprotty';
/**
 * The view used to display diagrams.
 *
 * @hmarchadour
 */
export class DiagramView extends SGraphView {
  // @ts-ignore
  render(model: Readonly<SGraph>, context: RenderingContext) {
    const transform = `scale(${model.zoom}) translate(${-model.scroll.x},${-model.scroll.y})`;
    let cursor = 'pointer';
    if (model['cursor']) {
      cursor = model['cursor'];
    }
    const styleObject = {
      cursor,
      outline: 'transparent solid 1px',
    };
    if (model['hoverFeedback']) {
      styleObject['outline'] = 'var(--blue-lagoon) solid 2px';
    }
    return (
      <svg
        class-sprotty-graph={true}
        style={styleObject}
        attrs-data-testid={`Diagram`}
        attrs-data-diagramid={model.id}
        attrs-data-descriptionid={model['descriptionId']}
      >
        <g transform={transform}>{context.renderChildren(model)}</g>
        <line id="edge-creation-feedback" x1="0" y1="0" x2="0" y2="0" visibility="hidden" />
      </svg>
    );
  }
}
