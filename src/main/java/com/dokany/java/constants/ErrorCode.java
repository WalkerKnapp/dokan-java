package com.dokany.java.constants;

public enum ErrorCode {

	SUCCESS(0),

	ERROR_WRITE_FAULT(29),

	ERROR_READ_FAULT(30),

	ERROR_FILE_NOT_FOUND(0xc0000034),

	OBJECT_NAME_COLLISION(0xc0000035),

	ERROR_FILE_EXISTS(80),

	ERROR_ALREADY_EXISTS(183);

	private final int mask;

	ErrorCode(int mask){
		this.mask = mask;
	}

	public int getMask() {
		return mask;
	}
}
