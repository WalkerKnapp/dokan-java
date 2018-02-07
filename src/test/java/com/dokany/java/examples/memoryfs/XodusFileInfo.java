package com.dokany.java.examples.memoryfs;

import com.dokany.java.structure.FullFileInfo;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;
import jetbrains.exodus.bindings.IntegerBinding;
import jetbrains.exodus.bindings.LongBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Objects;

public class XodusFileInfo extends FullFileInfo {
    private static final Logger log = LoggerFactory.getLogger(XodusFileInfo.class);

    XodusFileInfo(final String path, final ByteIterable iterable) throws FileNotFoundException {
        super(path, 0, null, 0, null, null, null);
        if (Objects.isNull(path) || Objects.isNull(iterable)) {
            throw new FileNotFoundException("path or iterable was null and thus file info could not be created");
        }

        ByteIterator iterator = iterable.iterator();

        log.debug("Creating FullFileInfo from infoStore: {}", path);

        filePath = path;

        setSize(LongBinding.readCompressed(iterator), IntegerBinding.readCompressed(iterator), IntegerBinding.readCompressed(iterator));
        setIndex(LongBinding.readCompressed(iterator), IntegerBinding.readCompressed(iterator), IntegerBinding.readCompressed(iterator));

        dwFileAttributes = IntegerBinding.readCompressed(iterator);

        setTimes(LongBinding.readCompressed(iterator), LongBinding.readCompressed(iterator), LongBinding.readCompressed(iterator));

        // always needs to be at least 1 for the file to show up
        dwNumberOfLinks = IntegerBinding.readCompressed(iterator);
        if (dwNumberOfLinks == 0) {
            dwNumberOfLinks = 1;
        }
        dwVolumeSerialNumber = IntegerBinding.readCompressed(iterator);

        dwReserved0 = IntegerBinding.readCompressed(iterator);
        dwReserved1 = IntegerBinding.readCompressed(iterator);
    }
}
