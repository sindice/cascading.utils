package com.scaleunlimited.cascading;

import java.io.File;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Properties;

import cascading.flow.FlowConnector;
import cascading.flow.FlowProcess;
import cascading.flow.FlowProps;
import cascading.property.AppProps;
import cascading.scheme.Scheme;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

@SuppressWarnings({ "rawtypes", "serial" })
public abstract class BasePlatform implements Serializable {
    
    public static final int CLUSTER_REDUCER_COUNT = -1;
    
    public enum FlowPriority {
        LOW, MEDIUM, HIGH
    }
    
    public abstract String getPlatformType();

    public abstract boolean isLocal();
    
    // Cascading properties, used when constructing the FlowConnector
    protected Map<Object, Object> _props;
    
    protected File _logDir;
    
    protected BasePlatform() {
        // For serialization
    }
    
    public BasePlatform(Class applicationJarClass) {
        _props = new Properties();
        
        // We assume that this is getting called from some main() method, so Cascading
        // can find the application main class.
        AppProps.setApplicationJarClass(_props, applicationJarClass);
    }
    
    public void setProperty(String name, String value) {
        _props.put(name, value);
    }
    
    public void setProperty(String name, int value) {
        _props.put(name, value);
    }
    
    public String getProperty(String name) {
        return (String)_props.get(name);
    }
    
    public int getIntProperty(String name) {
        return (Integer)_props.get(name);
    }
    
    public void setJobPollingInterval(long interval) {
        FlowProps.setJobPollingInterval(_props, interval);
    }
    
    public void setLogLevel(Level level, String... packageNames) {
        // TODO check for existing level set for package name, and just update
        // TODO VMa - we still have a log4j dependency here...
        String curLogSettings = (String)_props.get("log4j.logger");
        if  ((curLogSettings == null) || (curLogSettings.trim().isEmpty())) {
            curLogSettings = "";
        }
        
        StringBuilder newLogSettings = new StringBuilder(curLogSettings);
        
        for (String packageName : packageNames) {
            if (newLogSettings.length() > 0) {
                newLogSettings.append(',');
            }
            
            newLogSettings.append(packageName);
            newLogSettings.append('=');
            newLogSettings.append(level.toString());
        }
        
        _props.put("log4j.logger", newLogSettings.toString());
    }
    
    public void assertPathExists(BasePath path, String description) {
        if (!path.exists()) {
            throw new InvalidParameterException(String.format("%s doesn't exist: %s", description, path));
        }
    }
    
    public Tap makeTap(Scheme scheme, BasePath path) throws Exception {
        return makeTap(scheme, path, SinkMode.KEEP);
    }

    public File getLogDir() {
        File result = _logDir;
        if (result == null) {
            result = getDefaultLogDir();
        }
        
        if (!result.exists()) {
            result.mkdirs();
        }
        
        return result;
    }
    
    public void setLogDir(File logDir) {
        _logDir = logDir;
    }
    
    public abstract File getDefaultLogDir();
    
    public abstract BasePath getTempDir() throws Exception;
    
    public abstract boolean isTextSchemeCompressable();

    public abstract void setNumReduceTasks(int numReduceTasks) throws Exception;

    public abstract int getNumReduceTasks() throws Exception;

    /**
     * Reset the number of reduce tasks to the default for the platform
     * 
     * @throws Exception
     */
    public abstract void resetNumReduceTasks() throws Exception;
    
    public abstract void setFlowPriority(FlowPriority priority) throws Exception;

    public abstract FlowConnector makeFlowConnector() throws Exception;

    public abstract FlowProcess makeFlowProcess() throws Exception;
    
    public abstract BasePath makePath(String path) throws Exception;

    public abstract BasePath makePath(BasePath parent, String subdir) throws Exception;

    public abstract Tap makeTap(Scheme scheme, BasePath path, SinkMode mode) throws Exception;

    public abstract Tap makeTemplateTap(Tap tap, String pattern, Fields fields) throws Exception;

    public abstract Scheme makeBinaryScheme(Fields fields) throws Exception;

    public abstract Scheme makeTextScheme(boolean isEnableCompression) throws Exception;
    
    public abstract Scheme makeTextScheme() throws Exception;
    
    public abstract boolean rename(BasePath src, BasePath dst) throws Exception;
    
    /**
     * Ensure that {@link #copySharedDirToLocal(FlowProcess, String)}
     * will have access to the data in <code>localDirName</code> by copying it
     * to a shared location (e.g., HDFS) if necessary.
     * @param localDirName of path to local copy of directory
     * @return shared directory name to pass to
     * {@link #copySharedDirToLocal(FlowProcess, String)}
     */
    public abstract String shareLocalDir(String localDirName);
    
    /**
     * Get a local copy of <code>sharedDirName</code>, which was shared by
     * {@link #shareLocalDir(String)}
     * @param flowProcess for the currently running Operation
     * @param sharedDirName where {@link #shareLocalDir(String)}
     * shared it
     * @return canonical name of path to local copy of directory
     */
    public abstract String copySharedDirToLocal(FlowProcess flowProcess, String sharedDirName);

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_logDir == null) ? 0 : _logDir.hashCode());
        result = prime * result + ((_props == null) ? 0 : _props.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BasePlatform other = (BasePlatform) obj;
        if (_logDir == null) {
            if (other._logDir != null)
                return false;
        } else if (!_logDir.equals(other._logDir))
            return false;
        if (_props == null) {
            if (other._props != null)
                return false;
        } else if (!_props.equals(other._props))
            return false;
        return true;
    }
    
    
}
