package org.iplantc.de.diskResource.client.views.metadata.dialogs;

import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.util.DiskResourceUtil;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.views.dialogs.IPlantDialog;
import org.iplantc.de.diskResource.client.GridView;
import org.iplantc.de.diskResource.client.MetadataView;
import org.iplantc.de.diskResource.client.presenters.callbacks.DiskResourceMetadataUpdateCallback;
import org.iplantc.de.diskResource.client.presenters.metadata.MetadataPresenterImpl;
import org.iplantc.de.diskResource.client.views.metadata.DiskResourceMetadataViewImpl;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

import com.sencha.gxt.widget.core.client.event.SelectEvent;

/**
 * @author jstroot
 */
public class ManageMetadataDialog extends IPlantDialog {

    private class CancelSelectHandler implements SelectEvent.SelectHandler {
        @Override
        public void onSelect(SelectEvent event) {
            if(!writable){
                return;
            }
            hide();
        }
    }

    private class OkSelectHandler implements SelectEvent.SelectHandler {
        @Override
        public void onSelect(SelectEvent event) {
            if(!writable){
                return;
            }
            Preconditions.checkNotNull(mdPresenter);
            Preconditions.checkNotNull(mdView);

            if (mdView.shouldValidate()
                    && !mdView.isValid()) {
                ErrorAnnouncementConfig errNotice = new ErrorAnnouncementConfig(appearance.metadataFormInvalid());
                announcer.schedule(errNotice);
            } else {
                mdPresenter.setDiskResourceMetadata(new DiskResourceMetadataUpdateCallback(ManageMetadataDialog.this));
            }
        }
    }

    private final IplantAnnouncer announcer;
    private final GridView.Presenter.Appearance appearance;
    private MetadataView.Presenter mdPresenter;
    private MetadataView mdView;

    @Inject DiskResourceUtil diskResourceUtil;
    private boolean writable;

    @Inject
    ManageMetadataDialog(final IplantAnnouncer announcer,
                         final GridView.Presenter.Appearance appearance){
        super(true);
        this.announcer = announcer;
        this.appearance = appearance;
        setSize(appearance.metadataDialogWidth(), appearance.metadataDialogHeight());
        setResizable(true);
        addHelp(new HTML(appearance.metadataHelp()));
        addOkButtonSelectHandler(new OkSelectHandler());
        addCancelButtonSelectHandler(new CancelSelectHandler());
    }

    public void show(final DiskResource resource){
        mdView = new DiskResourceMetadataViewImpl(resource);
        mdPresenter = new MetadataPresenterImpl(resource, mdView);
        mdPresenter.go(this);
        writable = diskResourceUtil.isWritable(resource);
        if(writable){
            setHideOnButtonClick(false);
        }

        super.show();
    }

    @Override
    public void show() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method is not supported for this class. " +
                                                    "Use show(MetadataServiceFacade) instead.");
    }
}