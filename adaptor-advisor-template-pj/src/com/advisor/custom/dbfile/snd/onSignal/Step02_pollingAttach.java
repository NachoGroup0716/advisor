package com.advisor.custom.dbfile.snd.onSignal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import com.advisor.core.data.InterfaceInfo;
import com.advisor.core.result.OnSignalResult;
import com.advisor.core.strategy.OnSignalStrategy;
import com.advisor.custom.common.ChronoUtils;
import com.advisor.custom.common.StringUtils;
import com.advisor.custom.common.ZipUtils;
import com.advisor.custom.dbfile.data.DBFileConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Step02_pollingAttach implements OnSignalStrategy {
	private final String HEADER = "[POLLING_ATTACH]";
	private String nasPath = "/nas/FILE/";
	
	@Override
	public void onStart(OnSignalResult signalResult) throws Exception {
		InterfaceInfo info = signalResult.getInterfaceInfo();
		String type = info.getInterfaceType();
		
		if(type.equals(DBFileConstants.DBFILE) || type.equals(DBFileConstants.FILE)) {
			List<Map<String, Object>> polledResultList = (List<Map<String, Object>>) signalResult.getPollDataObj();
			String ifId = info.getInterfaceId();
			String txId = signalResult.getProperties().get(DBFileConstants.TX_ID);
			Map<String, String> chronoInfo = ChronoUtils.getChronoInfoFromTxId(txId);
			String dateTime = chronoInfo.get(ChronoUtils.DATE_TIME);
			String date = chronoInfo.get(ChronoUtils.DATE);
			String time = chronoInfo.get(ChronoUtils.TIME);
			String tempDirPath = info.getTempDir();
			
			polledResultList.stream()
						.filter(map -> map.containsKey(DBFileConstants.ORGL_FILE_PATH_NAME) && map.containsKey(DBFileConstants.ORGL_FILE_NAME))
						.forEach(map -> {
							String pkId = map.containsKey(DBFileConstants.PK_ID) ? String.valueOf(map.get(DBFileConstants.PK_ID)) : null;
							String orglFilePath = String.valueOf(map.get(DBFileConstants.ORGL_FILE_PATH_NAME));
							String orglFileName = String.valueOf(map.get(DBFileConstants.ORGL_FILE_NAME));
							String tempFilePath = StringUtils.fillPalceholder(tempDirPath, dateTime, date, time, ifId, txId, pkId);
							String message = null;
							
							Path orglDir = Paths.get(orglFilePath);
							Path tempDir = Paths.get(tempFilePath);
							Path orglFile = orglDir.resolve(orglFileName);
							Path tempFile = null;
							if(info.isPreserveFileTree()) {
								tempFile = tempDir.resolve(orglFileName);
							} else {
								tempFile = tempDir.resolve(orglFile.getFileName());
							}
							try {
								Files.createDirectories(tempFile.getParent());
								if(info.isDeleteOriginalFile()) {
									try {
										Files.move(orglFile, tempFile);
									} catch(IOException e) {
										message = "Fail to move [" + orglFile.toAbsolutePath() + "] --> [" + tempFile.toAbsolutePath() + "]";
										log.error("{} {}\r\n", HEADER, message , e);
									}
								} else {
									try {
										Files.copy(orglFile, tempFile, StandardCopyOption.COPY_ATTRIBUTES);
									} catch(IOException e) {
										message = "Fail to copy [" + orglFile.toAbsolutePath() + "] --> [" + tempFile.toAbsolutePath() + "]";
										log.error("{} {}\r\n", HEADER, message , e);
									}
								}
							} catch(IOException e) {
								message = "Fail to create directories [" + tempFile.getParent().toAbsolutePath() + "]";
								log.error("{} {}\r\n", HEADER, message , e);
							}
							
							if(message == null) {
								map.put(DBFileConstants.ESB_STATUS, DBFileConstants.SUCCESS);
							} else {
								map.put(DBFileConstants.ESB_STATUS, DBFileConstants.FAIL);
								map.put(DBFileConstants.ESB_MESSAGE, message);
							}
						});
			
			String filledTempDirPath = StringUtils.fillPalceholder(tempDirPath, dateTime, date, time, ifId, txId, null);
			Path tempDir = Paths.get(filledTempDirPath);
			if(sendFile(tempDir.toAbsolutePath().toString(), txId, info.isDeleteTempolarFile())) {
				signalResult.addProperty(DBFileConstants.ESB_STATUS, DBFileConstants.FAIL);
				signalResult.addProperty(DBFileConstants.ATTACH, DBFileConstants.YES);
			} else {
				signalResult.addProperty(DBFileConstants.ESB_STATUS, DBFileConstants.FAIL);
				signalResult.addProperty(DBFileConstants.ESB_MESSAGE, "Fail to send temp zip file to nas directory");
			}
		}
	}
	
	public boolean sendFile(String sendDir, String txId, boolean delFlag) throws IOException, Exception {
		log.debug("############### tempDir compress process start.....................");
		Path tempDir = Paths.get(sendDir);
		Path zipFile = tempDir.resolve(txId + ".zip");
		try {
			ZipUtils.compress(Files.list(tempDir).map(file -> file.toFile()).toArray(File[]::new), tempDir.toAbsolutePath().toString(), zipFile.toFile());
		} catch(Exception e) {
			log.error("{} Fail to compress temp directory into zip file [{}]\r\n", HEADER, zipFile.toAbsolutePath(), e);
			return false;
		}
		log.debug("############### tempDir compress process end.....................");
		Path nasFile = Paths.get(nasPath).resolve(zipFile.getFileName());
		if(delFlag) {
			try {
				Files.move(zipFile, nasFile, StandardCopyOption.REPLACE_EXISTING);
			} catch(IOException e) {
				log.error("{} Fail to move temp zip file [{}] --> [{}]", HEADER, zipFile.toAbsolutePath(), nasFile.toAbsolutePath());
				return false;
			}
		} else {
			try {
				Files.copy(zipFile, nasFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			} catch(IOException e) {
				log.error("{} Fail to copy temp zip file [{}] --> [{}]", HEADER, zipFile.toAbsolutePath(), nasFile.toAbsolutePath());
				return false;
			}
		}
		return true;
	}
}