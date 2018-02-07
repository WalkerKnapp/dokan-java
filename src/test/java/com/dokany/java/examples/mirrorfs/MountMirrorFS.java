package com.dokany.java.examples.mirrorfs;

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
 * Mounts MirrorFS at K:\\.
 */
public class MountMirrorFS {
	private static final Logger log = LoggerFactory.getLogger(MountMirrorFS.class);

	public static void main(final String[] args) throws Throwable {
		log.info("Starting Dokany MirrorFS");

		String mountPoint = "K:\\";
		final short threadCount = 1;
		EnumIntegerSet<MountOption> mountOptions = new EnumIntegerSet<>(MountOption.class);
		mountOptions.add(MountOption.DEBUG_MODE, MountOption.STD_ERR_OUTPUT, MountOption.MOUNT_MANAGER);
		String uncName = "";
		long timeout = 10000;
		long allocationUnitSize = 4096;
		long sectorSize = 4096;

		DeviceOptions deviceOptions = new DeviceOptions(mountPoint, threadCount, mountOptions, uncName, timeout, allocationUnitSize, sectorSize);

		EnumIntegerSet<FileSystemFeature> fsFeatures = new EnumIntegerSet<>(FileSystemFeature.class);
		fsFeatures.add(FileSystemFeature.CASE_PRESERVED_NAMES, FileSystemFeature.CASE_SENSITIVE_SEARCH,
		        FileSystemFeature.PERSISTENT_ACLS, FileSystemFeature.SUPPORTS_REMOTE_STORAGE, FileSystemFeature.UNICODE_ON_DISK);

		VolumeInformation volumeInfo = new VolumeInformation(VolumeInformation.DEFAULT_MAX_COMPONENT_LENGTH, "Mirror", 0x98765432, "Dokany MirrorFS", fsFeatures);
		FreeSpace freeSpace = new FreeSpace(200000, 200);

		MirrorFS mirrorFS = new MirrorFS(deviceOptions, volumeInfo, freeSpace, new Date(), "C:\\development");
		DokanyDriver dokanyDriver = new DokanyDriver(deviceOptions, mirrorFS);

		dokanyDriver.start();
	}
}
