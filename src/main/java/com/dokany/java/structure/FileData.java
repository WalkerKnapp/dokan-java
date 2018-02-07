package com.dokany.java.structure;

public final class FileData {
	private final byte[] bytes;
	private final int length;

	public FileData(byte[] bytes, int length){
		this.bytes = bytes;
		this.length = length;
	}

	public byte[] getBytes() {
		return bytes;
	}
	public int getLength() {
		return length;
	}
}
