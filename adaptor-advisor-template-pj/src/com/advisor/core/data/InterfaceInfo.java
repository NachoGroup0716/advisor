package com.advisor.core.data;

import com.advisor.core.generator.ValueGeneratorImpl;

import lombok.Data;

@Data
public class InterfaceInfo {
	private String interfaceId;
	private String interfaceType;
	private ValueGeneratorImpl txidGenerator;
	
	private String tempDir;
	private String succDir;
	private String failDir;
	private String recvDir;
	private String pollDir;
	
	private String privateKeyColumns;
	private String filePathColumn;
	private String fileNameColumn;
	private String fileServerInfo;
	private String compressArchiveFileName;
	private String decompressArchiveExtension;
	private boolean preserveFileTree;
	private boolean deleteOriginalFile;
	private boolean deleteTempolarFile;
	
	private String afterCallApiUrl;
	private String afterCallApiParams;
}
