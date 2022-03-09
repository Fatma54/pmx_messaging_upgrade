package org.palladiosimulator.pmxupgrade.pcm.config;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.metricspec.MetricSpecPackage;

import java.net.URL;
import java.util.Map;

public class StandaloneSetup {

    public static void init() {

        ResourceSet resourceSet = new ResourceSetImpl();

        if (!Platform.isRunning()) {
            MetricSpecPackage.eINSTANCE.getEFactoryInstance();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().putIfAbsent("metricspec", new XMIResourceFactoryImpl());
            resourceSet.getURIConverter().getURIMap().putIfAbsent(URI.createURI("pathmap://METRIC_SPEC_MODELS/commonMetrics.metricspec"), URI.createURI(Thread.currentThread().getContextClassLoader().getResource("commonMetrics.metricspec").toString()));
            resourceSet.getURIConverter().getURIMap().putIfAbsent(URI.createURI("pathmap://PCM_MODELS/PrimitiveTypes.repository"), URI.createURI(Thread.currentThread().getContextClassLoader().getResource("PrimitiveTypes.repository").toString()));
            resourceSet.getURIConverter().getURIMap().putIfAbsent(URI.createURI("pathmap://PCM_MODELS/Palladio.resourcetype"), URI.createURI(Thread.currentThread().getContextClassLoader().getResource("Palladio.resourcetype").toString()));
        }

    }

}
