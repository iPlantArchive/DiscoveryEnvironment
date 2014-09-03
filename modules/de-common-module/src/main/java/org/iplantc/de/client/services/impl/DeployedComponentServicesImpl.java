package org.iplantc.de.client.services.impl;

import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.GET;
import org.iplantc.de.client.models.DEProperties;
import org.iplantc.de.client.models.HasId;
import org.iplantc.de.client.models.deployedComps.DeployedComponent;
import org.iplantc.de.client.models.deployedComps.DeployedComponentAutoBeanFactory;
import org.iplantc.de.client.services.DeployedComponentServices;
import org.iplantc.de.client.services.converters.GetAppTemplateDeployedComponentConverter;
import org.iplantc.de.client.services.converters.GetDeployedComponentsCallbackConverter;
import org.iplantc.de.shared.services.DEServiceAsync;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import java.util.List;

public class DeployedComponentServicesImpl implements DeployedComponentServices {

    private final DeployedComponentAutoBeanFactory factory;
    private final DEProperties deProperties;
    private final DEServiceAsync deServiceFacade;

    @Inject
    public DeployedComponentServicesImpl(final DEServiceAsync deServiceFacade,
                                         final DEProperties deProperties,
                                         final DeployedComponentAutoBeanFactory factory) {
        this.deServiceFacade = deServiceFacade;
        this.deProperties = deProperties;
        this.factory = factory;
    }

    @Override
    public void getAppTemplateDeployedComponent(HasId appTemplateId, AsyncCallback<DeployedComponent> callback) {
        String address = deProperties.getMuleServiceBaseUrl() + "get-components-in-analysis/" + appTemplateId.getId();
        ServiceCallWrapper wrapper = new ServiceCallWrapper(GET, address);

        deServiceFacade.getServiceData(wrapper, new GetAppTemplateDeployedComponentConverter(callback, factory));
    }

    @Override
    public void getDeployedComponents(AsyncCallback<List<DeployedComponent>> callback) {
        GetDeployedComponentsCallbackConverter callbackCnvt = new GetDeployedComponentsCallbackConverter(callback, factory);
        ServiceCallWrapper wrapper = new ServiceCallWrapper("org.iplantc.services.zoidberg.components"); //$NON-NLS-1$

        deServiceFacade.getServiceData(wrapper, callbackCnvt);
    }

    @Override
    public void searchDeployedComponents(String searchTerm, AsyncCallback<List<DeployedComponent>> callback) {
        GetDeployedComponentsCallbackConverter callbackCnvt = new GetDeployedComponentsCallbackConverter(callback, factory);

        String address = deProperties.getUnproctedMuleServiceBaseUrl() + "search-deployed-components/" + URL.encodeQueryString(searchTerm);
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        deServiceFacade.getServiceData(wrapper, callbackCnvt);
    }

}
