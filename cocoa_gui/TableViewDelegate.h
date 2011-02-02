//
//  TableViewDelegate.h
//  Translator
//
//  Created by Jeffrey Varner on 1/21/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "TableComboBoxDataSource.h"


@interface TableViewDelegate : NSObject <NSTableViewDelegate,NSTableViewDataSource> {

	@private
	IBOutlet NSTableView *tableView;				// Pointer to the table -
	IBOutlet NSWindow *window;						// Pointer to the window -
	TableComboBoxDataSource *comboDataSource;		// ComboBox data source -
	NSMutableArray *tableData;						// Array holding table data -
	NSXMLElement *selectedXMLNode;					// The selected *data model node* (not the GUI node) 

}

// property -
@property (retain) IBOutlet NSTableView *tableView;
@property (retain) TableComboBoxDataSource <NSComboBoxDataSource> *comboDataSource;
@property (retain) NSMutableArray *tableData;
@property (retain) NSXMLElement *selectedXMLNode;
@property (retain) NSWindow *window;


// Ok, so let's setup an action -
-(IBAction)updateTableValues:(NSButton *)sender;

@end
