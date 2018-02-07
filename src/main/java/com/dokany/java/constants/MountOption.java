package com.dokany.java.constants;

import com.dokany.java.DokanyUtils;
import com.dokany.java.structure.EnumIntegerSet;

public enum MountOption implements EnumInteger {

	DEBUG_MODE(1, "Enable ouput debug message"),

	STD_ERR_OUTPUT(2, "Enable ouput debug message to stderr"),

	ALT_STREAM(4, "Use alternate stream"),

	WRITE_PROTECTION(8, "Enable mount drive as write-protected"),

	NETWORK_DRIVE(16, "Use network drive - Dokan network provider need to be installed"),

	REMOVABLE_DRIVE(32, "Use removable drive"),

	MOUNT_MANAGER(64, "Use mount manager"),

	CURRENT_SESSION(128, "Mount the drive on current session only"),

	FILELOCK_USER_MODE(256, "Enable Lockfile/Unlockfile operations. Otherwise Dokan will take care of it");

	private final int mask;

	private final String description;

	private final boolean isReadonly;

	MountOption(final int i, final String desc) {
		mask = i;
		description = desc;

		// TODO: is this proper logic?
		isReadonly = (mask == 8);
	}

	@Override
	public int getMask() {
		return mask;
	}

	public String getDescription() {
		return description;
	}

	public boolean isReadonly() {
		return isReadonly;
	}

	public static EnumIntegerSet<MountOption> fromInt(final int value) {
		return DokanyUtils.enumSetFromInt(value, values());
	}
}
