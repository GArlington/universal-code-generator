//
//  TranslatorWindowController.h
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "XMLTreeModel.h"
#import "TreeIconModel.h"
#import "TreeNodeNamingModel.h"



@interface TranslatorWindowController : NSWindowController {
	
	@private
	XMLTreeModel *xmlTreeModel;							// Pointer to the tree model 
	TreeIconModel *iconModel;							// Icon model -
	NSString *strFilePath;								// Pointer to the path to the file -
	NSXMLDocument *xmlDocument;							// Pointer to the xml document w/the tag children relationships -
	NSXMLElement *selectedXMLNode;						// Node currently selected on the tree -
	
	
	IBOutlet NSTextField *bottomDisplayLabel;			// Bottom label to update user on status -
	IBOutlet NSProgressIndicator *progressWheel;		// Pointer to progress wheel (so I can turn on/off)
	IBOutlet NSPopUpButton *popupButton;				// Popup button holding conversion types -
	IBOutlet NSButton *saveAsButton;					// Pointer to the save as button -
	IBOutlet NSButton *codeGeneratorButton;				// Pointer to code generator button 
	IBOutlet NSOutlineView *treeView;					// Pointer to the tree -
	IBOutlet NSTextView *consoleTextField;				// Text field 
	IBOutlet NSProgressIndicator *progressIndicator;	// Pointer to the progress indicator -
	

}

// properties -
@property (retain) XMLTreeModel *xmlTreeModel;
@property (retain) TreeIconModel *iconModel;
@property (retain) NSString *strFilePath;
@property (retain) NSXMLDocument *xmlDocument;
@property (retain) NSXMLElement *selectedXMLNode;


@property (retain) IBOutlet NSTextField *bottomDisplayLabel;
@property (retain) IBOutlet NSProgressIndicator *progressWheel;
@property (retain) IBOutlet NSPopUpButton *popupButton;
@property (retain) IBOutlet NSButton *saveAsButton;
@property (retain) IBOutlet NSButton *codeGeneratorButton;
@property (retain) IBOutlet NSOutlineView *treeView;
@property (retain) IBOutlet	NSTextView *consoleTextField;
@property (retain) IBOutlet NSProgressIndicator *progressIndicator;



// Open and load an XML file -
-(IBAction)doOpenXMLFile:(NSButton *)sender;
-(IBAction)doSaveXMLFile:(NSButton *)sender;
-(IBAction)menuSelectionAction:(NSMenuItem *)sender;
-(IBAction)addTreeNode:(NSButton *)sender;
-(IBAction)removeTreeNode:(NSButton *)sender;
-(IBAction)runCodeGenerator:(NSButton *)sender;

// Create XML document methods -
-(void)createXMLDocumentFromFile:(NSString *)file;


@end
