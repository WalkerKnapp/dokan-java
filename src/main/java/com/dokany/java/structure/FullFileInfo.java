package com.dokany.java.structure;

import java.io.FileNotFoundException;
import java.util.Objects;

import com.dokany.java.DokanyUtils;
import com.dokany.java.constants.FileAttribute;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinBase.WIN32_FIND_DATA;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Combines {@link FullFileInfo} and {@link WIN32_FIND_DATA}. This object will be stored in the Xodus fileInfo store.
 */
public class FullFileInfo extends ByHandleFileInfo {
	private static final Logger log = LoggerFactory.getLogger(FullFileInfo.class);

	/**
	 * If the dwFileAttributes member includes the FILE_ATTRIBUTE_REPARSE_POINT attribute, this member specifies the reparse point tag. Otherwise, this value is undefined and
	 * should not be used. For more information see Reparse Point Tags.
	 *
	 * IO_REPARSE_TAG_CSV (0x80000009) IO_REPARSE_TAG_DEDUP (0x80000013) IO_REPARSE_TAG_DFS (0x8000000A) IO_REPARSE_TAG_DFSR (0x80000012) IO_REPARSE_TAG_HSM (0xC0000004)
	 * IO_REPARSE_TAG_HSM2 (0x80000006) IO_REPARSE_TAG_MOUNT_POINT (0xA0000003) IO_REPARSE_TAG_NFS (0x80000014) IO_REPARSE_TAG_SIS (0x80000007) IO_REPARSE_TAG_SYMLINK (0xA000000C)
	 * IO_REPARSE_TAG_WIM (0x80000008)
	 */
	protected int dwReserved0;

	/**
	 * Reserved for future use.
	 */
	protected int dwReserved1;

	public FullFileInfo(
	        final String path,
	        final long index,
	        final EnumIntegerSet<FileAttribute> attributes,
	        final int volumeSerialNumber) throws FileNotFoundException {

		// times automatically set to now by ByHandleFileInfo constructors
		this(path, index, attributes, volumeSerialNumber, null, null, null);
	}

	public FullFileInfo(
	        final String path,
	        final long index,
	        final EnumIntegerSet<FileAttribute> attributes,
	        final int volumeSerialNumber,
	        final FILETIME creationTime,
	        final FILETIME lastAccessTime,
	        final FILETIME lastWriteTime) throws FileNotFoundException {

		super(creationTime, lastAccessTime, lastWriteTime);

		if (Objects.isNull(path)) {
			throw new FileNotFoundException("path was null and thus file info could not be created");
		}

		filePath = path;
		setIndex(index);
		setAttributes(attributes);
		dwVolumeSerialNumber = volumeSerialNumber;
		log.trace(super.toString());
	}

	/**
	 * Simply casts this object to ByHandleFileInfo
	 *
	 * @return this (cast as ByHandleFileInfo)
	 */
	public ByHandleFileInfo toByHandleFileInfo() {
		return this;
	}

	/**
	 *
	 * @return WIN32_FIND_DATA
	 */
	public WIN32_FIND_DATA toWin32FindData() {
		char[] cFileName = DokanyUtils.trimFrontSeparator(DokanyUtils.trimStrToSize(filePath, 260)).toCharArray();
		char[] cAlternateFileName = new char[1];
		// val cAlternateFileName = Utils.trimFrontSlash(Utils.trimStrToSize(path, 14)).toCharArray();
		// TODO: Why does setting alternate name cause file name to show up twice??
		return new WIN32_FIND_DATA(
		        dwFileAttributes,
		        ftCreationTime, ftLastAccessTime, ftLastWriteTime,
		        nFileSizeHigh, nFileSizeLow,
		        dwReserved0, dwReserved1,
		        cFileName, cAlternateFileName);
	}

	public int getDwReserved0() {
		return dwReserved0;
	}

	public int getDwReserved1() {
		return dwReserved1;
	}

	@Override
	public String toString() {
		return "FullFileInfo{" +
				"super=" + super.toString() +
				", dwReserved0=" + dwReserved0 +
				", dwReserved1=" + dwReserved1 +
				'}';
	}
}