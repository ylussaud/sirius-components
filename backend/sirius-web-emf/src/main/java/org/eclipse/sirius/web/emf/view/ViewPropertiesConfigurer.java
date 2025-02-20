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
package org.eclipse.sirius.web.emf.view;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.web.api.configuration.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.web.api.configuration.IPropertiesDescriptionRegistryConfigurer;
import org.eclipse.sirius.web.core.api.IEditingContext;
import org.eclipse.sirius.web.forms.components.SelectComponent;
import org.eclipse.sirius.web.forms.description.AbstractControlDescription;
import org.eclipse.sirius.web.forms.description.CheckboxDescription;
import org.eclipse.sirius.web.forms.description.FormDescription;
import org.eclipse.sirius.web.forms.description.GroupDescription;
import org.eclipse.sirius.web.forms.description.PageDescription;
import org.eclipse.sirius.web.forms.description.SelectDescription;
import org.eclipse.sirius.web.forms.description.TextfieldDescription;
import org.eclipse.sirius.web.representations.Failure;
import org.eclipse.sirius.web.representations.GetOrCreateRandomIdProvider;
import org.eclipse.sirius.web.representations.IRepresentationDescription;
import org.eclipse.sirius.web.representations.IStatus;
import org.eclipse.sirius.web.representations.Success;
import org.eclipse.sirius.web.representations.VariableManager;
import org.eclipse.sirius.web.spring.collaborative.validation.api.IValidationService;
import org.eclipse.sirius.web.view.ConditionalNodeStyle;
import org.eclipse.sirius.web.view.NodeStyle;
import org.eclipse.sirius.web.view.ViewPackage;
import org.springframework.stereotype.Component;

/**
 * Customizes the properties view for some of the View DSL elements.
 *
 * @author pcdavid
 */
@Component
public class ViewPropertiesConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    private static final String EMPTY = ""; //$NON-NLS-1$

    private static final String UNNAMED = "<unnamed>"; //$NON-NLS-1$

    private final Function<VariableManager, List<Object>> semanticElementsProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).stream().collect(Collectors.toList());

    private final ICustomImageSearchService customImageSearchService;

    private final IValidationService validationService;

    public ViewPropertiesConfigurer(ICustomImageSearchService customImageSearchService, IValidationService validationService) {
        this.validationService = Objects.requireNonNull(validationService);
        this.customImageSearchService = Objects.requireNonNull(customImageSearchService);
    }

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        registry.add(this.getConditionalNodeStyleProperties());
        registry.add(this.getNodeStyleProperties());
    }

    private FormDescription getConditionalNodeStyleProperties() {
        UUID formDescriptionId = UUID.nameUUIDFromBytes("conditionalnodestyle".getBytes()); //$NON-NLS-1$

        // @formatter:off
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, NodeStyle.class)
                                                                                                     .map(obj -> EcoreUtil.getURI(obj).toString())
                                                                                                     .orElse(null);

        List<AbstractControlDescription> controls = List.of(
                this.createTextField("nodestyle.sizeExpression", "Size Expression", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).getSizeComputationExpression(),
                        (style, newSizeExpression) -> ((NodeStyle) style).setSizeComputationExpression(newSizeExpression),
                        ViewPackage.Literals.NODE_STYLE__SIZE_COMPUTATION_EXPRESSION),
                this.createTextField("conditionalnodestyle.condition", "Condition", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((ConditionalNodeStyle) style).getCondition(),
                        (style, newCondition) -> ((ConditionalNodeStyle) style).setCondition(newCondition),
                        ViewPackage.Literals.CONDITIONAL__CONDITION),
                this.createTextField("conditionalnodestyle.labelColor", "Label Color", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).getLabelColor(),
                        (style, newLabelColor) -> ((NodeStyle) style).setLabelColor(newLabelColor),
                        ViewPackage.Literals.NODE_STYLE__LABEL_COLOR),
                this.createTextField("conditionalnodestyle.color", "Color", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).getColor(),
                        (style, newColor) -> ((NodeStyle) style).setColor(newColor),
                        ViewPackage.Literals.STYLE__COLOR),
                this.createTextField("conditionalnodestyle.borderColor", "Border Color", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).getBorderColor(),
                        (style, newColor) -> ((NodeStyle) style).setBorderColor(newColor),
                        ViewPackage.Literals.STYLE__BORDER_COLOR),
                this.createTextField("conditionalnodestyle.borderRadius", "Border Radius", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> String.valueOf(((NodeStyle) style).getBorderRadius()),
                        (style, newBorderRadius) -> {
                            try {
                                ((NodeStyle) style).setBorderRadius(Integer.parseInt(newBorderRadius));
                            } catch (NumberFormatException nfe) {
                                // Ignore.
                            }
                        },
                        ViewPackage.Literals.NODE_STYLE__BORDER_RADIUS),
                this.createTextField("conditionalnodestyle.borderSize", "Border Size", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> String.valueOf(((NodeStyle) style).getBorderSize()),
                        (style, newBorderSize) -> {
                            try {
                                ((NodeStyle) style).setBorderSize(Integer.parseInt(newBorderSize));
                            } catch (NumberFormatException nfe) {
                                // Ignore.
                            }
                        },
                        ViewPackage.Literals.NODE_STYLE__BORDER_SIZE),
                this.createCheckbox("conditionalnodestyle.listMost", "List Mode", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isListMode(),
                        (style, newListMode) -> ((NodeStyle) style).setListMode(newListMode),
                        ViewPackage.Literals.NODE_STYLE__LIST_MODE),
                this.createTextField("conditionalnodestyle.fontSize", "Font Size", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> String.valueOf(((NodeStyle) style).getFontSize()),
                        (style, newColor) -> {
                            try {
                                ((NodeStyle) style).setFontSize(Integer.parseInt(newColor));
                            } catch (NumberFormatException nfe) {
                                // Ignore.
                            }
                        },
                        ViewPackage.Literals.STYLE__FONT_SIZE),
                this.createCheckbox("conditionalnodestyle.italic", "Italic", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isItalic(),
                        (style, newItalic) -> ((NodeStyle) style).setItalic(newItalic),
                        ViewPackage.Literals.NODE_STYLE__ITALIC),
                this.createCheckbox("conditionalnodestyle.bold", "Bold", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isBold(),
                        (style, newBold) -> ((NodeStyle) style).setBold(newBold),
                        ViewPackage.Literals.NODE_STYLE__BOLD),
                this.createCheckbox("conditionalnodestyle.underline", "Underline", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isUnderline(),
                        (style, newUnderline) -> ((NodeStyle) style).setUnderline(newUnderline),
                        ViewPackage.Literals.NODE_STYLE__UNDERLINE),
                this.createCheckbox("conditionalnodestyle.strikeThrough", "Strike Through", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isStrikeThrough(),
                        (style, newStrikeThrough) -> ((NodeStyle) style).setStrikeThrough(newStrikeThrough),
                        ViewPackage.Literals.NODE_STYLE__STRIKE_THROUGH),
                this.createShapeSelectionField(ViewPackage.Literals.NODE_STYLE__SHAPE));

        GroupDescription groupDescription = this.createSimpleGroupDescription(controls);
        return FormDescription.newFormDescription(formDescriptionId)
                .label("Conditional Node Style") //$NON-NLS-1$
                .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, ConditionalNodeStyle.class).map(ConditionalNodeStyle::getCondition).orElse(UNNAMED))
                .canCreatePredicate(variableManager -> {
                    var optionalClass = variableManager.get(IRepresentationDescription.CLASS, Object.class);
                    return optionalClass.isPresent() && optionalClass.get().equals(ConditionalNodeStyle.class);
                })
                .idProvider(new GetOrCreateRandomIdProvider())
                .targetObjectIdProvider(targetObjectIdProvider)
                .pageDescriptions(List.of(this.createSimplePageDescription(groupDescription,  variableManager -> {
                    Optional<?> optionalValue = variableManager.get(VariableManager.SELF, ConditionalNodeStyle.class);
                    return optionalValue.isPresent();
                })))
                .groupDescriptions(List.of(groupDescription))
                .build();
        // @formatter:on
    }

    private FormDescription getNodeStyleProperties() {
        UUID formDescriptionId = UUID.nameUUIDFromBytes("nodestyle".getBytes()); //$NON-NLS-1$

        // @formatter:off
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, NodeStyle.class)
                                                                                                     .map(obj -> EcoreUtil.getURI(obj).toString())
                                                                                                     .orElse(null);

        List<AbstractControlDescription> controls = List.of(
                this.createTextField("nodestyle.sizeExpression", "Size Expression", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).getSizeComputationExpression(),
                        (style, newSizeExpression) -> ((NodeStyle) style).setSizeComputationExpression(newSizeExpression),
                        ViewPackage.Literals.NODE_STYLE__SIZE_COMPUTATION_EXPRESSION),
                this.createTextField("nodestyle.labelColor", "Label Color", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).getLabelColor(),
                        (style, newLabelColor) -> ((NodeStyle) style).setLabelColor(newLabelColor),
                        ViewPackage.Literals.NODE_STYLE__LABEL_COLOR),
                this.createTextField("nodestyle.color", "Color", //$NON-NLS-1$ //$NON-NLS-2$
                                     style -> ((NodeStyle) style).getColor(),
                                     (style, newColor) -> ((NodeStyle) style).setColor(newColor),
                                     ViewPackage.Literals.STYLE__COLOR),
                this.createTextField("nodestyle.borderColor", "Border Color", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).getBorderColor(),
                        (style, newColor) -> ((NodeStyle) style).setBorderColor(newColor),
                        ViewPackage.Literals.STYLE__BORDER_COLOR),
                this.createTextField("nodestyle.borderRadius", "Border Radius", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> String.valueOf(((NodeStyle) style).getBorderRadius()),
                        (style, newBorderRadius) -> {
                            try {
                                ((NodeStyle) style).setBorderRadius(Integer.parseInt(newBorderRadius));
                            } catch (NumberFormatException nfe) {
                                // Ignore.
                            }
                        },
                        ViewPackage.Literals.NODE_STYLE__BORDER_RADIUS),
                this.createCheckbox("nodestyle.listMost", "List Mode", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isListMode(),
                        (style, newListMode) -> ((NodeStyle) style).setListMode(newListMode),
                        ViewPackage.Literals.NODE_STYLE__LIST_MODE),
                this.createTextField("nodestyle.borderSize", "Border Size", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> String.valueOf(((NodeStyle) style).getBorderSize()),
                        (style, newBorderSize) -> {
                            try {
                                ((NodeStyle) style).setBorderSize(Integer.parseInt(newBorderSize));
                            } catch (NumberFormatException nfe) {
                                // Ignore.
                            }
                        },
                        ViewPackage.Literals.NODE_STYLE__BORDER_SIZE),
                this.createTextField("nodestyle.fontSize", "Font Size", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> String.valueOf(((NodeStyle) style).getFontSize()),
                        (style, newColor) -> {
                            try {
                                ((NodeStyle) style).setFontSize(Integer.parseInt(newColor));
                            } catch (NumberFormatException nfe) {
                                // Ignore.
                            }
                        },
                        ViewPackage.Literals.STYLE__FONT_SIZE),
                this.createCheckbox("nodestyle.italic", "Italic", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isItalic(),
                        (style, newItalic) -> ((NodeStyle) style).setItalic(newItalic),
                        ViewPackage.Literals.NODE_STYLE__ITALIC),
                this.createCheckbox("nodestyle.bold", "Bold", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isBold(),
                        (style, newBold) -> ((NodeStyle) style).setBold(newBold),
                        ViewPackage.Literals.NODE_STYLE__BOLD),
                this.createCheckbox("nodestyle.underline", "Underline", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isUnderline(),
                        (style, newUnderline) -> ((NodeStyle) style).setUnderline(newUnderline),
                        ViewPackage.Literals.NODE_STYLE__UNDERLINE),
                this.createCheckbox("nodestyle.strikeThrough", "Strike Through", //$NON-NLS-1$ //$NON-NLS-2$
                        style -> ((NodeStyle) style).isStrikeThrough(),
                        (style, newStrikeThrough) -> ((NodeStyle) style).setStrikeThrough(newStrikeThrough),
                        ViewPackage.Literals.NODE_STYLE__STRIKE_THROUGH),
                this.createShapeSelectionField(ViewPackage.Literals.NODE_STYLE__SHAPE));

        GroupDescription groupDescription = this.createSimpleGroupDescription(controls);
        return FormDescription.newFormDescription(formDescriptionId)
                .label("Node Style") //$NON-NLS-1$
                .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, NodeStyle.class).map(style -> style.getColor()).orElse(UNNAMED))
                .canCreatePredicate(variableManager -> {
                    var optionalClass = variableManager.get(IRepresentationDescription.CLASS, Object.class);
                    return optionalClass.isPresent() && optionalClass.get().equals(NodeStyle.class);
                })
                .idProvider(new GetOrCreateRandomIdProvider())
                .targetObjectIdProvider(targetObjectIdProvider)
                .pageDescriptions(List.of(this.createSimplePageDescription(groupDescription, variableManager -> {
                    Optional<?> optionalValue = variableManager.get(VariableManager.SELF, NodeStyle.class);
                    return optionalValue.isPresent() && !(optionalValue.get() instanceof ConditionalNodeStyle);
                })))
                .groupDescriptions(List.of(groupDescription))
                .build();
        // @formatter:on
    }

    private PageDescription createSimplePageDescription(GroupDescription groupDescription, Predicate<VariableManager> canCreatePredicate) {
        // @formatter:off
        return PageDescription.newPageDescription("page") //$NON-NLS-1$
                              .idProvider(variableManager -> "page") //$NON-NLS-1$
                              .labelProvider(variableManager -> "Properties") //$NON-NLS-1$
                              .semanticElementsProvider(this.semanticElementsProvider)
                              .canCreatePredicate(canCreatePredicate)
                              .groupDescriptions(List.of(groupDescription))
                              .build();
        // @formatter:on
    }

    private GroupDescription createSimpleGroupDescription(List<AbstractControlDescription> controls) {
        // @formatter:off
        return GroupDescription.newGroupDescription("group") //$NON-NLS-1$
                               .idProvider(variableManager -> "group") //$NON-NLS-1$
                               .labelProvider(variableManager -> "General") //$NON-NLS-1$
                               .semanticElementsProvider(this.semanticElementsProvider)
                               .controlDescriptions(controls)
                               .build();
        // @formatter:on
    }

    private TextfieldDescription createTextField(String id, String title, Function<Object, String> reader, BiConsumer<Object, String> writer, Object feature) {
        Function<VariableManager, String> valueProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).map(reader).orElse(EMPTY);
        BiFunction<VariableManager, String, IStatus> newValueHandler = (variableManager, newValue) -> {
            var optionalDiagramMapping = variableManager.get(VariableManager.SELF, Object.class);
            if (optionalDiagramMapping.isPresent()) {
                writer.accept(optionalDiagramMapping.get(), newValue);
                return new Success();
            } else {
                return new Failure(""); //$NON-NLS-1$
            }
        };

        // @formatter:off
        return TextfieldDescription.newTextfieldDescription(id)
                                   .idProvider(variableManager -> id)
                                   .labelProvider(variableManager -> title)
                                   .valueProvider(valueProvider)
                                   .newValueHandler(newValueHandler)
                                   .diagnosticsProvider(this.getDiagnosticsProvider(feature))
                                   .kindProvider(this::kindProvider)
                                   .messageProvider(this::messageProvider)
                                   .build();
        // @formatter:on
    }

    private CheckboxDescription createCheckbox(String id, String title, Function<Object, Boolean> reader, BiConsumer<Object, Boolean> writer, Object feature) {
        Function<VariableManager, Boolean> valueProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).map(reader).orElse(Boolean.FALSE);
        BiFunction<VariableManager, Boolean, IStatus> newValueHandler = (variableManager, newValue) -> {
            var optionalDiagramMapping = variableManager.get(VariableManager.SELF, Object.class);
            if (optionalDiagramMapping.isPresent()) {
                writer.accept(optionalDiagramMapping.get(), newValue);
                return new Success();
            } else {
                return new Failure(""); //$NON-NLS-1$
            }
        };
        // @formatter:off
        return CheckboxDescription.newCheckboxDescription(id)
                                   .idProvider(variableManager -> id)
                                   .labelProvider(variableManager -> title)
                                   .valueProvider(valueProvider)
                                   .newValueHandler(newValueHandler)
                                   .diagnosticsProvider(this.getDiagnosticsProvider(feature))
                                   .kindProvider(this::kindProvider)
                                   .messageProvider(this::messageProvider)
                                   .build();
        // @formatter:on
    }

    private SelectDescription createShapeSelectionField(Object feature) {
        // @formatter:off
        return SelectDescription.newSelectDescription("nodestyle.shapeSelector") //$NON-NLS-1$
                                .idProvider(variableManager -> "nodestyle.shapeSelector") //$NON-NLS-1$
                                .labelProvider(variableManager -> "Shape") //$NON-NLS-1$
                                .valueProvider(variableManager -> variableManager.get(VariableManager.SELF, NodeStyle.class).map(NodeStyle::getShape).orElse(EMPTY))
                                .optionsProvider(variableManager -> {
                                    Optional<IEditingContext> optionalEditingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class);
                                    if (optionalEditingContext.isPresent()) {
                                        return this.customImageSearchService.getAvailableImages(optionalEditingContext.get().getId()).stream().sorted(Comparator.comparing(CustomImage::getLabel)).collect(Collectors.toList());
                                    } else {
                                        return List.of();
                                    }
                                })
                                .optionIdProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, CustomImage.class).map(CustomImage::getId).map(UUID::toString).orElse(EMPTY))
                                .optionLabelProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, CustomImage.class).map(CustomImage::getLabel).orElse(EMPTY))
                                .newValueHandler(this.getNewShapeValueHandler())
                                .diagnosticsProvider(this.getDiagnosticsProvider(feature))
                                .kindProvider(this::kindProvider)
                                .messageProvider(this::messageProvider)
                                .build();
        // @formatter:on
    }

    private BiFunction<VariableManager, String, IStatus> getNewShapeValueHandler() {
        return (variableManager, newValue) -> {
            var optionalNodeStyle = variableManager.get(VariableManager.SELF, NodeStyle.class);
            if (optionalNodeStyle.isPresent()) {
                if (newValue != null && newValue.isBlank()) {
                    newValue = null;
                }
                optionalNodeStyle.get().setShape(newValue);
                return new Success();
            }
            return new Failure(""); //$NON-NLS-1$
        };
    }

    private Function<VariableManager, List<Object>> getDiagnosticsProvider(Object feature) {
        return variableManager -> {
            var optionalSelf = variableManager.get(VariableManager.SELF, EObject.class);

            if (optionalSelf.isPresent()) {
                EObject self = optionalSelf.get();
                List<Object> diagnostics = this.validationService.validate(self, feature);
                return diagnostics;
            }

            return List.of();
        };
    }

    private String kindProvider(Object object) {
        String kind = "Unknown"; //$NON-NLS-1$
        if (object instanceof Diagnostic) {
            Diagnostic diagnostic = (Diagnostic) object;
            switch (diagnostic.getSeverity()) {
            case org.eclipse.emf.common.util.Diagnostic.ERROR:
                kind = "Error"; //$NON-NLS-1$
                break;
            case org.eclipse.emf.common.util.Diagnostic.WARNING:
                kind = "Warning"; //$NON-NLS-1$
                break;
            case org.eclipse.emf.common.util.Diagnostic.INFO:
                kind = "Info"; //$NON-NLS-1$
                break;
            default:
                kind = "Unknown"; //$NON-NLS-1$
                break;
            }
        }
        return kind;
    }

    private String messageProvider(Object object) {
        if (object instanceof Diagnostic) {
            Diagnostic diagnostic = (Diagnostic) object;
            return diagnostic.getMessage();
        }
        return ""; //$NON-NLS-1$
    }

}
