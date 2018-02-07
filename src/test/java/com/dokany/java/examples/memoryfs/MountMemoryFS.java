package com.dokany.java.examples.memoryfs;

import java.util.Date;

import com.dokany.java.DokanyDriver;
import com.dokany.java.constants.FileSystemFeature;
import com.dokany.java.constants.MountOption;
import com.dokany.java.structure.DeviceOptions;
import com.dokany.java.structure.EnumIntegerSet;
import com.dokany.java.structure.FreeSpace;
import com.dokany.java.structure.VolumeInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mounts MemoryFS at M:\\.
 */
public class MountMemoryFS {
	private static final Logger log = LoggerFactory.getLogger(MountMemoryFS.class);

	public static void main(final String[] args) throws Throwable {
		log.info("Starting Dokany MemoryFS");

		String mountPoint = "M:\\";
		final short threadCount = 1;
		EnumIntegerSet<MountOption> mountOptions = new EnumIntegerSet<>(MountOption.class);
		mountOptions.add(MountOption.DEBUG_MODE, MountOption.STD_ERR_OUTPUT, MountOption.MOUNT_MANAGER);
		String uncName = "";
		long timeout = 10000;
		long allocationUnitSize = 4096;
		long sectorSize = 4096;

		DeviceOptions deviceOptions = new DeviceOptions(mountPoint, threadCount, mountOptions, uncName, timeout, allocationUnitSize, sectorSize);

		EnumIntegerSet<FileSystemFeature> fsFeatures = new EnumIntegerSet<>(FileSystemFeature.class);
		fsFeatures.add(FileSystemFeature.CASE_PRESERVED_NAMES, FileSystemFeature.CASE_SENSITIVE_SEARCH, FileSystemFeature.UNICODE_ON_DISK);

		VolumeInformation volumeInfo = new VolumeInformation(VolumeInformation.DEFAULT_MAX_COMPONENT_LENGTH, "Memory", 0x234234, "MemoryFS", fsFeatures);
		FreeSpace freeSpace = new FreeSpace(1024L * 1024L * 1024L, 1024L);

		MemoryFS memoryFS = new MemoryFS(deviceOptions, volumeInfo, freeSpace, new Date(), "/");

		DokanyDriver dokanyDriver = new DokanyDriver(deviceOptions, memoryFS);
		dokanyDriver.start();
	}
}