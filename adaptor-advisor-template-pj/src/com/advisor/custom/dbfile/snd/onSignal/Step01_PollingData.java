package com.advisor.custom.dbfile.snd.onSignal;

import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.advisor.core.data.InterfaceInfo;
import com.advisor.core.result.OnSignalResult;
import com.advisor.core.strategy.OnSignalStrategy;
import com.advisor.custom.common.ChronoUtils;
import com.advisor.custom.common.FileUtils;
import com.advisor.custom.common.StringUtils;
import com.advisor.custom.dbfile.data.DBFileConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Step01_PollingData implements OnSignalStrategy {
	private final String HEADER = "[POLLING_DATA]";
	@Autowired(required = false)
	private SqlSessionTemplate sqlSession;
	
	@Override
	public void onStart(OnSignalResult signalResult) throws Exception {
		List<Map<String, Object>> polledResultList = new ArrayList<Map<String,Object>>();
		InterfaceInfo info = signalResult.getInterfaceInfo();
		String ifId = info.getInterfaceId();
		String txId = info.getTxidGenerator().create();
		String type = info.getInterfaceType();
		Map<String, String> chronoInfo = ChronoUtils.getChronoInfoFromTxId(txId);
		String dateTime = chronoInfo.get(ChronoUtils.DATE_TIME);
		String date = chronoInfo.get(ChronoUtils.DATE);
		String time = chronoInfo.get(ChronoUtils.TIME);
		
		if(type.equals(DBFileConstants.DB) || type.equals(DBFileConstants.DBFILE)) {
			String updateId = StringUtils.getMybatisId(ifId, DBFileConstants.UPDATE);
			String selectId = StringUtils.getMybatisId(ifId, DBFileConstants.SELECT);
			
			try {
				this.sqlSession.update(updateId);
				List<Map<String, Object>> selectList = this.sqlSession.selectList(selectId);
				if(selectList != null && selectList.size() > 0) {
					polledResultList.addAll(selectList);
				}
			} catch(Exception e) {
				log.error("{} Fail to update");
				throw e;
			}
		}
		
		if(type.equals(DBFileConstants.FILE)) {
			String pollDirPath = info.getPollDir();
			String filledPath = StringUtils.fillPalceholder(pollDirPath, dateTime, date, time, ifId, txId);
			log.info("{} Filled polling directory path [{}] --> [{}]", HEADER, pollDirPath, filledPath);
			
			Map<String, String> pathInfo = FileUtils.getBaseAndGlobInfo(filledPath);
			String baseDirPath = pathInfo.get(FileUtils.BASE_DIR);
			String globPattern = pathInfo.get(FileUtils.GLOB_PATTERN);
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
			
			Path baseDir = Paths.get(baseDirPath);
			if(Files.exists(baseDir)) {
				Files.walk(baseDir)
					.filter(path -> matcher.matches(baseDir.relativize(path)))
					.forEach(path -> {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(DBFileConstants.ORGL_FILE_PATH_NAME, path.getParent().toAbsolutePath().toString());
						map.put(DBFileConstants.ORGL_FILE_NAME, path.getFileName().toString());
						polledResultList.add(map);
					});
			} else {
				throw new FileNotFoundException("No such directory exists '" + baseDir.toAbsolutePath() + "'");
			}
			
			if(polledResultList.size() > 0) {
				signalResult.addProperty(DBFileConstants.ATTACH, DBFileConstants.YES);
				signalResult.addProperty(DBFileConstants.ESB_STATUS, DBFileConstants.SUCCESS);
			}
		} else if(type.equals(DBFileConstants.DBFILE)) {
			String pkColumns = info.getPrivateKeyColumns();
			String filePathColumn = info.getFilePathColumn();
			String fileNameColumn = info.getFileNameColumn();
		}
		
		if(polledResultList.size() > 0) {
			signalResult.setCount(polledResultList.size());
			signalResult.setPollDataObj(polledResultList);
		} else {
			signalResult.setCount(0);
		}
	}
}