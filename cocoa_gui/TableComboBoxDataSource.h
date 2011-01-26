//
//  CCMLTableComboBoxDataSource.h
//  CCMLEditor
//
//  Created by Jeffrey Varner on 1/9/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "NSXMLElement_Category.h"
#import	"XMLAttributeWrapper.h"


@interface TableComboBoxDataSource : NSObject <NSComboBoxCellDataSource> {

	@private
	NSMutableArray *dataArray;		// Data array that I will use to hold options -
	NSXMLDocument *xmlDocument;		// Document we are going to use to get drop down list
	NSXMLElement *selectedXMLNode;	// What is the selected node?
	
	
}

// Set properties -
@property (retain) NSMutableArray *dataArray;
@property (retain) NSXMLDocument *xmlDocument;
@property (retain) NSXMLElement *selectedXMLNode;

// Methods -
- (id)comboBoxCell:(NSComboBoxCell *)aComboBoxCell objectValueForItemAtIndex:(NSInteger)index;
- (NSInteger)numberOfItemsInComboBoxCell:(NSComboBoxCell *)aComboBoxCell;

@end
