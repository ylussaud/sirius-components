/*******************************************************************************
 * Copyright (c) 2019, 2021 Obeo.
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
package org.eclipse.sirius.web.spring.collaborative.diagrams.handlers;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.sirius.web.core.api.ErrorPayload;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.core.api.IPayload;
import org.eclipse.sirius.web.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.web.diagrams.Diagram;
import org.eclipse.sirius.web.diagrams.Node;
import org.eclipse.sirius.web.diagrams.Position;
import org.eclipse.sirius.web.diagrams.description.DiagramDescription;
import org.eclipse.sirius.web.diagrams.events.CreationEvent;
import org.eclipse.sirius.web.representations.Failure;
import org.eclipse.sirius.web.representations.IStatus;
import org.eclipse.sirius.web.representations.Success;
import org.eclipse.sirius.web.representations.VariableManager;
import org.eclipse.sirius.web.spring.collaborative.api.ChangeDescription;
import org.eclipse.sirius.web.spring.collaborative.api.ChangeKind;
import org.eclipse.sirius.web.spring.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.web.spring.collaborative.diagrams.api.IDiagramEventHandler;
import org.eclipse.sirius.web.spring.collaborative.diagrams.api.IDiagramInput;
import org.eclipse.sirius.web.spring.collaborative.diagrams.api.IDiagramQueryService;
import org.eclipse.sirius.web.spring.collaborative.diagrams.dto.DropOnDiagramInput;
import org.eclipse.sirius.web.spring.collaborative.diagrams.dto.DropOnDiagramSuccessPayload;
import org.eclipse.sirius.web.spring.collaborative.diagrams.messages.ICollaborativeDiagramMessageService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * Handle "Drop in Diagram" events.
 *
 * @author hmarchadour
 */
@Service
public class DropOnDiagramEventHandler implements IDiagramEventHandler {

    private final IObjectService objectService;

    private final IDiagramQueryService diagramQueryService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final ICollaborativeDiagramMessageService messageService;

    public DropOnDiagramEventHandler(IObjectService objectService, IDiagramQueryService diagramQueryService, IRepresentationDescriptionSearchService representationDescriptionSearchService,
            ICollaborativeDiagramMessageService messageService) {
        this.objectService = Objects.requireNonNull(objectService);
        this.diagramQueryService = Objects.requireNonNull(diagramQueryService);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.messageService = Objects.requireNonNull(messageService);
    }

    @Override
    public boolean canHandle(IDiagramInput diagramInput) {
        return diagramInput instanceof DropOnDiagramInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, IDiagramContext diagramContext, IDiagramInput diagramInput) {
        String message = this.messageService.invalidInput(diagramInput.getClass().getSimpleName(), DropOnDiagramInput.class.getSimpleName());

        var changeDescription = new ChangeDescription(ChangeKind.NOTHING, diagramInput.getRepresentationId(), diagramInput);
        IPayload payload = new ErrorPayload(diagramInput.getId(), message);
        if (diagramInput instanceof DropOnDiagramInput) {
            DropOnDiagramInput input = (DropOnDiagramInput) diagramInput;

            List<Object> objects = input.getObjectIds().stream().map(objectId -> this.objectService.getObject(editingContext, objectId)).flatMap(Optional::stream).collect(Collectors.toList());
            Diagram diagram = diagramContext.getDiagram();

            payload = new ErrorPayload(diagramInput.getId(), this.messageService.invalidDrop());
            if (!objects.isEmpty()) {
                IStatus status = this.executeTool(editingContext, diagramContext, objects, input.getDiagramTargetElementId(), input.getStartingPositionX(), input.getStartingPositionY());
                if (status instanceof Success) {
                    changeDescription = new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, diagramInput.getRepresentationId(), diagramInput);
                    payload = new DropOnDiagramSuccessPayload(diagramInput.getId(), diagram);
                }
            }
        }

        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }

    private IStatus executeTool(IEditingContext editingContext, IDiagramContext diagramContext, List<Object> objects, UUID diagramElementId, double startingPositionX, double startingPositionY) {
        IStatus result = new Failure(""); //$NON-NLS-1$
        Diagram diagram = diagramContext.getDiagram();
        Optional<Node> node = this.diagramQueryService.findNodeById(diagram, diagramElementId);

        // @formatter:off
        var optionalDropHandler = this.representationDescriptionSearchService.findById(editingContext, diagram.getDescriptionId())
            .filter(DiagramDescription.class::isInstance)
            .map(DiagramDescription.class::cast)
            .map(DiagramDescription::getDropHandler);
        // @formatter:on

        if (optionalDropHandler.isPresent()) {
            result = new Success();
            var dropHandler = optionalDropHandler.get();
            Position newPosition = Position.at(startingPositionX, startingPositionY);
            diagramContext.setDiagramEvent(new CreationEvent(newPosition));

            for (Object self : objects) {
                VariableManager variableManager = new VariableManager();
                if (node.isPresent()) {
                    variableManager.put(Node.SELECTED_NODE, node.get());
                }
                variableManager.put(IEditingContext.EDITING_CONTEXT, editingContext);
                variableManager.put(IDiagramContext.DIAGRAM_CONTEXT, diagramContext);
                variableManager.put(VariableManager.SELF, self);

                IStatus dropResult = dropHandler.apply(variableManager);
                if (result instanceof Failure) {
                    // Let all drops finished but keep the error state
                    result = dropResult;
                }
            }
        }
        return result;
    }
}
