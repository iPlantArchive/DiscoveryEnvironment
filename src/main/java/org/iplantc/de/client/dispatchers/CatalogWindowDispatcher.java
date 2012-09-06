package org.iplantc.de.client.dispatchers;

import org.iplantc.core.uiapplications.client.models.CatalogWindowConfig;
import org.iplantc.de.client.Constants;
import org.iplantc.de.client.factories.WindowConfigFactory;

import com.google.gwt.json.client.JSONObject;

/**
 * A WindowDispatcher that can launch the DECatalogWindow with the Category and App selected for any
 * given IDs.
 * 
 * @author psarando
 * 
 */
public class CatalogWindowDispatcher extends WindowDispatcher {

    /**
     * Launches the DECatalogWindow with the Category and App selected for the given IDs. If an ID is
     * null, then the default for that category is selected.
     * 
     * @param selectedCategoryId
     * @param selectedAppId
     */
    public void launchCatalogWindow(String selectedCategoryId, String selectedAppId) {
        WindowConfigFactory configFactory = new WindowConfigFactory();
        CatalogWindowConfig config = new CatalogWindowConfig(new JSONObject());

        if (selectedCategoryId != null) {
            config.setCategoryId(selectedCategoryId);
        }
        if (selectedAppId != null) {
            config.setAppId(selectedAppId);
        }

        JSONObject windowConfig = configFactory.buildWindowConfig(Constants.CLIENT.deCatalog(), config);

        // Dispatch window display action with this config
        setConfig(windowConfig);
        dispatchAction(Constants.CLIENT.deCatalog());
    }
}
