package com.clean.space.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.clean.space.Constants;

public class ItemComparator {
	public static List<Object> sort(List<Object> list, int type) {
		List<Object> temp = new ArrayList<Object>(list);
		Comparator<Object> comparator = null;
		
		switch (type) {
		case Constants.SORT_TYPE_DATE_ASC: {
			comparator = new ComparatorDateAsc();
			break;
		}
		case Constants.SORT_TYPE_DATE_DESC: {
			comparator = new ComparatorDateDesc();
			break;
		}
		case Constants.SORT_TYPE_SIZE_ASC: {
			comparator = new ComparatorSizeAsc();
			break;
		}
		case Constants.SORT_TYPE_SIZE_DESC: {
			comparator = new ComparatorSizeDesc();
			break;
		}
		default: {
			return null;
		}
		}
		
		Collections.sort(temp, comparator);
		return temp;
	}
	
	private static class ComparatorDateAsc implements Comparator<Object> {

		@Override
		public int compare(Object lhs, Object rhs) {
			long value = 0;
			if (lhs instanceof FileItem) {
				value = ((FileItem)lhs).getDate() - ((FileItem)rhs).getDate();
			} else if (lhs instanceof ExportedImageItem) {
				value = ((ExportedImageItem)lhs).getDate() - ((ExportedImageItem)rhs).getDate();
			}
			
			if (value > 0) {
				return 1;
			} else if (value < 0) {
				return -1;
			} else {
				return 0;
			}
		}

	}
	
	private static class ComparatorDateDesc implements Comparator<Object> {

		@Override
		public int compare(Object lhs, Object rhs) {
			long value = 0;			
			if (lhs instanceof FileItem) {
				value =  ((FileItem)lhs).getDate() - ((FileItem)rhs).getDate();
			} else if (lhs instanceof ExportedImageItem) {
				value =  ((ExportedImageItem)lhs).getDate() - ((ExportedImageItem)rhs).getDate();
			}
			
			if (value > 0) {
				return -1;
			} else if (value < 0) {
				return 1;
			} else {
				return 0;
			}
		}

	}
	
	private static class ComparatorSizeAsc implements Comparator<Object> {

		@Override
		public int compare(Object lhs, Object rhs) {
			long value = 0;
			if (lhs instanceof FileItem) {
				value = ((FileItem)lhs).getSize() - ((FileItem)rhs).getSize();
			} else if (lhs instanceof ExportedImageItem) {
				value = ((ExportedImageItem)lhs).getSize() - ((ExportedImageItem)rhs).getSize();
			}
			
			if (value > 0) {
				return 1;
			} else if (value < 0) {
				return -1;
			} else {
				return 0;
			}
		}

	}
	
	private static class ComparatorSizeDesc implements Comparator<Object> {

		@Override
		public int compare(Object lhs, Object rhs) {
			long value = 0;
			if (lhs instanceof FileItem) {
				value = ((FileItem)lhs).getSize() - ((FileItem)rhs).getSize();
			} else if (lhs instanceof ExportedImageItem) {
				value = ((ExportedImageItem)lhs).getSize() - ((ExportedImageItem)rhs).getSize();
			}
			
			if (value > 0) {
				return -1;
			} else if (value < 0) {
				return 1;
			} else {
				return 0;
			}
		}

	}
}
