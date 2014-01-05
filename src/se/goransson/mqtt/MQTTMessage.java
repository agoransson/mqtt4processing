package se.goransson.mqtt;

/*
 * Copyright (C) 2012 Andreas Goransson, David Cuartielles
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

import java.util.HashMap;
import java.util.Map;

public class MQTTMessage {

	public byte type;
	public boolean DUP;
	public int QoS;
	public boolean retain;
	public int remainingLength;

	public Map<String, Object> variableHeader = new HashMap<String, Object>();

	public byte[] payload;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Type:" + type).append("\n");
		sb.append("DUP:" + DUP).append("\n");
		sb.append("QoS:" + QoS).append("\n");
		sb.append("Retain:" + retain).append("\n");
		sb.append("remaining length:" + remainingLength).append("\n");

		sb.append("Topic name:" + variableHeader.get("topic_name")).append("\n");
		sb.append("Topic len:" + variableHeader.get("topic_name_len")).append("\n");
		
		return sb.toString();
	}
}
