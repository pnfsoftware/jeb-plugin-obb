# Android OBB Plugin for JEB

JEB plugin for Android Opaque Binary Blob (OBB) files. This plugin handles packaged file extraction, as well as listing metadata
found in the OBB file (application package name, version, etc).

A OBB file consists of a FAT image wrapped with OBB metadata in the form of a footer.

References:
<a href="http://developer.android.com/tools/help/jobb.html">OBB creation</a>
<a href="http://developer.android.com/google/play/expansion-files.html">APK expansion files</a>

How to build:
`$ ant -Dversion=1.0.2`
