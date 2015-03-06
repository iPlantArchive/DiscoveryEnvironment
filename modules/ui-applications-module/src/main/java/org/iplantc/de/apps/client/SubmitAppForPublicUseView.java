package org.iplantc.de.apps.client;

import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.apps.AppCategory;
import org.iplantc.de.client.models.apps.AppRefLink;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

import com.sencha.gxt.data.shared.TreeStore;

import java.util.List;

/**
 * @author jstroot
 */
public interface SubmitAppForPublicUseView extends IsWidget {

    interface SubmitAppAppearance {

        ImageResource categoryIcon();

        ImageResource categoryOpenIcon();

        String links();

        String publicNameNote();

        String publicName();

        String publicDescriptionNote();

        String publicSubmissionFormAttach();

        String publicSubmissionFormCategories();

        String publicSubmitTip();

        String publishFailureDefaultMessage();

        ImageResource subCategoryIcon();

        String submitForPublicUse();

        String submitForPublicUseIntro();

        String submitRequest();

        String submitting();

        String testDataLabel();

        String inputDescriptionEmptyText();

        String optionalParametersEmptyText();

        String outputDescriptionEmptyText();

        ImageResource addIcon();

        String add();

        String delete();

        ImageResource deleteIcon();

        SafeHtml publicDescription();

        String publicAttach();

        SafeHtml describeInputLbl();

        SafeHtml describeParamLbl();

        SafeHtml describeOutputLbl();

        SafeHtml publicCategories();

        String testDataWarn();

        String warning();
    }

    public interface Presenter extends org.iplantc.de.commons.client.presenter.Presenter {
        void onSubmit();

        void go(HasOneWidget container, App selectedApp, AsyncCallback<String> callback);
    }

    TreeStore<AppCategory> getTreeStore();

    void expandAppCategories();

    JSONObject toJson();

    App getSelectedApp();

    boolean validate();

    public void loadReferences(List<AppRefLink> refs);

    void setSelectedApp(App selectedApp);
}