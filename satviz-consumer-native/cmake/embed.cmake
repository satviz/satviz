function(embed_files output)
    # Create empty output file
    file(WRITE ${output} "")
    # Iterate through input files
    foreach(filepath ${ARGN})
        # Get short filename
        string(REGEX MATCH "([^/]+)$" filename ${filepath})
        # Replace dot for C++ compatibility
        string(REGEX REPLACE "\\." "_" filename ${filename})
        # Read hex data from file
        file(READ ${filepath} filedata HEX)
        # Convert hex data for C compatibility
        string(REGEX REPLACE "([0-9a-f][0-9a-f])" "0x\\1," filedata ${filedata})
        # Append data to output file
        file(APPEND ${output} "const char ${filename}[] = {${filedata}};\nconst unsigned ${filename}_size = sizeof (${filename});\n")
    endforeach()
endfunction()