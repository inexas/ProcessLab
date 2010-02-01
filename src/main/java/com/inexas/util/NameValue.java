/*
	Copyright 2010 Inexas. All rights reserved.

	Licensed under the Inexas Software License V1.0. You may not use this file 
	except in compliance with the License. You may obtain a copy of the License
	at http://www.inexas.com/ISL-V1.0.

	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
	CONDITIONS OF ANY KIND, either express or implied. See the License for the 
	specific language governing permissions and limitations under the License.
*/
package com.inexas.util;

/**
 * A simple utility class that stores a name value pair. The name
 * is a String, the value is a generic type.
 * 
 * @author KeithWhittingham
 */
public class NameValue<T> {
	private final String name;
	private final T value;
	
	public NameValue(String name, T value) {
		assert name != null && name.trim().length() > 0;
		this.name = name.trim();
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return name + "=" + value;
	}
}
