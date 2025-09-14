package com.advisor.core.strategy;

import com.advisor.core.result.OnMessageResult;

public interface OnMessageStrategy {
	public void onMessage(OnMessageResult messageResult) throws Exception;
}
