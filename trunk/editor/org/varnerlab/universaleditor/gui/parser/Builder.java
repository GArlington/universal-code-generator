package org.varnerlab.universaleditor.gui.parser;

// Import statements
import java.util.Vector;
import java.util.Enumeration;
import org.varnerlab.universaleditor.gui.widgets.IVLWidget;

import java.io.File;

/**
 *  Helper class use dto interact with the Akiva Builder system
 *  @author J.Varner
 */
public class Builder extends Object {
    // Class/instance attributes
    private static Builder _instance;
    
    /**
     *  Access the builder service. Builds and returns component based upon specification
     *  @param String Path to Blueprint
     *  @param int Handler
     *  @return Object
     */
    public static Object doBuild(String _path,int _handler) throws Exception {
        
        
        // Setup parser
        ADFParser _parser=ADFParser.getInstance();
        _parser.doParse(_path,_handler);
        
        // Get back construct from parser
        Vector _vecT=_parser.getConstructs();

        System.out.println("_vecT = "+_vecT.size());


        Vector _part=(Vector)_vecT.firstElement();

        System.out.println("_vecPart = "+_part.size());
        
        // Return construct
        return(_part.firstElement());
    }
    
    /**
     *  Access the builder service. Builds and returns component based upon specification
     *  @param String Path to Blueprint
     *  @param int Handler
     *  @return Object
     */
    public static Object doBuildLast(String _path,int _handler) throws Exception {
        
        // Setup parser
        ADFParser _parser=ADFParser.getInstance();
        _parser.doParse(_path,_handler);
        
        // Get back construct from parser
        Vector _vecT=_parser.getConstructs();
        Vector _part=(Vector)_vecT.lastElement();
        
        // Return construct
        return(_part.lastElement());
    }
    
    
    public static Builder instance(){
        if (_instance==null){
            _instance=new Builder();
        }
        return(_instance);
    }
    
    
    
    public String getPath(){
        //String path=instance().getClass().getPackage().getName().replace('.','/')+"/blueprint/";
        String path = "/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/blueprints/";
        return(path);
    }
    
}
