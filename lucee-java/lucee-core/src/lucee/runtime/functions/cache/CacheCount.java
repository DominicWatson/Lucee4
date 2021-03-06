/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.functions.cache;

import java.io.IOException;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

/**
 * 
 */
public final class CacheCount implements Function {
	
	private static final long serialVersionUID = 4192649311671009474L;

	public static double call(PageContext pc) throws PageException {
		return call(pc,null);
		
	}
	
	public static double call(PageContext pc, String cacheName) throws PageException {
		try {
			return Util.getCache(pc,cacheName,ConfigImpl.CACHE_DEFAULT_OBJECT).keys().size();
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
}