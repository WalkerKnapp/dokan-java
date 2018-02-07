package com.dokany.java;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.dokany.java.constants.FileAttribute;
import com.dokany.java.constants.MountOption;
import com.dokany.java.structure.DeviceOptions;
import com.dokany.java.structure.DokanyFileInfo;
import com.dokany.java.structure.EnumIntegerSet;
import com.dokany.java.structure.FileData;
import com.dokany.java.structure.FreeSpace;
import com.dokany.java.structure.FullFileInfo;
import com.dokany.java.structure.VolumeInformation;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinBase.WIN32_FIND_DATA;

/**
 * This should be extended by file system providers.
 */
public abstract class DokanyFileSystem {
	protected final VolumeInformation volumeInfo;
	protected final FreeSpace freeSpace;
	protected final long allocationUnitSize;
	protected final long sectorSize;
	protected final long timeout;
	protected final Date rootCreationDate;
	protected final String root;
	protected final boolean isDebug;
	protected final boolean isDebugStdErr;

	public DokanyFileSystem(
	        final DeviceOptions deviceOptions,
	        final VolumeInformation volumeInfo,
	        final FreeSpace freeSpace,
	        final Date rootCreationDate,
	        final String rootPath) {
		this.volumeInfo = volumeInfo;

		this.freeSpace = freeSpace;

		timeout = deviceOptions.Timeout;
		allocationUnitSize = deviceOptions.AllocationUnitSize;
		sectorSize = deviceOptions.SectorSize;
		this.rootCreationDate = rootCreationDate;

		root = DokanyUtils.normalize(rootPath);
		isDebug = deviceOptions.getMountOptions().contains(MountOption.DEBUG_MODE);
		isDebugStdErr = deviceOptions.getMountOptions().contains(MountOption.STD_ERR_OUTPUT);
	}

	public abstract void mounted() throws IOException;

	public abstract void unmounted() throws IOException;

	public abstract boolean doesPathExist(final String path) throws IOException;

	public abstract Set<WIN32_FIND_DATA> findFilesWithPattern(final String pathToSearch, DokanyFileInfo dokanyFileInfo, final String pattern) throws IOException;

	public abstract Set<Win32FindStreamData> findStreams(final String pathToSearch) throws IOException;

	/**
	 * Only used if dokan option UserModeLock is enabled
	 */
	public abstract void unlock(final String path, final int offset, final int length) throws IOException;

	/**
	 * Only used if dokan option UserModeLock is enabled
	 */
	public abstract void lock(final String path, final int offset, final int length) throws IOException;

	public abstract void move(final String oldPath, final String newPath, final boolean replaceIfExisting) throws IOException;

	public abstract void deleteFile(final String path, final DokanyFileInfo dokanyFileInfo) throws IOException;

	public abstract void deleteDirectory(final String path, final DokanyFileInfo dokanyFileInfo) throws IOException;

	public abstract FileData read(final String path, final int offset, final int readLength) throws IOException;

	public abstract int write(final String path, final int offset, final byte[] data, final int writeLength) throws IOException;

	// TODO: Add SecurityContext and ShareAccess and DesiredAccess
	public abstract void createEmptyFile(final String path, long options, final EnumIntegerSet<FileAttribute> attributes) throws IOException;

	// TODO: Add SecurityContext and ShareAccess and DesiredAccess
	public abstract void createEmptyDirectory(final String path, final long options, final EnumIntegerSet<FileAttribute> attributes) throws IOException;

	public abstract void flushFileBuffers(final String path) throws IOException;

	public abstract void cleanup(final String path, final DokanyFileInfo dokanyFileInfo) throws IOException;

	public abstract void close(final String path, final DokanyFileInfo dokanyFileInfo) throws IOException;

	public abstract int getSecurity(final String path, final int kind, final byte[] out) throws IOException;

	public abstract void setSecurity(final String path, final int kind, final byte[] data) throws IOException;

	public abstract long truncate(final String path) throws IOException;

	public abstract void setAllocationSize(final String path, final int length) throws IOException;

	public abstract void setEndOfFile(final String path, final int offset) throws IOException;

	public abstract void setAttributes(final String path, final EnumIntegerSet<FileAttribute> attributes) throws IOException;

	public abstract FullFileInfo getInfo(final String path) throws IOException;

	public abstract void setTime(final String path, final FILETIME creation, final FILETIME lastAccess, final FILETIME lastModification) throws IOException;

	public VolumeInformation getVolumeInfo() {
		return volumeInfo;
	}

	public FreeSpace getFreeSpace() {
		return freeSpace;
	}

	public long getAllocationUnitSize() {
		return allocationUnitSize;
	}

	public long getSectorSize() {
		return sectorSize;
	}

	public long getTimeout() {
		return timeout;
	}

	public Date getRootCreationDate() {
		return rootCreationDate;
	}

	public String getRoot() {
		return root;
	}

	public boolean isDebug() {
		return isDebug;
	}

	public boolean isDebugStdErr() {
		return isDebugStdErr;
	}
}