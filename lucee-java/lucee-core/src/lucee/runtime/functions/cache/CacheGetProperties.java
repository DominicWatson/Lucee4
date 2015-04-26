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

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class CacheGetProperties implements Function {
	
	private static final long serialVersionUID = -8665995702411192700L;

	public static Array call(PageContext pc) throws PageException {
		return call(pc, null);
	}
	
	public static Array call(PageContext pc, String cacheName) throws PageException {
		Array arr = new ArrayImpl();
		try {
			if(StringUtil.isEmpty(cacheName)){
				addDefault(pc,ConfigImpl.CACHE_DEFAULT_OBJECT,arr);
				addDefault(pc,ConfigImpl.CACHE_DEFAULT_TEMPLATE,arr);
				addDefault(pc,ConfigImpl.CACHE_DEFAULT_QUERY,arr);
				addDefault(pc,ConfigImpl.CACHE_DEFAULT_RESOURCE,arr);
				addDefault(pc,ConfigImpl.CACHE_DEFAULT_FUNCTION,arr);
				addDefault(pc,ConfigImpl.CACHE_DEFAULT_INCLUDE,arr);
				//arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_TEMPLATE).getCustomInfo());
				//arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_QUERY).getCustomInfo());
				//arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_RESOURCE).getCustomInfo());
				// MUST welcher muss zuers sein
			}
			else{
				String name;
				String[] names=ListUtil.listToStringArray(cacheName, ',');
				for(int i=0;i<names.length;i++){
					name=names[i].trim();
					if(name.equalsIgnoreCase("template"))
						arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_TEMPLATE).getCustomInfo());
					else if(name.equalsIgnoreCase("object"))
						arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_OBJECT).getCustomInfo());
					else if(name.equalsIgnoreCase("query"))
						arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_QUERY).getCustomInfo());
					else if(name.equalsIgnoreCase("resource"))
						arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_RESOURCE).getCustomInfo());
					else if(name.equalsIgnoreCase("function"))
						arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_FUNCTION).getCustomInfo());
					else if(name.equalsIgnoreCase("include"))
						arr.appendEL(Util.getDefault(pc,ConfigImpl.CACHE_DEFAULT_INCLUDE).getCustomInfo());
					else
						arr.appendEL(Util.getCache(pc.getConfig(),name).getCustomInfo());
				}
			}
			
			
			return arr;
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private static void addDefault(PageContext pc, int type, Array arr) {
		try {
			arr.appendEL(Util.getDefault(pc,type).getCustomInfo());
		} catch (IOException e) {}
	}
}