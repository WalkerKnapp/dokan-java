package com.dokany.java.structure;

public class FreeSpace {
	private final long totalBytes;
	private final long totalUsed;

	public FreeSpace(long totalBytes, long totalUsed){
		this.totalBytes = totalBytes;
		this.totalUsed = totalUsed;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public long getTotalUsed() {
		return totalUsed;
	}

	public long getFreeBytes() {
		return totalBytes - totalUsed;
	}
}
