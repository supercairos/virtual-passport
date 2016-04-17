/*
 *    Copyright 2016 Romain
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.romain.passport.utils;

import android.util.Log;

import com.facebook.stetho.inspector.console.CLog;
import com.facebook.stetho.inspector.console.ConsolePeerManager;
import com.facebook.stetho.inspector.protocol.module.Console;

public class StethoBone extends Dog.Bone {

	@Override
	protected void log(int priority, String tag, String message, Throwable t) {

		ConsolePeerManager peerManager = ConsolePeerManager.getInstanceOrNull();
		if (peerManager == null) {
			return;
		}

		Console.MessageLevel logLevel;

		switch (priority) {
			case Log.VERBOSE:
			case Log.DEBUG:
				logLevel = Console.MessageLevel.DEBUG;
				break;
			case Log.INFO:
				logLevel = Console.MessageLevel.LOG;
				break;
			case Log.WARN:
				logLevel = Console.MessageLevel.WARNING;
				break;
			case Log.ERROR:
			case Log.ASSERT:
				logLevel = Console.MessageLevel.ERROR;
				break;
			default:
				logLevel = Console.MessageLevel.LOG;
		}

		CLog.writeToConsole(
				logLevel,
				Console.MessageSource.OTHER,
				message
		);
	}
}
