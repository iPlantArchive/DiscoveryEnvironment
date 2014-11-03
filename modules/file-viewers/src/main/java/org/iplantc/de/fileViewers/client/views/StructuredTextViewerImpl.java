package org.iplantc.de.fileViewers.client.views;

import static org.iplantc.de.resources.client.messages.I18N.DISPLAY;
import static org.iplantc.de.resources.client.messages.I18N.ERROR;
import org.iplantc.de.client.events.FileSavedEvent;
import org.iplantc.de.client.gin.ServicesInjector;
import org.iplantc.de.client.models.diskResources.File;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.client.models.viewer.StructuredText;
import org.iplantc.de.client.models.viewer.StructuredTextAutoBeanFactory;
import org.iplantc.de.client.services.FileEditorServiceFacade;
import org.iplantc.de.client.util.JsonUtil;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.diskResource.client.views.dialogs.SaveAsDialog;
import org.iplantc.de.fileViewers.client.callbacks.FileSaveCallback;
import org.iplantc.de.resources.client.messages.IplantDisplayStrings;
import org.iplantc.de.resources.client.messages.IplantErrorStrings;

import com.google.common.base.Joiner;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.RowNumberer;
import com.sencha.gxt.widget.core.client.grid.editing.ClicksToEdit;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * FIXME JDS Need to extract this into a ui.xml
 * @author sriram, jstroot
 */
public class StructuredTextViewerImpl extends StructuredTextViewer {

    private final class StructuredTextValueProvider implements ValueProvider<JSONObject, String> {
        private final int index;

        private StructuredTextValueProvider(int index) {
            this.index = index;
        }

        @Override
        public String getPath() {
            return index + "";
        }

        @Override
        public String getValue(JSONObject object) {
            JSONValue val = object.get(index + "");
            if (val != null) {
                return val.isString().stringValue();
            } else {
                return "";
            }
        }

        @Override
        public void setValue(JSONObject object, String value) {
            if (value == null) {
                value = "";
            }
            object.put(index + "", new JSONString(value));
        }
    }

    Logger LOG = Logger.getLogger(StructuredTextViewerImpl.class.getName());

    private final String COMMA_SEPARATOR = ",";
    private final String NEW_LINE = "\n";
    private final String TAB_SEPARATOR = "\t";

    private final IplantDisplayStrings displayStrings = DISPLAY;
    private final IplantErrorStrings errorStrings = ERROR;
    private final StructuredTextAutoBeanFactory factory = GWT.create(StructuredTextAutoBeanFactory.class);
    private final FileEditorServiceFacade fileEditorService = ServicesInjector.INSTANCE.getFileEditorServiceFacade();

    private final Folder parentFolder;
    private final FileViewer.Presenter presenter;
    private VerticalLayoutContainer center;
    private int columns;
    private BorderLayoutContainer container;
    private boolean dirty;
    private Grid<JSONObject> grid;
    private boolean hasHeader;
    private JSONObject headerRow;
    private ContentPanel north;
    private RowNumberer<JSONObject> numberer;
    private ViewerPagingToolBar pagingToolbar;
    private GridInlineEditing<JSONObject> rowEditing;
    private List<JSONObject> skippedRows;
    private ContentPanel south;
    private ListStore<JSONObject> store;
    private StructuredText text_bean;
    private StructuredTextViewToolBar toolbar;

    public StructuredTextViewerImpl(final File file,
                                    final String infoType,
                                    final Integer columns,
                                    final Folder parentFolder,
                                    final FileViewer.Presenter presenter) {
        super(file, infoType);
        this.parentFolder = parentFolder;
        this.presenter = presenter;
        initLayout();
        initToolbar();
        initPagingToolbar();
        loadData();
        if (columns != null) {
            LOG.info("Columns: " + columns);
            initGrid(columns);
            setEditing(true);
            addRow();
        }
        addLineNumberHandler();
    }


    @Override
    public HandlerRegistration addFileSavedEventHandler(final FileSavedEvent.FileSavedEventHandler handler) {
        return asWidget().addHandler(handler, FileSavedEvent.TYPE);
    }

    @Override
    public void addRow() {
        JSONObject obj = new JSONObject();
        for (int i = 0; i < columns; i++) {
            obj.put(i + "", new JSONString("col" + i));
        }
        if (rowEditing != null) {
            rowEditing.cancelEditing();
            getStore().add(obj);
            int row = getStore().indexOf(obj);
            setDirty(true);
            rowEditing.startEditing(new GridCell(row, 1));
        }
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    @Override
    public void deleteRow() {
        List<JSONObject> selectedRows = grid.getSelectionModel().getSelectedItems();
        if (selectedRows != null && selectedRows.size() > 0) {
            for (JSONObject obj : selectedRows) {
                getStore().remove(obj);
            }
            setDirty(true);
        }
    }

    @Override
    public String getViewName() {
        if (file != null) {
            return "Tabular View: " + file.getName();
        } else {
            return "Tabular View";
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(Boolean dirty) {
        this.dirty = dirty;
        if (presenter.isDirty() == dirty) {
            return;
        }
        presenter.setViewDirtyState(dirty);
    }

    @Override
    public void loadData() {
        String url = "read-csv-chunk";
        if (file != null) {
            container.mask(displayStrings.loadingMask());
            fileEditorService.getDataChunk(url, getRequestBody(), new AsyncCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    AutoBean<StructuredText> bean = AutoBeanCodex.decode(factory,
                                                                         StructuredText.class,
                                                                         result);
                    text_bean = bean.as();
                    if (grid == null) {
                        initGrid(Integer.parseInt(text_bean.getMaxColumns()));
                    }
                    Splittable sp = StringQuoter.split(result);
                    setData(sp);
                    setEditing(pagingToolbar.getToltalPages() == 1);
                    container.unmask();
                }

                @Override
                public void onFailure(Throwable caught) {
                    ErrorHandler.post(errorStrings.unableToRetrieveFileData(file.getName()),
                                      caught);
                    container.unmask();
                }
            });
        }
    }

    @Override
    public void loadDataWithHeader(boolean header) {
        hasHeader = header;
        defineColumnHeader();
    }

    @Override
    public void refresh() {
        loadData();
    }

    @Override
    public void save() {
        store.commitChanges();
        container.mask(displayStrings.savingMask());
        if (file == null) {
            final SaveAsDialog saveDialog = new SaveAsDialog(parentFolder);
            saveDialog.addOkButtonSelectHandler(new SelectHandler() {

                @Override
                public void onSelect(SelectEvent event) {
                    if (saveDialog.isVaild()) {

                        String destination = saveDialog.getSelectedFolder().getPath() + "/"
                                                 + saveDialog.getFileName();
                        fileEditorService.uploadTextAsFile(destination,
                                                           getEditorContent(),
                                                           true,
                                                           new FileSaveCallback(destination,
                                                                                true,
                                                                                container));
                        saveDialog.hide();
                    }
                }
            });
            saveDialog.addCancelButtonSelectHandler(new SelectHandler() {

                @Override
                public void onSelect(SelectEvent event) {
                    saveDialog.hide();
                    container.unmask();
                }
            });
            saveDialog.show();
            saveDialog.toFront();
        } else {
            fileEditorService.uploadTextAsFile(file.getPath(),
                                               getEditorContent(),
                                               false,
                                               new FileSaveCallback(file.getPath(),
                                                                    false,
                                                                    container));
        }
    }

    @Override
    public void setData(Object data) {
        Splittable textData = (Splittable) data;
        JSONObject obj = JsonUtil.getObject(textData.getPayload());
        JSONArray arr = obj.get("csv").isArray();

        if (arr != null && arr.size() > 0) {
            store.clear();
            for (int i = 0; i < arr.size(); i++) {
                store.add(arr.get(i).isObject());
            }
        }

        if (pagingToolbar.getPageNumber() == 1) {
            skipRows(toolbar.getSkipRowCount());
            if (hasHeader) {
                if (headerRow == null) {
                    defineColumnHeader();
                } else {
                    // just remove the first row bcos header is set
                    store.remove(0);
                }
            }
        }

    }

    @Override
    public void skipRows(int val) {
        if (val > 0 && val < store.size() + ((skippedRows != null) ? skippedRows.size() : 0)) {
            if (skippedRows == null) {
                skippedRows = new ArrayList<>();
                // increment
                skippedRows.addAll(store.subList(0, val));
            } else if (skippedRows.size() > val) {
                // reduction
                for (int j = skippedRows.size() - 1; j >= val; j--) {
                    store.add(0, skippedRows.remove(j));
                }
            } else if (skippedRows.size() < val) {
                // increment
                skippedRows.addAll(store.subList(0, val - skippedRows.size()));
            } else {
                // same size
                skippedRows.clear();
                skippedRows.addAll(store.subList(0, val));
            }
            for (JSONObject obj : skippedRows) {
                store.remove(obj);
            }

        } else {
            if (val == 0 && skippedRows != null && skippedRows.size() > 0) {
                // add back the skipped rows.
                for (int j = 0; j < skippedRows.size(); j++) {
                    store.add(j, skippedRows.get(j));
                }
            }
            skippedRows = null;
        }

        grid.getView().refresh(true);

    }

    private void addLineNumberHandler() {
        toolbar.addLineNumberCbxChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                numberer.setHidden(!event.getValue());
                grid.getView().refresh(false);
            }

        });
    }

    private void defineColumnHeader() {
        ColumnModel<JSONObject> cm = grid.getColumnModel();
        List<ColumnConfig<JSONObject, ?>> configs = cm.getColumns();
        JSONObject firstRow = store.get(0);
        int index = 0;
        for (ColumnConfig<JSONObject, ?> conf : configs) {
            if (cm.indexOf(conf) != 0) { // col 0 is numberer
                JSONString string = (firstRow.get(index + "") != null) ? firstRow.get(index + "")
                                                                                 .isString() : null;
                if (hasHeader) {
                    conf.setHeader((string != null) ? string.stringValue() : index + "");
                } else {
                    conf.setHeader(index + "");
                }
                index++;
            }

        }

        // converted first row to header. so remove first row
        if (hasHeader) {
            store.remove(firstRow);
            headerRow = firstRow;
        } else {
            if (headerRow != null) {
                // if it was removed prev, add the back row at 1st position
                store.add(0, headerRow);
                headerRow = null;
            }
        }

        grid.reconfigure(store, cm);
        grid.getView().refresh(true);
    }

    private MarginData getCenterData() {
        return new MarginData();
    }

    private String getEditorContent() {
        StringBuilder sw = new StringBuilder();
        Joiner joiner = Joiner.on(getSeparator()).skipNulls();
        if (skippedRows != null && skippedRows.size() > 0) {
            for (JSONObject skipr : skippedRows) {
                joiner.appendTo(sw, jsonToStringList(skipr));
                sw.append(NEW_LINE);
            }
        }
        if (headerRow != null) {
            joiner.appendTo(sw, jsonToStringList(headerRow));
            sw.append(NEW_LINE);
        }
        for (JSONObject obj : getStore().getAll()) {
            joiner.appendTo(sw, jsonToStringList(obj));
            sw.append(NEW_LINE);
        }
        return sw.toString();
    }

    private BorderLayoutData getNorthData() {
        BorderLayoutData northData = new BorderLayoutData(30);
        northData.setMargins(new Margins(5));
        northData.setCollapsible(false);
        northData.setSplit(false);
        return northData;
    }

    private JSONObject getRequestBody() {
        JSONObject obj = new JSONObject();
        obj.put("path", new JSONString(file.getPath()));
        obj.put("separator", new JSONString(URL.encode(getSeparator())));
        obj.put("page", new JSONString(pagingToolbar.getPageNumber() + ""));
        obj.put("chunk-size", new JSONString("" + pagingToolbar.getPageSize()));
        return obj;
    }

    private String getSeparator() {
        if (infoType.equalsIgnoreCase("csv")) {
            return COMMA_SEPARATOR;
        } else if (infoType.equalsIgnoreCase("tsv") || infoType.equalsIgnoreCase("vcf")
                       || infoType.equalsIgnoreCase("gff")) {
            return TAB_SEPARATOR;
        } else {
            return " ";
        }
    }

    private BorderLayoutData getSouthData() {
        BorderLayoutData southData = new BorderLayoutData(30);
        southData.setMargins(new Margins(5));
        southData.setCollapsible(false);
        southData.setSplit(false);
        return southData;
    }

    private ListStore<JSONObject> getStore() {
        if (store == null) {
            store = new ListStore<>(new ModelKeyProvider<JSONObject>() {

                private int index;

                @Override
                public String getKey(JSONObject item) {
                    return index++ + "";
                }

            });
        }

        return store;
    }

    private void initGrid(int columns) {
        List<ColumnConfig<JSONObject, ?>> configs = new ArrayList<>();
        numberer = new RowNumberer<>();
        numberer.setHidden(true);
        configs.add(numberer);
        GridFilters<JSONObject> filters = new GridFilters<>();
        if (columns > 0) {
            this.columns = columns;
            for (int i = 0; i < columns; i++) {
                StructuredTextValueProvider valueProvider = new StructuredTextValueProvider(i);
                ColumnConfig<JSONObject, String> col = new ColumnConfig<>(valueProvider);
                col.setHeader(i + "");
                StringFilter<JSONObject> strFilter = new StringFilter<>(valueProvider);
                filters.addFilter(strFilter);
                configs.add(col);
            }
        }

        grid = new Grid<>(getStore(), new ColumnModel<>(configs));
        grid.setSelectionModel(new CellSelectionModel<JSONObject>());
        grid.getView().setStripeRows(true);
        grid.getView().setTrackMouseOver(true);
        filters.initPlugin(grid);
        filters.setLocal(true);
        grid.setHeight(center.getOffsetHeight(true));
        center.add(grid, new VerticalLayoutData(1, 1, new Margins(0)));
    }

    private void initLayout() {
        container = new BorderLayoutContainer();
        north = new ContentPanel();
        north.setCollapsible(false);
        north.setHeaderVisible(false);

        south = new ContentPanel();
        south.setCollapsible(false);
        south.setHeaderVisible(false);

        center = new VerticalLayoutContainer();
        center.setScrollMode(ScrollMode.AUTO);

        container.setNorthWidget(north, getNorthData());
        container.setCenterWidget(center, getCenterData());
        container.setSouthWidget(south, getSouthData());
    }

    private void initPagingToolbar() {
        pagingToolbar = new ViewerPagingToolBar(this, getFileSize());
        south.add(pagingToolbar);
    }

    private void initToolbar() {
        toolbar = new StructuredTextViewToolBar(this, false);
        north.add(toolbar);
    }

    private List<String> jsonToStringList(JSONObject obj) {
        List<String> strList = new ArrayList<>();
        for (String key : obj.keySet()) {
            JSONValue val = obj.get(key);
            if (val != null && val.isString() != null) {
                strList.add(val.isString().stringValue());
            }
        }

        return strList;
    }

    @SuppressWarnings("unchecked")
    private void setEditing(boolean editing) {
        if (grid == null) {
            return;
        }

        toolbar.setEditing(editing);

        if (editing) {
            if (rowEditing == null) {
                // Initialize row editing
                grid.setToolTip("Double click to edit...");
                rowEditing = new GridInlineEditing<>(grid);
                rowEditing.setClicksToEdit(ClicksToEdit.TWO);
                rowEditing.addCompleteEditHandler(new CompleteEditHandler<JSONObject>() {

                    @Override
                    public void onCompleteEdit(CompleteEditEvent<JSONObject> event) {
                        dirty = true;
                        store.commitChanges();
                    }
                });

                List<ColumnConfig<JSONObject, ?>> cols = grid.getColumnModel().getColumns();
                for (ColumnConfig<JSONObject, ?> cc : cols) {
                    TextField field = new TextField();
                    field.setClearValueOnParseError(false);
                    rowEditing.addEditor((ColumnConfig<JSONObject, String>) cc, field);
                }
            }
        } else {
            rowEditing = null;
            grid.removeToolTip();
        }
    }
}