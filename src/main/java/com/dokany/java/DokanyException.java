package com.dokany.java;

import java.io.IOException;

import com.dokany.java.constants.ErrorCode;
import com.dokany.java.constants.WinError;

public final class DokanyException extends RuntimeException {
	private final long serialVersionUID = -862591089502909563L;

	private final int value;

	public DokanyException(final long errorCode, final IOException exception) {
		if ((errorCode < 0) || (errorCode > 4294967295L)) {
			throw new IllegalArgumentException("error code (" + errorCode + ") is not in range [0, 4294967295]", exception);
		}
		value = (int) errorCode;
	}

	public DokanyException(final WinError errorCode, final IOException exception) {
		this(errorCode.getMask(), exception);
	}

	public DokanyException(final ErrorCode errorCode, final IOException exception) {
		this(errorCode.getMask(), exception);
	}

	public int getValue() {
		return value;
	}

	public long getSerialVersionUID() {
		return serialVersionUID;
	}
}