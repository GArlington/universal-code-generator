package org.varnerlab.universaleditor.gui.parser;

/**
 * IDContentHandler - Set of string literals indicating which ContentHandler specialization to employ
 */
public interface IDContentHandler {
    public static final int CHID_STARTUP_HANDLER=0;
    public static final int CHID_BUILDER_HANDLER=1;
    public static final int CHID_PARAMETER_HANDLER=2;
    public static final int CHID_MATRIX_HANDLER=3;
    public static final int CHID_STARTUP_BUILDER_HANDLER=4;
    public static final int CHID_ARRAY_HANDLER=5;
    public static final int CHID_WIDGET_HANDLER=6;
    public static final int CHID_XMLTREE_HANDLER=7;
    public static final int CHID_SBMLTREE_HANDLER=8;
}

