/*
 * Copyright (C) 2012, 2015 The Android Open Source Project 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * 
 * Modified on 6/5/2015 by Carlos Gonzales (CNexus)
 * 
 * Changes:
 *			- Add support for reading from generic, in-memory byte data
 *			- Fix resource leak when writing to output file
 * 
 */

package com.pnf.plugin.obb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.pnfsoftware.jeb.util.serialization.annotations.Ser;
import com.pnfsoftware.jeb.util.serialization.annotations.SerId;

@Ser
public class ObbData {
    public static final int OBB_OVERLAY = (1 << 0);
    public static final int OBB_SALTED = (1 << 1);

    static final int kFooterTagSize = 8; /* last two 32-bit integers */

    static final int kFooterMinSize = 33; /*
                                           * 32-bit signature version (4 bytes)
                                           * 32-bit package version (4 bytes)
                                           * 32-bit flags (4 bytes) 64-bit salt
                                           * (8 bytes) 32-bit package name size
                                           * (4 bytes) >=1-character package
                                           * name (1 byte) 32-bit footer size (4
                                           * bytes) 32-bit footer marker (4
                                           * bytes)
                                           */

    private static final int kMaxBufSize = 32768; /* Maximum file read buffer */

    private static final long kSignature = 0x01059983; /* ObbFile signature */

    private static final int kSigVersion = 1; /*
                                               * We only know about signature
                                               * version 1
                                               */

    /* offsets in version 1 of the header */
    private static final int kPackageVersionOffset = 4;
    private static final int kFlagsOffset = 8;
    private static final int kSaltOffset = 12;
    private static final int kPackageNameLenOffset = 20;
    private static final int kPackageNameOffset = 24;

    public static final String[] DATA_KEYS = {"PACKAGE_NAME", "PACKAGE_VERSION", "FLAGS", "SALT"};

    @SerId(1)
    private long mPackageVersion = -1;
    @SerId(2)
    private long mFlags;
    @SerId(3)
    private String mPackageName;
    @SerId(4)
    private byte[] mSalt = new byte[8];
    @SerId(5)
    private Map<String, String> data;

    public ObbData() {
    }

    public Map<String, String> getData() {
        if(data == null)
            throw new RuntimeException("Data not yet read from obb file");
        return data;
    }

    public boolean readFrom(String filename) {
        File obbFile = new File(filename);
        return readFrom(obbFile);
    }

    public boolean readFrom(File obbFile) {
        return parseObbFile(obbFile);
    }

    public static long get4LE(ByteBuffer buf) {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return (buf.getInt() & 0xFFFFFFFFL);
    }

    public static boolean removeFooter(byte[] obbData) {
        if(obbData.length < kFooterMinSize) {
            return false;
        }

        /* Zero out the indices where the footer is stored */
        for(int i = obbData.length - kFooterTagSize; i < obbData.length; i++) {
            obbData[i] = 0x0;
        }

        return true;
    }

    public long getmPackageVersion() {
        return mPackageVersion;
    }

    public long getFlags() {
        return mFlags;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public byte[] getSalt() {
        return mSalt;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public void setSalt(byte[] salt) {
        if(salt.length != mSalt.length) {
            throw new RuntimeException("salt must be " + mSalt.length + " characters in length");
        }
        System.arraycopy(salt, 0, mSalt, 0, mSalt.length);
    }

    public void setPackageVersion(long packageVersion) {
        mPackageVersion = packageVersion;
    }

    public void setFlags(long flags) {
        mFlags = flags;
    }

    public boolean parseObbFile(byte[] bytes) {
        data = new LinkedHashMap<>();

        try(ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            long fileLength = bytes.length;

            if(fileLength < kFooterMinSize) {
                return false;
            }

            stream.reset();
            stream.skip(fileLength - kFooterTagSize);
            byte[] footer = new byte[kFooterTagSize];
            int len = stream.read(footer);

            if(len == -1 || len < footer.length) {
                return false;
            }

            ByteBuffer footBuf = ByteBuffer.wrap(footer);
            footBuf.position(4);
            long fileSig = get4LE(footBuf);
            if(fileSig != kSignature) {
                return false;
            }

            footBuf.rewind();
            long footerSize = get4LE(footBuf);
            if(footerSize > fileLength - kFooterTagSize || footerSize > kMaxBufSize) {
                return false;
            }

            if(footerSize < (kFooterMinSize - kFooterTagSize)) {
                return false;
            }

            long fileOffset = fileLength - footerSize - kFooterTagSize;
            stream.reset();
            stream.skip(fileOffset);

            footer = new byte[(int)footerSize];
            len = stream.read(footer);

            if(len == -1 || len < footer.length) {
                return false;
            }

            footBuf = ByteBuffer.wrap(footer);

            long sigVersion = get4LE(footBuf);
            if(sigVersion != kSigVersion) {
                return false;
            }

            footBuf.position(kPackageVersionOffset);
            mPackageVersion = get4LE(footBuf);
            data.put(DATA_KEYS[1], String.valueOf(mPackageVersion));

            footBuf.position(kFlagsOffset);
            mFlags = get4LE(footBuf);
            data.put(DATA_KEYS[2], String.valueOf(mFlags));

            footBuf.position(kSaltOffset);
            footBuf.get(mSalt);
            data.put(DATA_KEYS[3], Arrays.toString(mSalt));

            footBuf.position(kPackageNameLenOffset);
            long packageNameLen = get4LE(footBuf);
            if(packageNameLen == 0 || packageNameLen > (footerSize - kPackageNameOffset)) {
                return false;
            }
            byte[] packageNameBuf = new byte[(int)packageNameLen];
            footBuf.position(kPackageNameOffset);
            footBuf.get(packageNameBuf);

            mPackageName = new String(packageNameBuf);
            data.put(DATA_KEYS[0], mPackageName);
            return true;
        }
        catch(IOException e) {
            return false;
        }
    }

    public boolean parseObbFile(File obbFile) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(obbFile.toPath());
        }
        catch(IOException e) {
            return false;
        }

        return parseObbFile(bytes);
    }

    public boolean writeTo(String fileName) {
        File obbFile = new File(fileName);
        return writeTo(obbFile);
    }

    public boolean writeTo(File obbFile) {
        if(!obbFile.exists()) {
            return false;
        }

        if(null == mPackageName || mPackageVersion == -1) {
            throw new RuntimeException("tried to write uninitialized ObbFile data");
        }

        long fileLength = obbFile.length();
        try(RandomAccessFile raf = new RandomAccessFile(obbFile, "rw")) {
            raf.seek(fileLength);
            try(FileChannel fc = raf.getChannel()) {
                ByteBuffer bbInt = ByteBuffer.allocate(4);
                bbInt.order(ByteOrder.LITTLE_ENDIAN);
                bbInt.putInt(kSigVersion);
                bbInt.rewind();
                fc.write(bbInt);

                bbInt.rewind();
                bbInt.putInt((int)mPackageVersion);
                bbInt.rewind();
                fc.write(bbInt);

                bbInt.rewind();
                bbInt.putInt((int)mFlags);
                bbInt.rewind();
                fc.write(bbInt);

                raf.write(mSalt);

                bbInt.rewind();
                bbInt.putInt(mPackageName.length());
                bbInt.rewind();
                fc.write(bbInt);

                raf.write(mPackageName.getBytes());

                bbInt.rewind();
                bbInt.putInt(mPackageName.length() + kPackageNameOffset);
                bbInt.rewind();
                fc.write(bbInt);

                bbInt.rewind();
                bbInt.putInt((int)kSignature);
                bbInt.rewind();
                fc.write(bbInt);
            }
        }
        catch(IOException e) {
            return false;
        }
        return true;
    }
}
