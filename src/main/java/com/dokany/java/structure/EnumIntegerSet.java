package com.dokany.java.structure;

import java.util.AbstractSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;

import com.dokany.java.constants.EnumInteger;

/**
 * Used to store multiple enum values such as {@link com.dokany.java.constants.FileSystemFeature} and {@link com.dokany.java.constants.MountOption}.
 *
 * @param <T> Type of enum
 */
public final class EnumIntegerSet<T extends Enum<T>> extends AbstractSet<T> {

	private final EnumSet<T> elements;

	public EnumIntegerSet(final Class<T> clazz) {
		if (!EnumInteger.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Items must all implement EnumInteger");
		}
		elements = EnumSet.noneOf(clazz);
	}

	@SafeVarargs
	public final void add(final T... items) {
		if (Objects.isNull(items) || (items.length < 1)) {
			throw new IllegalArgumentException("items array cannot be empty");
		}

		for (T item : items) {
			if (Objects.nonNull(item)) {
				elements.add(item);
			}
		}
	}

	public int toInt() {
		int toReturn = 0;
		for (T current : elements) {
			// Already checked (in constructor) to ensure only objects which implement EnumInteger are stored in values
			EnumInteger enumInt = (EnumInteger) current;
			toReturn |= enumInt.getMask();
		}
		return toReturn;
	}

	@Override
	public boolean add(final T e) {
		return elements.add(e);
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public String toString() {
		return "EnumIntegerSet{" +
				"elements=" + elements +
				'}';
	}
}
