/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.remote.server

import groovyx.remote.*
import groovyx.remote.util.*

class CommandChainInvoker {
	
	final ClassLoader parentLoader
	final CommandChain commandChain

	CommandChainInvoker(ClassLoader parentLoader, CommandChain commandChain) {
		this.parentLoader = parentLoader
		this.commandChain = commandChain
	}
	
	Result invokeAgainst(delegate, firstArg = null) {
		def arg = firstArg
		def lastResult = null
		def lastCommand = commandChain.commands.last()
		
		for (command in commandChain.commands) {
			lastResult = createInvoker(parentLoader, command).invokeAgainst(delegate, arg)
			
			if (command != lastCommand) {
				if (lastResult.thrown) {
					return lastResult
				} else if (lastResult.wasUnserializable) {
					// unserializable is ok when chaining
					arg = lastResult.unserializable
				} else {
					arg = lastResult.value
				}
			}
		}
		
		lastResult
	}

	protected createInvoker(ClassLoader loader, Command command) {
		new CommandInvoker(parentLoader, command)
	}
}