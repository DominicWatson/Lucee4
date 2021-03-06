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
package lucee.runtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.servlet.ServletContext;

import lucee.commons.io.FileUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ArchiveClassLoader;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.MappingUtil;
import lucee.commons.lang.PCLCollection;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;

import org.apache.commons.collections.map.ReferenceMap;

/**  
 * Mapping class
 */
public final class MappingImpl implements Mapping {

	private static final long serialVersionUID = 6431380676262041196L;
	
	//private static final Object NULL = new Object();
	private String virtual;
    private String lcVirtual;
    private boolean topLevel;
    private short inspect;
    private boolean physicalFirst;
    private ArchiveClassLoader archiveClassLoader;
    //private PhysicalClassLoader physicalClassLoader;
    private PCLCollection pclCollection;
    private Resource archive;
    
    private boolean hasArchive;
    private Config config;
    private Resource classRootDirectory;
    private PageSourcePool pageSourcePool=new PageSourcePool();
    
    private boolean readonly=false;
    private boolean hidden=false;
    private String strArchive;
    
    private String strPhysical;
    private Resource physical;
    //private boolean hasPhysical;
    
    private String lcVirtualWithSlash;
    //private Resource classRoot;
    private Map<String,Object> customTagPath=new ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
    //private final Map<String,Object> customTagPath=new HashMap<String, Object>();
	private int classLoaderMaxElements=1000;
	/**
	 * @return the classLoaderMaxElements
	 */
	public int getClassLoaderMaxElements() {
		return classLoaderMaxElements;
	}

	private boolean appMapping;
	private boolean ignoreVirtual;

	private ApplicationListener appListener;

    public MappingImpl(Config config, String virtual, String strPhysical,String strArchive, short inspect, 
            boolean physicalFirst, boolean hidden, boolean readonly,boolean topLevel, boolean appMapping,boolean ignoreVirtual,ApplicationListener appListener) {
    	this(config, virtual, strPhysical, strArchive, inspect, physicalFirst, hidden, readonly,topLevel,appMapping,ignoreVirtual,appListener,5000);
    	
    }

    /**
     * @param configServer 
     * @param config
     * @param virtual
     * @param strPhysical
     * @param strArchive
     * @param trusted
     * @param physicalFirst
     * @param hidden
     * @param readonly
     * @throws IOException
     */
    public MappingImpl(Config config, String virtual, String strPhysical,String strArchive, short inspect, 
            boolean physicalFirst, boolean hidden, boolean readonly,boolean topLevel, boolean appMapping, boolean ignoreVirtual,ApplicationListener appListener, int classLoaderMaxElements) {
    	this.ignoreVirtual=ignoreVirtual;
    	this.config=config;
        this.hidden=hidden;
        this.readonly=readonly;
        this.strPhysical=StringUtil.isEmpty(strPhysical)?null:strPhysical;
        this.strArchive=StringUtil.isEmpty(strArchive)?null:strArchive;
        this.inspect=inspect;
        this.topLevel=topLevel;
        this.appMapping=appMapping;
        this.physicalFirst=physicalFirst;
        this.appListener=appListener;
        this.classLoaderMaxElements=classLoaderMaxElements;
        
        // virtual
        if(virtual.length()==0)virtual="/";
        if(!virtual.equals("/") && virtual.endsWith("/"))this.virtual=virtual.substring(0,virtual.length()-1);
        else this.virtual=virtual;
        this.lcVirtual=this.virtual.toLowerCase();
        this.lcVirtualWithSlash=lcVirtual.endsWith("/")?this.lcVirtual:this.lcVirtual+'/';

        //if(!(config instanceof ConfigWebImpl)) return;
        //ConfigWebImpl cw=(ConfigWebImpl) config;
        ServletContext cs = (config instanceof ConfigWebImpl)?((ConfigWebImpl)config).getServletContext():null;
        
        
        // Physical
        physical=ConfigWebUtil.getExistingResource(cs,strPhysical,null,config.getConfigDir(),FileUtil.TYPE_DIR,
                config);
        // Archive
        archive=ConfigWebUtil.getExistingResource(cs,strArchive,null,config.getConfigDir(),FileUtil.TYPE_FILE,
                config);
        if(archive!=null) {
            try {
                archiveClassLoader = new ArchiveClassLoader(archive,getClass().getClassLoader());
            } 
            catch (Throwable t) {
    			ExceptionUtil.rethrowIfNecessary(t);
                archive=null;
            }
        }
        hasArchive=archive!=null;

        if(archive==null) this.physicalFirst=true;
        else if(physical==null) this.physicalFirst=false;
        else this.physicalFirst=physicalFirst;
        
        
        //if(!hasArchive && !hasPhysical) throw new IOException("missing physical and archive path, one of them must be defined");
    }
    
    @Override
    public ClassLoader getClassLoaderForArchive() {
        return archiveClassLoader;
    }


    public Class<?> loadClass(String className) {
    	Class<?> clazz;
    	if(isPhysicalFirst()) {
			clazz=loadClassPhysical(className);
			if(clazz!=null) return clazz;
			clazz=loadClassArchive(className);
			if(clazz!=null) return clazz;
		}
    	
    	clazz=loadClassArchive(className);
		if(clazz!=null) return clazz;
    	clazz=loadClassPhysical(className);
		if(clazz!=null) return clazz;
		
		return null;
	}

    private Class<?> loadClassArchive(String className) {
		if(archiveClassLoader==null) return null;
    	try{
			return archiveClassLoader.loadClass(className);
		}
		catch(Throwable t){
			ExceptionUtil.rethrowIfNecessary(t);}
		return null;
	}
    
    private Class<?> loadClassPhysical(String className) {
    	if(pclCollection==null) return null;
		// first we check 
		try{
			return pclCollection.loadClass(className);
		}
		catch(Throwable t){
			ExceptionUtil.rethrowIfNecessary(t);}
		
		return null;
	}
    
    public PCLCollection touchPCLCollection() throws IOException {
    	
    	if(pclCollection==null){
    		pclCollection=new PCLCollection(this,getClassRootDirectory(),getConfig().getClassLoader(),classLoaderMaxElements);
		}
    	return pclCollection;
    }
    
	public PCLCollection getPCLCollection() {
		return pclCollection;
	}

    
    

	/**
	 * remove all Page from Pool using this classloader
	 * @param cl
	 */
	public void clearPages(ClassLoader cl){
		pageSourcePool.clearPages(cl);
	}
	
    @Override
    public Resource getPhysical() {
    	return physical;
    }

    @Override
    public String getVirtualLowerCase() {
        return lcVirtual;
    }
    @Override
    public String getVirtualLowerCaseWithSlash() {
        return lcVirtualWithSlash;
    }

    @Override
    public Resource getArchive() {
        //initArchive();
        return archive;
    }

    @Override
    public boolean hasArchive() {
        return hasArchive;
    }
    
    @Override
    public boolean hasPhysical() {
        return physical!=null;
    }

    @Override
    public Resource getClassRootDirectory() {
        if(classRootDirectory==null) {
        	String path=getPhysical()!=null?
        			getPhysical().getAbsolutePath():
        			getArchive().getAbsolutePath();
        	
        	classRootDirectory=config.getDeployDirectory().getRealResource(
                                        StringUtil.toIdentityVariableName(
                                        		path)
                                );
        }
        return classRootDirectory;
    }
    
    /**
     * clones a mapping and make it readOnly
     * @param config
     * @return cloned mapping
     * @throws IOException
     */
    public MappingImpl cloneReadOnly(ConfigImpl config) {
    	return new MappingImpl(config,virtual,strPhysical,strArchive,inspect,physicalFirst,hidden,true,topLevel,appMapping,ignoreVirtual,appListener,classLoaderMaxElements);
    }
    
    @Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		maxlevel--;
        
		
		
		DumpTable htmlBox = new DumpTable("mapping","#ff6600","#ffcc99","#000000");
		htmlBox.setTitle("Mapping");
		htmlBox.appendRow(1,new SimpleDumpData("virtual"),new SimpleDumpData(virtual));
		htmlBox.appendRow(1,new SimpleDumpData("physical"),DumpUtil.toDumpData(strPhysical,pageContext,maxlevel,dp));
		htmlBox.appendRow(1,new SimpleDumpData("archive"),DumpUtil.toDumpData(strArchive,pageContext,maxlevel,dp));
		htmlBox.appendRow(1,new SimpleDumpData("inspect"),new SimpleDumpData(ConfigWebUtil.inspectTemplate(getInspectTemplateRaw(),"")));
		htmlBox.appendRow(1,new SimpleDumpData("physicalFirst"),new SimpleDumpData(Caster.toString(physicalFirst)));
		htmlBox.appendRow(1,new SimpleDumpData("readonly"),new SimpleDumpData(Caster.toString(readonly)));
		htmlBox.appendRow(1,new SimpleDumpData("hidden"),new SimpleDumpData(Caster.toString(hidden)));
		htmlBox.appendRow(1,new SimpleDumpData("appmapping"),new SimpleDumpData(Caster.toBoolean(appMapping)));
		htmlBox.appendRow(1,new SimpleDumpData("toplevel"),new SimpleDumpData(Caster.toString(topLevel)));
		htmlBox.appendRow(1,new SimpleDumpData("ClassLoaderMaxElements"),new SimpleDumpData(Caster.toString(classLoaderMaxElements)));
		return htmlBox;
    }

    /**
     * inspect template setting (Config.INSPECT_*), if not defined with the mapping the config setting is returned
     * @return
     */
    public short getInspectTemplate() {
		if(inspect==ConfigImpl.INSPECT_UNDEFINED) return config.getInspectTemplate();
		return inspect;
	}
    
    /**
     * inspect template setting (Config.INSPECT_*), if not defined with the mapping, Config.INSPECT_UNDEFINED is returned
     * @return
     */
    public short getInspectTemplateRaw() {
		return inspect;
	}
    
    
	

	@Override
    public PageSource getPageSource(String relPath) {
    	boolean isOutSide = false;
		relPath=relPath.replace('\\','/');
		if(relPath.indexOf('/')!=0) {
		    if(relPath.startsWith("../")) {
				isOutSide=true;
			}
			else if(relPath.startsWith("./")) {
				relPath=relPath.substring(1);
			}
			else {
				relPath="/"+relPath;
			}
		}
		return getPageSource(relPath,isOutSide);
    }
    
    @Override
    public PageSource getPageSource(String path, boolean isOut) {
        PageSource source=pageSourcePool.getPageSource(path,true);
        if(source!=null) return source;

        PageSourceImpl newSource = new PageSourceImpl(this,path,isOut);
        pageSourcePool.setPage(path,newSource);
        
        return newSource;//new PageSource(this,path);
    }
    
    /**
     * @return Returns the pageSourcePool.
     */
    public PageSourcePool getPageSourcePool() {
        return pageSourcePool;
    }

    @Override
    public void check() {
        //if(config instanceof ConfigServer) return;
        //ConfigWebImpl cw=(ConfigWebImpl) config;
        ServletContext cs = (config instanceof ConfigWebImpl)?((ConfigWebImpl)config).getServletContext():null;
        
        
        // Physical
        if(getPhysical()==null && strPhysical!=null && strPhysical.length()>0) {
            physical=ConfigWebUtil.getExistingResource(cs,strPhysical,null,config.getConfigDir(),FileUtil.TYPE_DIR,config);
            
        }
        // Archive
        if(getArchive()==null && strArchive!=null && strArchive.length()>0) {
            try {
                archive=ConfigWebUtil.getExistingResource(cs,strArchive,null,config.getConfigDir(),FileUtil.TYPE_FILE,
                        config);
                if(archive!=null) {
                    try {
                        archiveClassLoader = new ArchiveClassLoader(archive,getClass().getClassLoader());
                    } 
                    catch (MalformedURLException e) {
                        archive=null;
                    }
                }
                hasArchive=archive!=null;
            } 
            catch (IOException e) {}
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean isPhysicalFirst() {
        return physicalFirst;
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public String getStrArchive() {
        return strArchive;
    }

    @Override
    public String getStrPhysical() {
        return strPhysical;
    }

    @Override
    @Deprecated
    public boolean isTrusted() {
        return getInspectTemplate()==ConfigImpl.INSPECT_NEVER;
    }

    @Override
    public String getVirtual() {
        return virtual;
    }

	public boolean isAppMapping() {
		return appMapping;
	}


	public boolean isTopLevel() {
		return topLevel;
	}
	
	public PageSource getCustomTagPath(String name, boolean doCustomTagDeepSearch) {
		return searchFor(name, name.toLowerCase().trim(), doCustomTagDeepSearch);
	}
	
	public boolean ignoreVirtual(){
		return ignoreVirtual;
	}
	
	
	private PageSource searchFor(String filename, String lcName, boolean doCustomTagDeepSearch) {
		PageSource source=getPageSource(filename);
		if(isOK(source)) {
    		return source;
    	}
    	customTagPath.remove(lcName);
    	if(doCustomTagDeepSearch){
    		source = MappingUtil.searchMappingRecursive(this, filename, false);
    		if(isOK(source)) return source;
    	}
    	return null;
	}

	public static boolean isOK(PageSource ps) {
		if(ps==null) return false;
		return (ps.getMapping().isTrusted() && ((PageSourceImpl)ps).isLoad()) || ps.exists();
	}

	public static PageSource isOK(PageSource[] arr) {
		if(ArrayUtil.isEmpty(arr)) return null;
		for(int i=0;i<arr.length;i++) {
			if(isOK(arr[i])) return arr[i];
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return "StrPhysical:"+getStrPhysical()+";"+
		 "StrArchive:"+getStrArchive()+";"+
		 "Virtual:"+getVirtual()+";"+
		 "Archive:"+getArchive()+";"+
		 "Physical:"+getPhysical()+";"+
		 "topLevel:"+topLevel+";"+
		 "inspect:"+ConfigWebUtil.inspectTemplate(getInspectTemplateRaw(),"")+";"+
		 "physicalFirst:"+physicalFirst+";"+
		 "readonly:"+readonly+";"+
		 "hidden:"+hidden+";";
	}

	public ApplicationListener getApplicationListener() {
		if(appListener!=null) return appListener;
		return config.getApplicationListener();
	}
	
	public boolean getDotNotationUpperCase(){
		return ((ConfigImpl)config).getDotNotationUpperCase();
	}

}