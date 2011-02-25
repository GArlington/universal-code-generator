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
//  TableViewDelegate.h
//  Translator
//
//  Created by Jeffrey Varner on 1/21/11.

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
-(IBAction)tableClicked:(id)sender;
-(IBAction)addAttribute:(id)sender;
-(IBAction)removeAttribute:(id)sender;
-(void)tableView:(NSTableView *)tableView didClickTableColumn:(NSTableColumn *)tableColumn;

@end
