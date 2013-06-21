package org.iplantc.admin.belphegor.client.apps.views.cells;

import static org.iplantc.core.uiapps.client.views.cells.AppHyperlinkCell.ELEMENT_NAME;

import org.iplantc.core.resources.client.messages.I18N;
import org.iplantc.core.uiapps.client.models.autobeans.App;
import org.iplantc.core.uiapps.client.views.AppsView;
import org.iplantc.core.uiapps.client.views.cells.AppHyperlinkCell;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class AppNameCell extends AppHyperlinkCell {

    public AppNameCell(AppsView view) {
        super(view);

    }

    @Override
    public void render(Cell.Context context, App value, SafeHtmlBuilder sb) {
        if (value == null) {
            return;
        }
        sb.appendHtmlConstant("&nbsp;");
        SafeHtml safeHtmlName = SafeHtmlUtils.fromString(value.getName());
        if (!value.isDisabled()) {
            sb.append(templates.cell(resources.css().appName(), safeHtmlName, I18N.DISPLAY.run(),
                    ELEMENT_NAME));
        } else {
            sb.append(templates.cell(resources.css().appDisabled(), safeHtmlName,
                    I18N.DISPLAY.appUnavailable(), ELEMENT_NAME));
        }

    }
}