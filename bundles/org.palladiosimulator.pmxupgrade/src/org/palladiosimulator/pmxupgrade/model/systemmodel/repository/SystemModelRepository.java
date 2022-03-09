package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.MessagingOperation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * Represents the System Model repositoryof the system model.
 *
 * @author Patrick Treyer
 */
public class SystemModelRepository {

    private static final String ENCODING = "UTF-8";
    public static final String ROOT_NODE_LABEL = "'Entry'";

    private final TypeRepository typeRepositoryFactory;
    private final AssemblyRepository assemblyFactory;
    private final ExecutionEnvironmentRepository executionEnvironmentFactory;
    private final AllocationRepository allocationFactory;
    private final OperationRepository operationFactory;
    private final AllocationComponentOperationPairFactory allocationPairFactory;
    private final AssemblyComponentOperationPairFactory assemblyPairFactory;
    private final MessagingOperationRepository messagingOperationFactory;
    private final DataInterfacesRepository dataInterfaceFactory;

    /**
     * Creates a new instance of this class using the given parameters.
     */
    public SystemModelRepository() {
        this.typeRepositoryFactory = new TypeRepository(this);
        this.assemblyFactory = new AssemblyRepository(this);
        this.executionEnvironmentFactory = new ExecutionEnvironmentRepository(this);
        this.allocationFactory = new AllocationRepository(this);
        this.operationFactory = new OperationRepository(this);
        this.allocationPairFactory = new AllocationComponentOperationPairFactory(this);
        this.assemblyPairFactory = new AssemblyComponentOperationPairFactory(this);
        this.messagingOperationFactory = new MessagingOperationRepository(this);
        this.dataInterfaceFactory = new DataInterfacesRepository(this);
    }

    public final AllocationRepository getAllocationFactory() {
        return this.allocationFactory;
    }

    public final AssemblyRepository getAssemblyFactory() {
        return this.assemblyFactory;
    }

    public final ExecutionEnvironmentRepository getExecutionEnvironmentFactory() {
        return this.executionEnvironmentFactory;
    }
    
    /**
     * Delivering the factory managing the available operations.
     *
     * @return The operation factory.
     */
    public final OperationRepository getOperationFactory() {
        return this.operationFactory;
    }

    /**
     * Delivering the factory managing the available component types.
     *
     * @return The types factory.
     */
    public final TypeRepository getTypeRepositoryFactory() {
        return this.typeRepositoryFactory;
    }
    
    public final DataInterfacesRepository getDataInterfacesRepository() {
    	return this.dataInterfaceFactory;
    }

    public AllocationComponentOperationPairFactory getAllocationPairFactory() {
        return this.allocationPairFactory;
    }

    public AssemblyComponentOperationPairFactory getAssemblyPairFactory() {
        return this.assemblyPairFactory;
    }
    //TODO add html representation for channels op ~fat
    private enum EntityType {
        COMPONENT_TYPE, OPERATION, ASSEMBLY_COMPONENT, ALLOCATION_COMPONENT, EXECUTION_CONTAINER, MESSAGING_OPERATION
    }

    private String htmlEntityLabel(final int id, final String caption, final EntityType entityType) {
        final StringBuilder strBuild = new StringBuilder(64);
        strBuild.append("<a name=\"").append(SystemModelRepository.simpleHTMLEscape(entityType.toString())).append('-').append(id).append("\">").append(caption)
                .append("</a>");
        return strBuild.toString();
    }

    private String htmlEntityRef(final int id, final String caption, final EntityType entityType) {
        final StringBuilder strBuild = new StringBuilder(64);
        strBuild.append("<a href=\"#").append(SystemModelRepository.simpleHTMLEscape(entityType.toString())).append('-').append(id).append("\">").append(caption)
                .append("</a>");
        return strBuild.toString();
    }

    private void printOpenHtmlTable(final PrintStream ps, final String title, final String[] columnTitle) {
        ps.println("<table class=\"tab\" border=\"1\" style=\"width:100%\">");
        ps.println("<tr><th class=\"tabTitle\" colspan=\"" + columnTitle.length + "\">" + title + "</th></tr>");
        ps.println("<tr>");
        for (final String cell : columnTitle) {
            ps.println("<th class=\"colTitle space\">" + cell + "</th>");
        }
        ps.println("</tr>");
    }

    private void printHtmlTableRow(final PrintStream ps, final String[] cells) {
        ps.println("<tr class=\"cell\">");
        for (final String cell : cells) {
            ps.println("<td class=\"space\">" + ((cell.length() == 0) ? "&nbsp;" : cell) + "</td>"); // NOCS
        }
        ps.println("</tr>");
    }

    private void printCloseHtmlTable(final PrintStream ps) {
        ps.println("</table>");
    }

    private void htmlHSpace(final PrintStream ps, final int numLines) {
        if (numLines <= 0) {
            return;
        }
        final StringBuilder strBuild = new StringBuilder("<pre>\n");
        for (int i = 0; i < numLines; i++) {
            strBuild.append(".\n");
        }
        strBuild.append("</pre>");
        ps.println(strBuild.toString());
    }

    /**
     * Writes the contents of this system model to an HTML file.
     *
     * @param outputFn file system location of the output file (as accepted by {@link java.io.File#File(String)}).
     * @throws FileNotFoundException        If the given file is somehow invalid.
     * @throws UnsupportedEncodingException If the used default encoding is not supported.
     */
    public void saveSystemToHTMLFile(final String outputFn) throws FileNotFoundException, UnsupportedEncodingException {
        final PrintStream ps = new PrintStream(new FileOutputStream(outputFn), false, ENCODING);
        ps.println("<html><head><title>System Model Reconstructed by Kieker.TraceAnalysis</title>");
        ps.println("<style type=\"text/css\">\n"
                + ".colTitle {font-size: 11px; background: linear-gradient(to bottom, #FDFDFD, #DDDDDD) transparent }\n"
                + ".cell {font-family: monospace; font-size: 10px; font-family: inherited}\n"
                + ".tabTitle {padding: 4px 4px; font-size: 12px; background: linear-gradient(to bottom, #FFFFFF, #CCEEFF) transparent; border: 1px solid #4DC4FF;"
                + "color: #333399}\n"
                + ".tab {border-collapse: collapse;  border: 1px solid #9D9D9D; font-family: \"Segoe UI\", \"Verdana\", \"Arial\", sans-serif}\n"
                + ".space{padding: 4px 10px;}\n" + "</style>");
        ps.println("</head><body>");
        this.htmlHSpace(ps, 10);
        this.printOpenSurroundingSpan(ps);
        this.printOpenHtmlTable(ps, "Component Types", new String[]{"ID", "Package", "Name", "Operations"});
        final Collection<BasicComponentType> componentTypes = this.typeRepositoryFactory.getComponentTypes();
        for (final BasicComponentType type : componentTypes) {
            final StringBuilder opListBuilder = new StringBuilder();
            if (type.getOperations().size() > 0) {
                for (final Operation op : type.getOperations()) {
                    opListBuilder.append("<li>")
                            .append(this.htmlEntityRef(op.getId(), SystemModelRepository.simpleHTMLEscape(op.getSignature().toString()), EntityType.OPERATION))
                            .append("</li>");
                }
            }
            final String[] cells = new String[]{
                    this.htmlEntityLabel(type.getId(), Integer.toString(type.getId()), EntityType.COMPONENT_TYPE),
                    SystemModelRepository.simpleHTMLEscape(type.getPackageName()), SystemModelRepository.simpleHTMLEscape(type.getTypeName()),
                    opListBuilder.toString(),};
            this.printHtmlTableRow(ps, cells);
        }
        this.printCloseHtmlTable(ps);
        this.printLinebreak(ps);
        this.printOpenHtmlTable(ps, "Channel Types", new String[]{"ID", "Package", "Name", "Operations"});
        final Collection<DataChannelType> channelTypes = this.typeRepositoryFactory.getChannelTypes();
        for (final DataChannelType type : channelTypes) {
            final StringBuilder opListBuilder = new StringBuilder();
            if (type.getOperations().size() > 0) {
                for (final Operation op : type.getOperations()) {
                    opListBuilder.append("<li>")
                            .append(this.htmlEntityRef(op.getId(), SystemModelRepository.simpleHTMLEscape(op.getSignature().toString()), EntityType.OPERATION))
                            .append("</li>");
                }
            }
            final String[] cells = new String[]{
                    this.htmlEntityLabel(type.getId(), Integer.toString(type.getId()), EntityType.COMPONENT_TYPE),
                    SystemModelRepository.simpleHTMLEscape(type.getPackageName()), SystemModelRepository.simpleHTMLEscape(type.getTypeName()),
                    opListBuilder.toString(),};
            this.printHtmlTableRow(ps, cells);
        }
        this.printCloseHtmlTable(ps);
        this.printLinebreak(ps);
        this.printOpenHtmlTable(ps, "Operations", new String[]{"ID", "Component type", "Name", "Parameter types", "Return type"});
        final Collection<Operation> operations = this.operationFactory.getOperations();
        for (final Operation op : operations) {
            final StringBuilder paramListStrBuild = new StringBuilder();
            for (final String paramType : op.getSignature().getParamTypeList()) {
                paramListStrBuild.append("<li>").append(SystemModelRepository.simpleHTMLEscape(paramType)).append("</li>");
            }
            final String[] cells = new String[]{
                    this.htmlEntityLabel(op.getId(), Integer.toString(op.getId()), EntityType.OPERATION),
                    this.htmlEntityRef(op.getComponentType().getId(), SystemModelRepository.simpleHTMLEscape(op.getComponentType().getFullQualifiedName()),
                            EntityType.COMPONENT_TYPE),
                    SystemModelRepository.simpleHTMLEscape(op.getSignature().getName()), paramListStrBuild.toString(),
                    SystemModelRepository.simpleHTMLEscape(op.getSignature().getReturnType()),};
            this.printHtmlTableRow(ps, cells);
        }
        final Collection<MessagingOperation> messagingOperations = this.operationFactory.getMessagingOperations();
        for (final Operation op : messagingOperations) {
            final StringBuilder paramListStrBuild = new StringBuilder();
            for (final String paramType : op.getSignature().getParamTypeList()) {
                paramListStrBuild.append("<li>").append(SystemModelRepository.simpleHTMLEscape(paramType)).append("</li>");
            }
            final String[] cells = new String[]{
                    this.htmlEntityLabel(op.getId(), Integer.toString(op.getId()), EntityType.OPERATION),
                    this.htmlEntityRef(op.getComponentType().getId(), SystemModelRepository.simpleHTMLEscape(op.getComponentType().getFullQualifiedName()),
                            EntityType.COMPONENT_TYPE),
                    SystemModelRepository.simpleHTMLEscape(op.getSignature().getName()), paramListStrBuild.toString(),
                    SystemModelRepository.simpleHTMLEscape(op.getSignature().getReturnType()),};
            this.printHtmlTableRow(ps, cells);
        }
        this.printCloseHtmlTable(ps);
        this.printLinebreak(ps);
        this.printOpenHtmlTable(ps, "Assembly Components", new String[]{"ID", "Name", "Component type"});
        final Collection<AssemblyBasicComponent> assemblyComponents = this.assemblyFactory.getAssemblyComponentInstances();
        for (final AssemblyComponent ac : assemblyComponents) {
            final String[] cells = new String[]{
                    this.htmlEntityLabel(ac.getId(), Integer.toString(ac.getId()), EntityType.ASSEMBLY_COMPONENT),
                    ac.getName(),
                    this.htmlEntityRef(ac.getType().getId(), SystemModelRepository.simpleHTMLEscape(ac.getType().getFullQualifiedName()), EntityType.COMPONENT_TYPE),};
            this.printHtmlTableRow(ps, cells);
        }
        this.printCloseHtmlTable(ps);
        this.printLinebreak(ps);
        this.printOpenHtmlTable(ps, "Execution Containers", new String[]{"ID", "Name"});
        final Collection<ExecutionContainer> containers = this.executionEnvironmentFactory.getExecutionContainers();
        for (final ExecutionContainer container : containers) {
            final String[] cells = new String[]{
                    this.htmlEntityLabel(container.getId(), Integer.toString(container.getId()), EntityType.EXECUTION_CONTAINER),
                    SystemModelRepository.simpleHTMLEscape(container.getName()),};
            this.printHtmlTableRow(ps, cells);
        }
        this.printCloseHtmlTable(ps);
        this.printLinebreak(ps);
        this.printOpenHtmlTable(ps, "Deployment Components", new String[]{"ID", "Assembly component", "Execution container"});
        final Collection<AllocationBasicComponent> allocationComponentInstances = this.allocationFactory.getAllocationComponentInstances();
        for (final AllocationComponent allocationComponent : allocationComponentInstances) {
            final String[] cells = new String[]{
                    this.htmlEntityLabel(allocationComponent.getId(), Integer.toString(allocationComponent.getId()),
                            EntityType.ALLOCATION_COMPONENT),
                    this.htmlEntityRef(allocationComponent.getAssemblyComponent().getId(),
                            SystemModelRepository.simpleHTMLEscape(allocationComponent.getAssemblyComponent().toString()),
                            EntityType.ALLOCATION_COMPONENT),
                    this.htmlEntityRef(allocationComponent.getExecutionContainer().getId(),
                            SystemModelRepository.simpleHTMLEscape(allocationComponent.getExecutionContainer().getName()),
                            EntityType.EXECUTION_CONTAINER),};
            this.printHtmlTableRow(ps, cells);
        }
        this.printCloseHtmlTable(ps);
        this.printCloseSurroundingSpan(ps);
        this.htmlHSpace(ps, 50);
        ps.println("</body></html>");
        ps.flush();
        ps.close();
    }

    private void printOpenSurroundingSpan(final PrintStream ps) {
        ps.println("<span style=\"display: inline-block\">");
    }

    private void printCloseSurroundingSpan(final PrintStream ps) {
        ps.println("</span>");
    }

    private void printLinebreak(final PrintStream ps) {
        ps.println("<br/>");
    }

    private static String simpleHTMLEscape(final String input) {
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
    }

	public MessagingOperationRepository getMessagingOperationFactory() {
		return messagingOperationFactory;
	}

}
