package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;

import model.AbstractViewRowDataModel;
import model.CategoryModel;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.TabularDataModel;


/**
 * This class wraps a provided {@link List} of arbitrary Java beans to provide an appropriate
 * interface for data views, view panels, etc.
 * 
 * @author Jesse Gallagher
 */
public class DataModelTabularListAdapter extends TabularDataModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<AbstractViewRowDataModel> list;
	private Set<String> selectedIds = new HashSet<String>();
	private final String documentIdProp;
	private final Set<String> categories = new HashSet<String>();

	@SuppressWarnings("unchecked")
	public DataModelTabularListAdapter(final Collection<AbstractViewRowDataModel> list, String documentIdProp) {
		this(list, documentIdProp, Collections.EMPTY_LIST);
	}
	public DataModelTabularListAdapter(final Collection<AbstractViewRowDataModel> list, String documentIdProp, Collection<String> categories) {
		this.list = new ArrayList<AbstractViewRowDataModel>(list);
		this.documentIdProp = documentIdProp;
		this.categories.addAll(categories);
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public AbstractViewRowDataModel getRowData() {
		return list.get(getRowIndex());
	}

	@Override
	public void addSelectedId(final String id) {
		this.selectedIds.add(id);
	}

	@Override
	public void removeSelectedId(final String id) {
		this.selectedIds.remove(id);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getSelectedIds() {
		return selectedIds.iterator();
	}

	@Override
	public void clearSelectedIds() {
		this.selectedIds.clear();
	}

	@Override
	public boolean isSelectedId(final String id) {
		return selectedIds.contains(id);
	}

	public List<AbstractViewRowDataModel> getSelectedObjects() {
		List<AbstractViewRowDataModel> result = new ArrayList<AbstractViewRowDataModel>(this.selectedIds.size());
		for(AbstractViewRowDataModel obj : list) {
			if(selectedIds.contains(obj.getValue(documentIdProp))) {
				result.add(obj);
			}
		}
		return result;
	}

	@Override
	public String getRowId() {
		int rowIndex = getRowIndex();
		if(rowIndex >= 0) {
			AbstractViewRowDataModel obj = list.get(rowIndex);
			return StringUtil.toString(obj.getValue(documentIdProp));
		} else {
			return null;
		}
	}
	
	@Override
	public int getRowType() {
		int rowIndex = getRowIndex();
		if(rowIndex >= 0) {
			DataModel adapter = list.get(rowIndex);
			if(adapter instanceof CategoryModel) {
				return TabularDataModel.TYPE_CATEGORY;
			} else {
				return TabularDataModel.TYPE_ENTRY;
			}
		} else {
			return super.getRowType();
		}
	}
	
	@Override
	public boolean isColumnCategorized(String columnName) {
		return this.categories.contains(columnName);
	}
	
	@Override
	public boolean isRowCollapsed() {
		return false;
	}
}