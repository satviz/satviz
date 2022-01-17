#
# Try to find Ogg/Theora libraries and include paths.
# Once done this will define
#
# THEORA_FOUND
# THEORA_INCLUDE_DIRS
# THEORA_LIBRARIES
#

find_path(OGG_INCLUDE_DIR ogg/ogg.h)
find_path(THEORA_INCLUDE_DIR theora/theoradec.h)

find_library(OGG_LIBRARY NAMES ogg)
find_library(THEORADEC_LIBRARY NAMES theoradec)
find_library(THEORAENC_LIBRARY NAMES theoraenc)
set(THEORA_LIBRARIES "${THEORAENC_LIBRARY}" "${THEORADEC_LIBRARY}" "${OGG_LIBRARY}")

include(FindPackageHandleStandardArgs)

find_package_handle_standard_args(THEORA DEFAULT_MSG THEORA_LIBRARIES THEORA_INCLUDE_DIR OGG_INCLUDE_DIR)

set(THEORA_INCLUDE_DIRS ${OGG_INCLUDE_DIR} ${THEORA_INCLUDE_DIR})

mark_as_advanced(OGG_INCLUDE_DIR THEORA_INCLUDE_DIR OGG_LIBRARY THEORADEC_LIBRARY THEORAENC_LIBRARY)

