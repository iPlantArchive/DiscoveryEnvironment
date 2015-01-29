package org.iplantc.de.theme.base.client.diskResource.grid.cells;

import org.iplantc.de.diskResource.client.views.grid.cells.DiskResourceFavoriteCell;
import org.iplantc.de.resources.client.FavoriteCellStyle;
import org.iplantc.de.resources.client.FavoriteTemplates;
import org.iplantc.de.resources.client.IplantResources;
import org.iplantc.de.resources.client.messages.IplantDisplayStrings;
import org.iplantc.de.theme.base.client.diskResource.grid.GridViewDisplayStrings;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author jstroot
 */
public class DiskResourceFavoriteCellDefaultAppearance implements DiskResourceFavoriteCell.Appearance {
    private final FavoriteTemplates favoriteTemplates;
    private final FavoriteCellStyle favoriteCellStyle;
    private final IplantDisplayStrings iplantDisplayStrings;
    private final GridViewDisplayStrings displayStrings;

    public DiskResourceFavoriteCellDefaultAppearance() {
        this(GWT.<FavoriteTemplates> create(FavoriteTemplates.class),
             GWT.<IplantResources> create(IplantResources.class),
             GWT.<IplantDisplayStrings> create(IplantDisplayStrings.class),
             GWT.<GridViewDisplayStrings> create(GridViewDisplayStrings.class));
    }

    DiskResourceFavoriteCellDefaultAppearance(final FavoriteTemplates favoriteTemplates,
                                              final IplantResources iplantResources,
                                              final IplantDisplayStrings iplantDisplayStrings,
                                              final GridViewDisplayStrings displayStrings) {
        this.favoriteTemplates = favoriteTemplates;
        this.favoriteCellStyle = iplantResources.favoriteCss();
        this.iplantDisplayStrings = iplantDisplayStrings;
        this.displayStrings = displayStrings;

        this.favoriteCellStyle.ensureInjected();
    }


    @Override
    public String addFavoriteClass() {
        return favoriteCellStyle.favoriteAdd();
    }

    @Override
    public String addToFavoriteTooltip() {
        return iplantDisplayStrings.addAppToFav();
    }

    @Override
    public String deleteFavoriteClass() {
        return favoriteCellStyle.favoriteDelete();
    }

    @Override
    public String favoriteClass() {
        return favoriteCellStyle.favorite();
    }

    @Override
    public String favoriteDisabledClass() {
        return favoriteCellStyle.favoriteDisabled();
    }

    @Override
    public String removeFromFavoriteTooltip() {
        return iplantDisplayStrings.remAppFromFav();
    }

    @Override
    public void render(final SafeHtmlBuilder sb,
                       final String imgName,
                       final String imgClassName,
                       final String imgToolTip,
                       final String baseID,
                       final String debugId) {

        if(DebugInfo.isDebugIdEnabled()
            && !Strings.isNullOrEmpty(baseID)){
            sb.append(favoriteTemplates.debugCell(imgName, imgClassName, imgToolTip, debugId));
        } else {
            sb.append(favoriteTemplates.cell(imgName, imgClassName, imgToolTip));
        }
    }
}
