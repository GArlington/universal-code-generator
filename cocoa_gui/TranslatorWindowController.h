// Copyright (c) 2011 Varner Lab

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

//
//  TranslatorWindowController.h
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.

#import <Cocoa/Cocoa.h>
#import "XMLTreeModel.h"
#import "TreeIconModel.h"
#import "TreeNodeNamingModel.h"
#import "MyCustomSheetController.h"



@interface TranslatorWindowController : NSWindowController {
	
	@private
	XMLTreeModel *xmlTreeModel;							// Pointer to the tree model 
	TreeIconModel *iconModel;							// Icon model -
	NSString *strFilePath;								// Pointer to the path to the file -
	NSXMLDocument *xmlDocument;							// Pointer to the xml document w/the tag children relationships -
	NSXMLElement *selectedXMLNode;						// Node currently selected on the tree -
    MyCustomSheetController *customSheetController;     // Pointer to sheer controller -
	
	
	IBOutlet NSTextField *bottomDisplayLabel;			// Bottom label to update user on status -
	IBOutlet NSProgressIndicator *progressWheel;		// Pointer to progress wheel (so I can turn on/off)
	IBOutlet NSPopUpButton *popupButton;				// Popup button holding conversion types -
	IBOutlet NSButton *saveAsButton;					// Pointer to the save as button -
	IBOutlet NSButton *codeGeneratorButton;				// Pointer to code generator button
	IBOutlet NSButton *treeCheckButton;					// Pointer to the tree check button (makes sure all stuff is set correctly)
	IBOutlet NSOutlineView *treeView;					// Pointer to the tree -
	IBOutlet NSTextView *consoleTextField;				// Text field 
	IBOutlet NSProgressIndicator *progressIndicator;	// Pointer to the progress indicator -
	IBOutlet NSPopUpButton *fileTypePopupButton;		// Pointer to the popup button -
	IBOutlet NSButton *actionButton;					// Action button 
	IBOutlet NSTableView *propTableView;				// Property table view -
	
	

}

// properties -
@property (retain) XMLTreeModel *xmlTreeModel;
@property (retain) TreeIconModel *iconModel;
@property (retain) NSString *strFilePath;
@property (retain) NSXMLDocument *xmlDocument;
@property (retain) NSXMLElement *selectedXMLNode;
@property (retain) MyCustomSheetController *customSheetController;


@property (retain) IBOutlet NSTextField *bottomDisplayLabel;
@property (retain) IBOutlet NSProgressIndicator *progressWheel;
@property (retain) IBOutlet NSPopUpButton *popupButton;
@property (retain) IBOutlet NSButton *saveAsButton;
@property (retain) IBOutlet NSButton *codeGeneratorButton;
@property (retain) IBOutlet NSButton *treeCheckButton;
@property (retain) IBOutlet NSOutlineView *treeView;
@property (retain) IBOutlet	NSTextView *consoleTextField;
@property (retain) IBOutlet NSProgressIndicator *progressIndicator;
@property (retain) IBOutlet NSPopUpButton *fileTypePopupButton;
@property (retain) IBOutlet NSButton *actionButton;
@property (retain) IBOutlet NSTableView *propTableView;



// Open and load an XML file -
-(IBAction)doOpenXMLFile:(NSButton *)sender;
-(IBAction)doSaveXMLFile:(NSButton *)sender;
-(IBAction)menuSelectionAction:(NSMenuItem *)sender;
-(IBAction)addTreeNode:(NSButton *)sender;
-(IBAction)removeTreeNode:(NSButton *)sender;
-(IBAction)runCodeGenerator:(NSButton *)sender;
-(IBAction)checkTreeCompleteness:(NSButton *)sender;


// Create XML document methods -
-(void)createXMLDocumentFromFile:(NSString *)file;
-(void)createXMLDocumentFromData:(NSData *)data;


@end
