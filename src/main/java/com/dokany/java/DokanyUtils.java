package com.dokany.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Objects;

import com.dokany.java.constants.EnumInteger;
import com.dokany.java.constants.ErrorCode;
import com.dokany.java.constants.NtStatus;
import com.dokany.java.structure.DokanyFileInfo;
import com.dokany.java.structure.EnumIntegerSet;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinNT.LARGE_INTEGER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Utilities to do various operations.
 *
 */
public class DokanyUtils {
    private static final Logger log = LoggerFactory.getLogger(DokanyUtils.class);

	public static String UNIX_SEPARATOR = "/";

	/**
	 * Uses *nix separator
	 *
	 * @param str
	 * @return
	 */
	public static String trimTailSeparator(final String str) {
		return str.endsWith(UNIX_SEPARATOR) ? str.substring(0, str.length() - 1) : str;
	}

	/**
	 * Uses *nix separator
	 *
	 * @param str
	 * @return
	 */
	public static String trimFrontSeparator(final String str) {
		return str.startsWith(UNIX_SEPARATOR) ? str.substring(1, str.length()) : str;
	}

	// TODO: can this return null?
	/**
	 *
	 * @param path
	 * @return
	 */
	public static Path getPath(final String path) {
		return Paths.get(path);
	}

	// TODO: can this return null?
	/**
	 *
	 * @param path
	 * @return
	 */
	public static File toFile(final String path) {
		return getPath(path).toFile();
	}

	/**
	 * Will add tail UNIX_SEPARATOR if file is a directory and tail separator is not already present
	 *
	 * @param path
	 * @return
	 */
	public static String normalize(final String path) {
		String normalizedPath = Paths.get(path).normalize().toString();

		if (new File(normalizedPath).isDirectory()) {
			final int lastSeparator = indexOfLastSeparator(normalizedPath);

			if ((lastSeparator == -1) || (lastSeparator != (normalizedPath.length() - 1))) {
				normalizedPath += UNIX_SEPARATOR;
			}
		}
		return normalizedPath;
	}

	public static int indexOfLastSeparator(final String normalizedPath) {
		return normalizedPath.lastIndexOf(UNIX_SEPARATOR);
	}

	public static String normalize(final WString path) {
		return normalize(path.toString());
	}

	public static String normalize(final Path path) {
		return normalize(path.toString());
	}

	static String getFileName(final String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	static String getExtension(final String fileName) {
        return fileName.substring(fileName.lastIndexOf('.'), fileName.length());
	}

	public static String toShortName(final Path path) {
		String pathAsStr = path.toString();

		String base = trimStrToSize(getFileName(pathAsStr), 8);
		log.trace("base: {}", base);

		String ext = trimStrToSize(getExtension(pathAsStr), 3);
		if (ext.length() > 0) {
			ext = "." + ext;
		}
		log.trace("ext: {}", ext);
		return base + ext;
	}

	public static String trimStrToSize(final String str, final int len) {
		return str.substring(0, Math.min(str.length(), len));
	}

	static long exceptionToErrorCode(final Throwable t) {
		return exceptionToErrorCode(t, NtStatus.UNSUCCESSFUL.getMask());
	}

	static long exceptionToErrorCode(final Throwable t, final long defaultCode) {
		log.warn(t.getMessage(), t);

		if (t instanceof DokanyException) {
			return ((DokanyException) t).getValue();
		}
		if (t instanceof FileNotFoundException) {
			return ErrorCode.ERROR_FILE_NOT_FOUND.getMask();
		}
		if (t instanceof FileAlreadyExistsException) {
			return ErrorCode.ERROR_ALREADY_EXISTS.getMask();
		}

		return defaultCode;
	}

	public static FileTime toNioFileTime(final FILETIME time) {
		return FileTime.from(time.toDate().toInstant());
	}

	public static FILETIME toJnaFileTime(final FileTime time) {
		return getTime(time.toMillis());
	}

	public static FILETIME getTime(final Date date) {
		return new FILETIME(date);
	}

	public static FILETIME getTime(final long time) {
		return getTime(new Date(time));
	}

	public static FILETIME getCurrentTime() {
		return getTime(new Date());
	}

	/**
	 *
	 * @param val
	 * @param high
	 * @param low
	 * @return
	 */
	public static LARGE_INTEGER getLargeInt(final long val, final int high, final int low) {
		LARGE_INTEGER largeInt = null;
		if ((val != 0) && ((high == 0) || (low == 0))) {
			largeInt = new LARGE_INTEGER(val);
		}
		return largeInt;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static BasicFileAttributeView getBasicAttributes(final String path) {
		return getBasicAttributes(getPath(path));
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static BasicFileAttributeView getBasicAttributes(final Path path) {
		return Files.getFileAttributeView(path, BasicFileAttributeView.class);
	}

	/**
	 * Will return an
	 *
	 * @param value
	 * @param allEnumValues
	 * @return
	 */
	public static <T extends Enum<T>> EnumIntegerSet<T> enumSetFromInt(final int value, final T[] allEnumValues) {
		EnumIntegerSet<T> elements = new EnumIntegerSet<>(allEnumValues[0].getDeclaringClass());
		int remainingValues = value;
		for (T current : allEnumValues) {
			int mask = ((EnumInteger) current).getMask();

			if ((remainingValues & mask) == mask) {
				elements.add(current);
				remainingValues -= mask;
			}
		}
		return elements;
	}

	public static <T extends EnumInteger> T enumFromInt(final int value, final T[] enumValues) {
		for (final T current : enumValues) {
			if ((value & current.getMask()) == current.getMask()) {
				return current;
			}
		}
		throw new IllegalArgumentException("Invalid int value: " + value);
	}

	/**
	 * Set DokanyFileInfo.DeleteOnClose based on whether file or directory can be deleted.
	 *
	 * @param fileOrDirectory
	 * @param dokanyFileInfo
	 */
	public static void setDeleteStatus(final File fileOrDirectory, final DokanyFileInfo dokanyFileInfo) {
		boolean canDelete = fileOrDirectory.renameTo(fileOrDirectory);

		if (canDelete) {
			dokanyFileInfo.DeleteOnClose = 1;
		}
	}

	/**
	 * Returns String representation of WString.
	 *
	 * @param wStr .
	 * @return if wStr is null, method will return null
	 */
	public static String wStrToStr(final WString wStr) {
		return Objects.nonNull(wStr) ? wStr.toString() : null;
	}
}
