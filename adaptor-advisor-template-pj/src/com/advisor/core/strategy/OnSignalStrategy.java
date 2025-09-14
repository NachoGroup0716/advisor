package com.advisor.core.strategy;

import com.advisor.core.result.OnSignalResult;

public interface OnSignalStrategy {
	public void onStart(OnSignalResult signalResult) throws Exception;
}
