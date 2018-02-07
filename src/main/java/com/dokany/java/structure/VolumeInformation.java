package com.dokany.java.structure;

import com.dokany.java.constants.FileSystemFeature;

public class VolumeInformation {

	private final int maxComponentLength;
	private final String name;
	private final int serialNumber;
	private final String fileSystemName;
	private final EnumIntegerSet<FileSystemFeature> fileSystemFeatures;

	public static final int DEFAULT_MAX_COMPONENT_LENGTH = 256;
	public static final int DEFAULT_SERIAL_NUMBER = 0x12345678;
	public static final String DEFAULT_VOLUME_NAME = "VOLUME1";
	public static final String DEFAULT_FS_NAME = "DOKANY";
	public static final EnumIntegerSet<FileSystemFeature> DEFAULT_FS_FEATURES = new EnumIntegerSet<>(FileSystemFeature.class);

	static {
		DEFAULT_FS_FEATURES.add(FileSystemFeature.CASE_PRESERVED_NAMES);
	}

	public VolumeInformation(int maxComponentLength, String name, int serialNumber, String fileSystemName, EnumIntegerSet<FileSystemFeature> fileSystemFeatures){
		this.maxComponentLength = maxComponentLength;
		this.name = name;
		this.serialNumber = serialNumber;
		this.fileSystemName = fileSystemName;
		this.fileSystemFeatures = fileSystemFeatures;
	}

	/**
	 * Provides default values for maxComponentLength and fileSystemFeatures.
	 *
	 * @param volumeName
	 * @param serialNumber
	 * @param fileSystemName
	 */
	public VolumeInformation(final String volumeName, final int serialNumber, final String fileSystemName) {
		this(DEFAULT_MAX_COMPONENT_LENGTH, volumeName, serialNumber, fileSystemName, DEFAULT_FS_FEATURES);
	}

	/**
	 * Provides default values for all values.
	 */
	public VolumeInformation() {
		this(DEFAULT_MAX_COMPONENT_LENGTH, DEFAULT_VOLUME_NAME, DEFAULT_SERIAL_NUMBER, DEFAULT_FS_NAME, DEFAULT_FS_FEATURES);
	}

	public int getMaxComponentLength() {
		return maxComponentLength;
	}

	public String getName() {
		return name;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public String getFileSystemName() {
		return fileSystemName;
	}

	public EnumIntegerSet<FileSystemFeature> getFileSystemFeatures() {
		return fileSystemFeatures;
	}
}
