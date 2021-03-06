/*
 * Copyright 2012-2016 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.client.async;

import java.nio.ByteBuffer;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.ResultCode;
import com.aerospike.client.cluster.Partition;
import com.aerospike.client.listener.ExistsListener;
import com.aerospike.client.policy.Policy;

public final class AsyncExists extends AsyncSingleCommand {
	private final Policy policy;
	private final ExistsListener listener;
	private final Key key;
	private final Partition partition;
	private boolean exists;
	
	public AsyncExists(AsyncCluster cluster, Policy policy, ExistsListener listener, Key key) {
		super(cluster);
		this.policy = policy;
		this.listener = listener;
		this.key = key;
		this.partition = new Partition(key);
	}
		
	@Override
	protected Policy getPolicy() {
		return policy;
	}

	@Override
	protected void writeBuffer() {
		setExists(policy, key);
	}

	@Override
	protected AsyncNode getNode() {
		return (AsyncNode)cluster.getReadNode(partition, policy.replica);
	}

	@Override
	protected void parseResult(ByteBuffer byteBuffer) {
		int resultCode = byteBuffer.get(5) & 0xFF;
		        
        if (resultCode == 0) {
        	exists = true;
        }
        else {
        	if (resultCode == ResultCode.KEY_NOT_FOUND_ERROR) {
            	exists = false;
        	}
        	else {
        		throw new AerospikeException(resultCode);
        	}
        }
	}	

	@Override
	protected void onSuccess() {
		if (listener != null) {
			listener.onSuccess(key, exists);
		}
	}

	@Override
	protected void onFailure(AerospikeException e) {
		if (listener != null) {
			listener.onFailure(e);
		}
	}
}
