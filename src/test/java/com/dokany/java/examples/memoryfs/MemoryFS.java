package com.dokany.java.examples.memoryfs;

import static com.sun.jna.platform.win32.WinNT.FILE_ATTRIBUTE_NORMAL;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

import com.dokany.java.DokanyFileSystem;
import com.dokany.java.Win32FindStreamData;
import com.dokany.java.constants.FileAttribute;
import com.dokany.java.structure.DeviceOptions;
import com.dokany.java.structure.DokanyFileInfo;
import com.dokany.java.structure.EnumIntegerSet;
import com.dokany.java.structure.FileData;
import com.dokany.java.structure.FreeSpace;
import com.dokany.java.structure.FullFileInfo;
import com.dokany.java.structure.VolumeInformation;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinBase.WIN32_FIND_DATA;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.IntegerBinding;
import jetbrains.exodus.bindings.LongBinding;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.core.dataStructures.hash.HashSet;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.util.LightOutputStream;
import jetbrains.exodus.vfs.File;
import jetbrains.exodus.vfs.VfsInputStream;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * In memory (virtual) filesystem backed by Xodus
 */
public class MemoryFS extends DokanyFileSystem {
	private static final Logger log = LoggerFactory.getLogger(MemoryFS.class);

	/**
	 * Path matcher glob
	 */
	private static final String GLOB = "glob:";
	private static final java.nio.file.FileSystem DEFAULT_FS = FileSystems.getDefault();

	// VFS - virtual file store provided by xodus
	private final Environment env;
	private final VirtualFileSystem vfs;
	private final Store infoStore;

	public MemoryFS(
	        final DeviceOptions deviceOptions,
	        final VolumeInformation volumeInfo,
	        final FreeSpace freeSpace,
	        final Date rootCreationDate,
	        final String rootPath) throws IOException {
		super(deviceOptions, volumeInfo, freeSpace, rootCreationDate, rootPath);

		// Try to create store location in temp directory
		Path fileStorePath = Files.createTempDirectory("dokany-java_");

		// Init VFS
		env = Environments.newInstance(fileStorePath.toString());
		vfs = new VirtualFileSystem(env);

		// File store name
		String fileStoreName = "com.dokany.java.fileinfo";

		// Create/open store and save in fileInfoStore
		// must not be read only so that store can be created
		infoStore = env.computeInTransaction((final Transaction txn) -> env.openStore(fileStoreName, StoreConfig.WITHOUT_DUPLICATES, txn));

		createSampleItems();
	}

	@Override
	public void mounted() {
		// try {
		// } catch (final IOException e) {
		// throw new DokanyException(MountError.DOKAN_START_ERROR.val, e);
		// }
	}

	@Override
	public void unmounted() {
		/*
		 * vfs.shutdown(); if (env.isOpen()) { env.close(); }
		 */
	}

	/**
	 * Determines if file exists already or not. Calls {@link #getExistingFile(String)}.
	 *
	 * @param path
	 * @return true if path exists
	 */
	@Override
	public boolean doesPathExist(final String path) {
		return Objects.nonNull(getExistingFile(path));
	}

	/**
	 * Retrieves existing path
	 *
	 * @param path
	 * @return file or null if path does not exist
	 */
	public File getExistingFile(final String path) {
		log.trace("getExistingFile: {}", path);
		// will be null if openFile does not find it
		return env.computeInReadonlyTransaction((final Transaction txn) -> getExistingFile(path, txn));
	}

	/**
	 * Retrieves existing path
	 *
	 * @param path
	 * @return file or null if path does not exist
	 */
	private File getExistingFile(final String path, final Transaction txn) {
		return vfs.openFile(txn, path, false);
	}

	/**
	 * Throws an exception if file cannot be found for specified path.
	 *
	 * @param path
	 * @return file or FileNotFoundException
	 * @throws FileNotFoundException
	 */
	private File getExistingFileWithException(final String path, final Transaction txn) throws FileNotFoundException {
		File file = getExistingFile(path, txn);
		if (Objects.isNull(file)) {
			throw new FileNotFoundException("Could not find file for path: " + path);
		}
		return file;
	}

	/*-
	@Override
	public Set<WIN32_FIND_DATA> findFiles(@NonNull final String pathToSearch) {
		final Set<WIN32_FIND_DATA> files = new HashSet<>();

		env.executeInReadonlyTransaction((@NonNull final Transaction txn) -> {
			vfs.getFiles(txn).forEach(file -> {
				final String currentPath = file.getPath();

				// Do not match pathToSearch or it will get listed in the directory
				if (!pathToSearch.equals(pathToSearch) && currentPath.startsWith(currentPath)) {
					try {
						files.add(getInfo(currentPath, txn).toWin32FindData());
					} catch (final FileNotFoundException e) {
						log.warn("Failed to add found file because of caught exception", e);
					}
				}
			});
		});
		return files;
	}
	*/

	@Override
	public Set<WIN32_FIND_DATA> findFilesWithPattern(final String pathToSearch, final DokanyFileInfo dokanyFileInfo, final String pattern) {
		Set<WIN32_FIND_DATA> files = new HashSet<>();

		log.debug("findFilesWithPattern memoryfs:   path  {};     pattern {}", pathToSearch, pattern);
		// Only use if pattern is not null
		PathMatcher pathMatcher = DEFAULT_FS.getPathMatcher(GLOB + root + pattern);

		env.executeInReadonlyTransaction((final Transaction txn) -> {
			vfs.getFiles(txn).forEach(file -> {
				String currentPath = file.getPath();
				log.trace("getFiles path: {}", currentPath);

				// Do not match pathToSearch or it will get listed in the directory
				if (!currentPath.equals(pathToSearch) && pathMatcher.matches(Paths.get(currentPath))) {
					try {
						files.add(getInfo(currentPath, txn).toWin32FindData());
						log.trace("Added {}", currentPath);
					} catch (final FileNotFoundException e) {
						log.warn("Failed to add found file because of caught exception", e);
					}
				}
			});
		});
		return files;
	}

	@Override
	public Set<Win32FindStreamData> findStreams(final String path) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Deletes a file.
	 */
	@Override
	public void deleteFile(final String path, final DokanyFileInfo dokanyFileInfo) throws IOException {
		delete(path);
	}

	/**
	 * Deletes a directory.
	 */
	@Override
	public void deleteDirectory(final String path, final DokanyFileInfo dokanyFileInfo) throws IOException {
		delete(path);
	}

	/**
	 * Deletes a file or directory.
	 *
	 * @param path
	 */
	private void delete(final String path) {
		env.executeInTransaction((final Transaction txn) -> {
			vfs.deleteFile(txn, path);
		});
	}

	/**
	 *
	 */
	@Override
	public void move(final String oldPath, final String newPath, final boolean replaceIfExisting) throws IOException {
		throw new UnsupportedOperationException("Not yet implemented");

		/*
		 * env.executeInTransaction((@NonNull final Transaction txn) -> { final VfsInputStream inputStream = vfs.readFile(txn, descriptor); inputStream.read(data, offset,
		 * Math.min(length, data.length - offset)); }); findExisting(oldPath, oldPath.isDirectory()).replaceWith(findExisting(newPath, newPath.isDirectory()));
		 */
	}

	/**
	 * This method creates FullFileInfo for the specified file and attributes. All times are automatically set to now. Size is automatically set to 0.
	 *
	 * @param file
	 * @param attributes
	 * @return
	 * @throws FileNotFoundException
	 */
	private FullFileInfo getNewInfo(final File file, final EnumIntegerSet<FileAttribute> attributes) throws FileNotFoundException {
		return new FullFileInfo(file.getPath(), file.getDescriptor(), attributes, getVolumeInfo().getSerialNumber());
	}

	/**
	 * This method retrieves FullFileInfo from the VFS.
	 *
	 * @throws IOException
	 */
	@Override
	public FullFileInfo getInfo(final String path) throws IOException {
		log.debug("getInfo for {}", path);

		FullFileInfo result = env.computeInReadonlyTransaction((final Transaction txn) -> {
			FullFileInfo toReturn = null;
			try {
				toReturn = getInfo(path, txn);
			} catch (final IOException e) {
				log.warn("Could not retrieve info", e);
			}
			return toReturn;
		});

		if (Objects.isNull(result)) {
			throw new IOException();
		}
		return result;
	}

	/**
	 * This method retrieves FullFileInfo from the VFS.
	 *
	 * @throws FileNotFoundException
	 */
	private FullFileInfo getInfo(final String path, final Transaction txn) throws FileNotFoundException {
		if (Objects.isNull(path)) {
			throw new IllegalArgumentException("path cannot be null");
		}
		ArrayByteIterable pathKey = StringBinding.stringToEntry(path);
		ByteIterable iterable = infoStore.get(txn, pathKey);
		if (Objects.isNull(iterable)) {
			throw new FileNotFoundException("iterable was null and thus file info could not be created");
		}
		return new XodusFileInfo(path, iterable);
	}

	/**
	 * Creates a new empty file for the path and sets the specified attributes.
	 *
	 * @throws IOException
	 */
	@Override
	public void createEmptyFile(final String path, final long options, final EnumIntegerSet<FileAttribute> attributes) throws IOException {
		final IOException error = env.computeInTransaction((final Transaction txn) -> {
			IOException toReturn = null;
			try {
				createEmptyFile(path, options, attributes, txn);
			} catch (final IOException e) {
				toReturn = e;
			}
			return toReturn;
		});

		if (Objects.nonNull(error)) {
			throw error;
		}
	}

	/**
	 * Creates a new empty file for the path and sets the specified attributes.
	 *
	 * @throws FileNotFoundException
	 */
	private void createEmptyFile(final String path, final long options, final EnumIntegerSet<FileAttribute> attributes, final Transaction txn)
	        throws FileNotFoundException {
		File file = createFile(path, txn);
		FullFileInfo info = getNewInfo(file, attributes);
		setInfo(path, info, txn);
	}

	/**
	 * Creates a new empty directory for the path and sets the specified attributes.
	 *
	 * @throws IOException
	 */
	@Override
	public void createEmptyDirectory(final String path, final long options, final EnumIntegerSet<FileAttribute> attributes) throws IOException {
		createEmptyFile(path, options, attributes);
	}

	/**
	 * Calls {@link jetbrains.exodus.vfs.VirtualFileSystem#createFile(Transaction, String)}
	 */
	private File createFile(final String path, final Transaction txn) {
		final String updatedPath;
		if (!path.startsWith(root)) {
			updatedPath = root + path;
		} else {
			updatedPath = path;
		}
		return vfs.createFile(txn, updatedPath);
	}

	/**
	 * Reads file from VFS and stores into data array.
	 */
	@Override
	public FileData read(final String path, final int offset, final int readLength) throws IOException {
		if (readLength < 1) {
			throw new IOException("readLength cannot be empty");
		}

		if (Objects.isNull(path)) {
			throw new FileNotFoundException("path cannot be null");
		}

		FileData fileData = env.computeInTransaction((final Transaction txn) -> {
			FileData toReturn = null;

			File file = getExistingFile(path, txn);
			long fileSize = vfs.getFileLength(txn, file);

			if (fileSize > 0) {
				if (Objects.nonNull(file)) {
					final VfsInputStream inputStream = vfs.readFile(txn, file);
					try {
						byte[] data = new byte[readLength];
						int numRead = inputStream.read(data, offset, Math.min(readLength, data.length - offset));
						toReturn = new FileData(data, numRead);
					} catch (final IOException e) {
						log.warn("Read fault on path {}", path, e);
					}
				}
			} else {
				// initialize empty so IOException is not thrown
				log.debug("fileSize was 0; sending back empty FileData");
				toReturn = new FileData(new byte[0], 0);
			}
			return toReturn;
		});

		if (Objects.isNull(fileData)) {
			throw new IOException("Error reading file");
		}
		return fileData;
	}

	/**
	 * Writes a file or directory to VFS.
	 *
	 * @param filePath
	 * @param offset
	 * @param data
	 * @param int
	 * @param info
	 */
	private int writeAll(final File file, final byte[] data, final Transaction txn) throws IOException {
		return write(file, 0, data, data.length, null, txn);
	}

	/**
	 * Writes a file or directory to VFS.
	 *
	 * @param path
	 * @param offset
	 * @param data
	 * @param int
	 * @param info
	 */
	@Override
	public int write(final String path, final int offset, final byte[] data, final int writeLength) throws IOException {
		if (Objects.isNull(path)) {
			throw new FileNotFoundException("Path was null");
		}

		IOException error = env.computeInTransaction((final Transaction txn) -> {
			IOException toReturn = null;
			try {
				File file = getExistingFileWithException(path, txn);
				write(file, offset, data, writeLength, null, txn);
			} catch (final IOException e) {
				toReturn = e;
			}
			return toReturn;
		});

		if (Objects.nonNull(error)) {
			throw error;
		}

		return writeLength;
	}

	private int write(final File file, final int offset, final byte[] data, final int writeLength, final FullFileInfo info, final Transaction txn)
	        throws IOException {
		try(final DataOutputStream output = new DataOutputStream(vfs.writeFile(txn, file))) {
			output.write(data, offset, writeLength);

			// This has to go outside the try statement so the buffer is fully written/flushed
			final long fileSize = vfs.getFileLength(txn, file);
			log.debug("wrote file: {}", file.getPath());

			FullFileInfo newInfo = info;
			if (Objects.isNull(info)) {
				// Figure out how to properly set attribute
				EnumIntegerSet<FileAttribute> attributes = new EnumIntegerSet<>(FileAttribute.class);
				attributes.add(FileAttribute.NORMAL);
				newInfo = getNewInfo(file, attributes);

				newInfo.setCreationTime(file.getCreated());
				newInfo.setSize(fileSize);
			}
			setInfo(file.getPath(), newInfo, txn);
		}
		return writeLength;
	}

	/**
	 * Stores FullFileInfo into VFS.
	 *
	 * @param path
	 * @param info
	 */
	private void setInfo(final String path, final FullFileInfo info, final Transaction txn) {
		ArrayByteIterable pathKey = StringBinding.stringToEntry(path);
		infoStore.put(txn, pathKey, toByteIterable(info));
		log.debug("Stored info for {}", path);
	}

	public ArrayByteIterable toByteIterable(FullFileInfo info) {
		LightOutputStream output = new LightOutputStream();

		LongBinding.writeCompressed(output, info.getSize());
		IntegerBinding.writeCompressed(output, info.nFileSizeHigh);
		IntegerBinding.writeCompressed(output, info.nFileSizeLow);

		LongBinding.writeCompressed(output, info.getFileIndex());
		IntegerBinding.writeCompressed(output, info.nFileIndexHigh);
		IntegerBinding.writeCompressed(output, info.nFileIndexLow);

		IntegerBinding.writeCompressed(output, info.dwFileAttributes);

		LongBinding.writeCompressed(output, info.ftCreationTime.toTime());
		LongBinding.writeCompressed(output, info.ftLastAccessTime.toTime());
		LongBinding.writeCompressed(output, info.ftLastWriteTime.toTime());

		IntegerBinding.writeCompressed(output, info.dwNumberOfLinks);
		IntegerBinding.writeCompressed(output, info.dwVolumeSerialNumber);

		IntegerBinding.writeCompressed(output, info.getDwReserved0());
		IntegerBinding.writeCompressed(output, info.getDwReserved1());

		// do not store char[] for cFileName and cAlternateFileName since they do not have native bindings and it seems to be as efficient to recalculate every time via the String
		// path than to covert to store as a StringBinding and then back again

		return output.asArrayByteIterable();
	}


	/**
	 * Stores FullFileInfo into VFS.
	 *
	 * @param path
	 * @param info
	 */
	private void setInfo(final String path, final FullFileInfo info) {
		env.executeInTransaction((final Transaction txn) -> {
			setInfo(path, info, txn);
		});
	}

	@Override
	/**
	 * Sets attributes on path.
	 */
	public void setAttributes(final String path, final EnumIntegerSet<FileAttribute> attributes) throws IOException {
		int attributeAsInt = FILE_ATTRIBUTE_NORMAL;
		// Will be null if coming from findExisting method
		if (Objects.nonNull(attributes)) {
			attributeAsInt = attributes.toInt();
		}

		FullFileInfo info = getInfo(path);
		info.dwFileAttributes = attributeAsInt;
		setInfo(path, info);
	}

	@Override
	public void setTime(final String path, final FILETIME creation, final FILETIME lastAccess, final FILETIME lastModification)
	        throws IOException {
		FullFileInfo info = getInfo(path);
		info.ftCreationTime = creation;
		info.ftLastAccessTime = lastAccess;
		info.ftLastWriteTime = lastModification;
		setInfo(path, info);
	}

	@Override
	public void setSecurity(final String path, final int kind, final byte[] data) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int getSecurity(final String path, final int kind, final byte[] out) throws IOException {
		throw new UnsupportedOperationException("Not yet implemented");
		// return 0;
	}

	@Override
	public void unlock(final String path, final int offset, final int length) {
		throw new UnsupportedOperationException("Unlocking not implemented.");
	}

	@Override
	public void lock(final String path, final int offset, final int length) {
		throw new UnsupportedOperationException("Locking not implemented.");
	}

	@Override
	// TODO: finish implementation
	public long truncate(final String path) throws IOException {
		// final Node parent = item.getParent();

		/*
		 * vfs. final FullFileInfo fileInfo = handle.getFileInfo(); final FullFileInfo parentFileInfo = createHandle(parent.getPath(), parent).getFileInfo(); final FILETIME now =
		 * new FILETIME(new Date()); fileInfo.ftLastAccessTime = now; parentFileInfo.ftLastAccessTime = now; fileInfo.ftLastWriteTime = now; parentFileInfo.ftLastWriteTime = now;
		 * return ErrorCodes.ERROR_ALREADY_EXISTS.val;
		 */
		// TODO: complete
		return 0;
	}

	@Override
	public void setAllocationSize(final String path, final int length) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void setEndOfFile(final String path, final int offset) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void flushFileBuffers(final String path) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void cleanup(final String path, final DokanyFileInfo dokanyFileInfo) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void close(final String path, final DokanyFileInfo dokanyFileInfo) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * // Dates will be automatically set to now by FileInfoBuilder. Number of links will default to 1.
	 *
	 * @param id
	 * @return
	 * @throws IOException
	 */
	private void createSampleItems() throws IOException {
		IOException error = env.computeInTransaction((final Transaction txn) -> {
			IOException toReturn = null;
			try {
				// Root - must be created
				File file = createFile(root, txn);
				EnumIntegerSet<FileAttribute> attributes = new EnumIntegerSet<>(FileAttribute.class);
				attributes.add(FileAttribute.DEVICE);
				FullFileInfo info = new FullFileInfo(root, file.getDescriptor(), attributes,
				        volumeInfo.getSerialNumber());
				info.setSize(freeSpace.getTotalBytes());
				setInfo(root, info, txn);

				// File 1 - empty
				attributes.clear();
				attributes.add(FileAttribute.NORMAL);
				createEmptyFile(root + "1.txt", 0, attributes, txn);

				// File 2 - 5 bytes
				file = createFile(root + "2.txt", txn);
				writeAll(file, new byte[] { 'H', 'E', 'L', 'L', 'O' }, txn);

				// Directory - empty
				attributes.clear();
				attributes.add(FileAttribute.DIRECTORY);
				createEmptyFile(root + "testFolder", 0, attributes, txn);

				// File 3 - several bytes
				file = createFile("testFolder/3.TXT", txn);
				writeAll(file, "This file is within testFolder".getBytes(StandardCharsets.UTF_8), txn);
				log.debug("done creating samples");
			} catch (final IOException e) {
				toReturn = e;
			}
			return toReturn;
		});

		if (Objects.nonNull(error)) {
			throw error;
		}
	}
}
